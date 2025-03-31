package client.game;
import client.objects.*;
import client.utility.Vector2D;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class that handles the backend of the game. Collision, loading + changing levels, and the GameLoop resides here.
 *
 */
public class GameClient {
    // Y value where if a player descends beneath it, they will perish.
    private static final int PLAYER_RESET_ZONE = 3000;

    // Variable used for DeltaTime between frames calculation. Value is in milliseconds, default is 60 fps.
    private static final float ASSUMED_UPDATE_TIME = 16;

    // ArrayList containing all level objects.
    private static final ArrayList<Level> levels = new ArrayList<>();

    // Current Level Index
    private static int currentLevel = 0;

    // Array containing all game objects. The number of game objects per level is capped to 300 for now.
    private static final GameObject[] gameObjects = new GameObject[300];

    // Player object containing the player that this client controls
    private static Player clientPlayer = null;

    // Serverhandler that manages the connection with the server
    static ServerHandler serverConnection = null;

    // GamePanel object instance that handles the GUI
    static GamePanel gamePanel = new GamePanel(gameObjects);

    // If the client was at one point connected to a server
    private static boolean previouslyConnected = false;

    // Main method
    public static void main(String[] args) {
        try {
            int currentTick = 0; // Storing the current frame
            long lastUpdate = System.currentTimeMillis(); // Used for DeltaTime
            long sendToServerTimer = System.currentTimeMillis(); // Used to determine when to send the next update to the server
            float fps; // Calculated frames per second.


            while(true) {
                long dt = System.currentTimeMillis() - lastUpdate; // Change in time from last frame to this frame
                float dtMod = dt / ASSUMED_UPDATE_TIME; // Value to multiply to game processes
                fps = (float) 1 / dt * 1000; // Frames per second calculation
                lastUpdate = System.currentTimeMillis();
                currentTick ++;

                if(serverConnection == null) { // The client is not connected to the server, try to connect
                    try {
                        serverConnection = handleServerConnection(gamePanel.getIP(), gamePanel.getTypedPort());
                    } catch (Exception e) {
                        if (currentTick % 60 == 0) {
                            System.out.println("Failed To Connect To The Server. Listening For A Connection.");
                        }
                    }

                    if(previouslyConnected) { // The client was previously connected but then got disconnected.
                        gamePanel.handlePlayerMovementInputs(dtMod);
                        updateGameObjects(dtMod, System.currentTimeMillis());
                        checkPlayerCollisions(dtMod);
                    }

                    gamePanel.renderScene(fps);

                    Thread.sleep(16);
                }else {
                    Thread.sleep(1); // By default 500 fps

                    sendToServerTimer = gameLoop(dtMod, sendToServerTimer, fps); // Game loop
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    static long gameLoop(float dtMod, long sendToServerTimer, float fps) throws IOException {
        previouslyConnected = true; // Game loop doesn't run without a server connection so previouslyConnected = true

        if(!serverConnection.getConnected()) { // We are no longer connected to the server
            serverConnection = null;
            return sendToServerTimer;
        }

        if(clientPlayer == null) { // We haven't created the player yet, but the server connection is there.
            if(serverConnection.getPlayerId() != -1) { // Make sure server has a player to give us.
                // Use the server connection to create the player
                clientPlayer = (Player) gameObjects[serverConnection.getPlayerId()];
                gamePanel.setClientPlayer(clientPlayer);
                // Create and load levels now that we can properly assign the player's spawn point.
                createLevels();
                loadCurrentLevel();
            }
        }else {
            gamePanel.handlePlayerMovementInputs(dtMod); // Capture movements from the keyboard with GamePanel.
            updateGameObjects(dtMod, getTime()); // Update the game objects using the deltatime and current time.
            checkPlayerCollisions(dtMod); // Apply all relevant collisions.
            gamePanel.renderScene(fps); // Display everything
            return serverConnection.handleOutgoingUpdates(sendToServerTimer); // Handle outgoing updates to the server
        }
        return  sendToServerTimer;
    }

    private static ServerHandler handleServerConnection(String ip, int port) throws IOException {
        Socket serverSocket = new Socket(ip, port); // Creates the connection to the server using the ip and port.
        System.out.println("Server Connected At IP: " + serverSocket.getRemoteSocketAddress());
        ServerHandler serverHandler = new ServerHandler(serverSocket); // Create ServerHandler object using the socket
        Thread serverThread = new Thread(serverHandler); // Create a thread that will run the connection independently
        serverThread.start(); // Start that thread.
        return serverHandler; // Hand the server connection to GameClient
    }

    static void updateGameObjects(float dtMod, long currentTime) {
        for (GameObject gameObject : gameObjects) { // Loop through all GameObjects
            if (gameObject == null) continue; // This GameObject doesn't exist, continue to the next.
            if (gameObject instanceof Player) { // This GameObject is a player
                Player playerObject = (Player) gameObject;
                playerObject.update(dtMod, currentTime); // Update the Player even if it is not the clientPlayer
                if (!playerObject.getCommunication().isEmpty() && playerObject.getId() != clientPlayer.getId()) {
                    gamePanel.newMessageFromServer(playerObject); // If the player has a message send it to the GamePanel
                }
                continue;
            }
            gameObject.update(dtMod, currentTime); // Update the non-player GameObjects
        }
    }
    private static void checkPlayerCollisions(float dtMod) {
        for (GameObject obj : gameObjects) { // Loop through all GameObjects
            if (obj == null) continue; // The GameObject doesn't exist, continue to the next
            if (obj instanceof Box) { // The GameObject is a Box
                for (int i = 0; i < 10; i++) { // Loop through all possible Players
                    if (!(gameObjects[i] instanceof Player)) continue;
                    Player currentPlayer = (Player) gameObjects[i];
                    if (currentPlayer == null) continue; // The player doesn't exist, continue to the next.
                    if (currentPlayer.getGodMode()) continue; // No collisions with a player in spectate mode.
                    handlePlayerBoxCollision(currentPlayer, (Box) obj, dtMod); // Collision detection and response.
                    if (currentPlayer.getPos().getY() > PLAYER_RESET_ZONE) currentPlayer.respawn();
                }
            }else if(obj instanceof Tip) { // The GameObject is a tip
                if(clientPlayer == null) continue;
                handlePlayerTipCollision(clientPlayer, (Tip) obj); // Collision detection and response.
            }
        }
    }
    private static void handlePlayerBoxCollision(GameObject dynamic, Box toCollide, float dtMod) {
        Vector2D pPos = dynamic.getPos(); // Ease of use values
        Vector2D pDim = dynamic.getDim();
        Vector2D pVel = dynamic.getVelocity();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();
        Vector2D oVel = toCollide.getVelocity();

        // toCollide is a Death Box, don't use bounding box in collision detection for accuracy.
        if (toCollide.getType() == BoxType.DEATH_BOX && dynamic instanceof Player) {
            boolean isColliding = pPos.getX() < oPos.getX() + oDim.getX() &&
                    pPos.getX() + pDim.getX() > oPos.getX() &&
                    pPos.getY() < oPos.getY() + oDim.getY() &&
                    pPos.getY() + pDim.getY() > oPos.getY();
            if (!isColliding) return;
            ((Player) dynamic).respawn();
            return;
        }

        // Calculate if the bounding box is colliding.
        boolean isBoundingBoxColliding = pPos.getX() + pVel.getX() < oPos.getX() + oDim.getX() + oVel.getX() &&
                pPos.getX() + pDim.getX() + pVel.getX() > oPos.getX() + oVel.getX() &&
                pPos.getY() + pVel.getY() < oPos.getY() + oDim.getY() + oVel.getY() &&
                pPos.getY() + pDim.getY() + pVel.getY() > oPos.getY() + oVel.getY();

        if (!isBoundingBoxColliding) return; // Don't respond to the collision if it is not colliding.

        // Calculate the overlap of the two rectangles.
        double overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        double overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

        if (overlapX < overlapY) {
            if (pPos.getX() < oPos.getX()) { // Left side of the box
                pPos.addXY(-overlapX, 0); // Move the player so that it is not inside of the box
                // Set the x velocity of the player to the x velocity of the Box
                if (pPos.getX() + pDim.getX() <= oPos.getX()) {
                    dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                }
            } else { // Right side of the box
                pPos.addXY(overlapX, 0); // Move the player so that it is not inside of the box
                // Set the x velocity of the player to the x velocity of the Box
                if (pPos.getX() >= oPos.getX() + oDim.getX()) {
                    dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                }
            }
        } else {
            if (pPos.getY() < oPos.getY()) { // Top of the box
                pPos.addXY(0, -overlapY); // Move the player so that it is not inside of the box
                if(dynamic instanceof Player) ((Player) dynamic).setGrounded(true); // Set the player to be grounded
                // Find the new value of the player's x velocity depending on what they are standing on
                float xModifier = (float) ((toCollide.getType() == BoxType.ICE) ? dynamic.getVelocity().getX() : dynamic.getVelocity().getX() * (1 - 0.03f * dtMod));
                dynamic.getVelocity().setXY(xModifier, oVel.getY() * 0.9f); // Apply the new velocity
                if (toCollide.getType() == BoxType.TRAMPOLINE) { // If the player is standing on a trampoline
                    dynamic.getVelocity().addXY(0, -36); // Toss them upwards
                    if(dynamic instanceof Player) ((Player) dynamic).setGrounded(false); // They are no longer grounded
                }
            } else { // Bottom of the box
                pPos.addXY(0, overlapY); // Move the player so that it is not inside of the box
                dynamic.getVelocity().setXY(dynamic.getVelocity().getX(), -dynamic.getVelocity().getY()); // Invert the y velocity
            }
        }

        // If the box is a checkpoint set the player's spawn point.
        if (toCollide.getType() == BoxType.CHECKPOINT && dynamic instanceof Player)
            ((Player) dynamic).setSpawnPoint(new Vector2D(oPos.getX() + (oDim.getX() / 2) - (pDim.getX() / 2), oPos.getY() - pDim.getY()));
        // If the box is a checkpoint set the player to spectate mode.
        if (toCollide.getType() == BoxType.FINISH_LINE && dynamic.equals(clientPlayer)) clientPlayer.setGodMode(true);
    }
    private static void handlePlayerTipCollision(GameObject dynamic, Tip toCollide) {
        Vector2D pPos = dynamic.getPos(); // Ease of use variables
        Vector2D pDim = dynamic.getDim();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();

        boolean isColliding = pPos.getX() < oPos.getX() + oDim.getX() &&
                pPos.getX() + pDim.getX() > oPos.getX() &&
                pPos.getY() < oPos.getY() + oDim.getY() &&
                pPos.getY() + pDim.getY() > oPos.getY();

        if (!isColliding) return; // If there is no collision ignore the response

        gamePanel.addTip(toCollide.getTipContent()); // Add the tip's message to the GamePanel
    }

    private static void createLevels() throws IOException {
        InputStream is = GameClient.class.getResourceAsStream("/levels.json"); // Try and load the levels.json file
        if (is == null) {
            // The json file could not be found
            throw new IOException("Could not find resource: " + "/levels.json");
        }

        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8); // Create a scanner object to read the json
        String jsonString = scanner.useDelimiter("\\A").next(); // Get the string of json
        scanner.close();

        JSONArray jsonArray = new JSONArray(jsonString); // Create a jsonArray object using the json string

        // Create a map of values that are used to convert string color values to actual values
        Map<String, Color> colors = Map.of(
        "checkpointColor", new Color(4, 126, 220),
        "deathBoxColor", new Color(239, 26, 26),
        "finishLineColor", new Color(26, 140, 41),
        "wallColor", new Color(32, 32, 45),
        "trampolineColor", new Color(255, 213, 0),
        "iceColor", new Color(4, 169, 222)
        );

        // Create a map of values that are used to convert string type values to BoxType values
        Map<String, BoxType> boxTypeMap = Map.of(
            "Wall", BoxType.WALL,
            "Checkpoint", BoxType.CHECKPOINT,
            "FinishLine", BoxType.FINISH_LINE,
            "DeathBox", BoxType.DEATH_BOX,
            "Trampoline", BoxType.TRAMPOLINE,
            "Ice", BoxType.ICE
        );

        // Create a buffer to add GameObjects to for the use of creating a level
        ArrayList<GameObject> buffer = new ArrayList<>();

        // Loop through the levels stored in the json array
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray rowArray = jsonArray.getJSONArray(i); // Get the array storing an individual level
            Vector2D spawnLoc = new Vector2D(100, 700); // Set the default spawn location for the level

            for (int j = 0; j < rowArray.length(); j++) { // Loop through objects in the level
                JSONObject jsonObject = rowArray.getJSONObject(j); // Get the jsonObject

                String type = jsonObject.getString("type"); // Get the type of the object
                if (type.equals("Spawn")) { // If the type is a spawn location, edit the spawn vector
                    spawnLoc.setX(jsonObject.getInt("x"));
                    spawnLoc.setY(jsonObject.getInt("y"));
                    continue;
                }

                // Get the position and dimensions of the object
                Vector2D boxPosition = new Vector2D(jsonObject.getInt("x"), jsonObject.getInt("y"));
                Vector2D boxDimensions = new Vector2D(jsonObject.getInt("w"), jsonObject.getInt("h"));

                // If the type is a tip object, add that to the buffer
                if (type.equals("Tip")) {
                    buffer.add(new Tip(boxPosition, boxDimensions, jsonObject.getString("message")));
                    continue;
                }

                // Get the equation used for movement in the object
                String boxEq = jsonObject.has("equation") ? jsonObject.getString("equation") : "#~#";
                // Get the color of the object
                Color boxCol = colors.get(jsonObject.getString("color"));
                // At this point the only thing the object could be is a box, so add the box
                buffer.add(new Box(boxPosition, boxDimensions, boxCol, boxTypeMap.get(type), boxEq));
            }
            levels.add(new Level((ArrayList<GameObject>) buffer.clone(), spawnLoc)); // Create the level using the buffer
            buffer.clear(); // Clear the buffer for the hext level
        }
    }
    private static void loadCurrentLevel() {
        if(currentLevel < 0 || levels.size() <= currentLevel) return; // The level you are trying to load is invalid

        ArrayList<GameObject> objects = levels.get(currentLevel).getObjects(); // Get the level's GameObjects
        if(objects.isEmpty()) return; // No objects? Get outta here
        for(int i = 10; i < gameObjects.length; i++) {
            gameObjects[i] = null; // Delete all old gameObjects from the array besides players
        }
        for (int i = 0; i < objects.size(); i++) { // Loop through all objects and add them into the array.
            gameObjects[i + 10] = objects.get(i);
        }
        if(clientPlayer != null) { // Set the player's spawn point and respawn them.
            clientPlayer.setSpawnPoint(levels.get(currentLevel).getSpawnPoint());
            clientPlayer.respawn();
            clientPlayer.setGodMode(false);
        }
    }
    public static void setLevel(int newLevel) { // Go to a new Level
        currentLevel = newLevel;
        loadCurrentLevel();
    }
    public static GameObject[] getGameObjects() {
        return gameObjects;
    }
    static long getTime() {
        if(serverConnection == null) return System.currentTimeMillis();
        return serverConnection.getServerTime();
    }
}
