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

public class GameClient {
    private static final int PLAYER_RESET_ZONE = 3000;
    private static final float ASSUMED_UPDATE_TIME = 16;
    private static final ArrayList<Level> levels = new ArrayList<>();
    private static int currentLevel = 0;
    private static final GameObject[] gameObjects = new GameObject[300];
    private static Player clientPlayer = null;
    static ServerHandler serverConnection = null;
    static GamePanel gamePanel = new GamePanel(gameObjects);

    private static ServerHandler handleServerConnection(String ip, int port) throws IOException {
        Socket serverSocket = new Socket(ip, port); // Server IP and port the-tower.net
        System.out.println("Server Connected At IP: " + serverSocket.getRemoteSocketAddress());
        ServerHandler serverHandler = new ServerHandler(serverSocket);
        Thread serverThread = new Thread(serverHandler);
        serverThread.start();
        return serverHandler;
    }

    public static void main(String[] args) {
        try {
            int currentTick = 0;

            long lastUpdate = System.currentTimeMillis();
            long sendToServerTimer = System.currentTimeMillis();
            float fps;


            boolean previouslyConnected = false;

            while(true) {
                long dt = System.currentTimeMillis() - lastUpdate;
                float dtMod = dt / ASSUMED_UPDATE_TIME;
                fps = (float) 1 / dt * 1000;
                lastUpdate = System.currentTimeMillis();
                currentTick ++;

                if(serverConnection == null) {

                    try {
                        serverConnection = handleServerConnection(gamePanel.getIP(), gamePanel.getTypedPort());
                    } catch (Exception e) {
                        if (currentTick % 60 == 0) {
                            System.out.println("Failed To Connect To The Server. Listening For A Connection.");
                        }
                    }

                    if(previouslyConnected) {
                        gamePanel.handlePlayerMovementInputs(dtMod);
                        updateGameObjects(dtMod, System.currentTimeMillis());
                        checkPlayerCollisions(dtMod);
                    }

                    gamePanel.renderScene(fps);

                    Thread.sleep(16);
                }else {
                    previouslyConnected = true;

                    if(!serverConnection.getConnected()) {
                        serverConnection = null;
                        continue;
                    }

                    if(!gamePanel.initializedPlayer()) {
                        if(serverConnection.getPlayerId() != -1) {
                            clientPlayer = (Player) gameObjects[serverConnection.getPlayerId()];
                            gamePanel.setClientPlayer(clientPlayer);
                        }

                        createLevels();
                        loadCurrentLevel();

                        Thread.sleep(16);
                    }else {
                        gamePanel.handlePlayerMovementInputs(dtMod);
                        updateGameObjects(dtMod, serverConnection.getServerTime());
                        checkPlayerCollisions(dtMod);
                        gamePanel.renderScene(fps);
                        sendToServerTimer = serverConnection.handleOutgoingUpdates(sendToServerTimer);

                        Thread.sleep(1);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void updateGameObjects(float dtMod, long currentTime) {
        for (GameObject gameObject : gameObjects) {
            if (gameObject == null) continue;
            if (gameObject instanceof Player) {
                Player playerObject = (Player) gameObject;
                playerObject.update(dtMod, currentTime);
                if (!playerObject.getCommunication().isEmpty() && playerObject.getId() != clientPlayer.getId()) {
                    gamePanel.newMessageFromServer(playerObject);
                }
                continue;
            }
            gameObject.update(dtMod, currentTime);
        }
    }
    static long getTime() {
        if(serverConnection == null) return System.currentTimeMillis();
        return serverConnection.getServerTime();
    }
    private static void checkPlayerCollisions(float dtMod) {
        for (GameObject obj : gameObjects) {
            if (obj == null) continue;
            if (obj instanceof Box) {
                for (int i = 0; i < 10; i++) {
                    if (!(gameObjects[i] instanceof Player)) continue;
                    Player currentPlayer = (Player) gameObjects[i];
                    if (currentPlayer == null) continue;
                    if (currentPlayer.getGodMode()) continue;
                    handlePlayerBoxCollision(currentPlayer, (Box) obj, dtMod);
                    if (currentPlayer.getPos().getY() > PLAYER_RESET_ZONE) currentPlayer.respawn();
                }
            }else if(obj instanceof Tip) {
                if(clientPlayer == null) continue;
                handlePlayerTipCollision(clientPlayer, (Tip) obj);
            }
        }
    }
    private static void handlePlayerBoxCollision(GameObject dynamic, Box toCollide, float dtMod) {
        Vector2D pPos = dynamic.getPos();
        Vector2D pDim = dynamic.getDim();
        Vector2D pVel = dynamic.getVelocity();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();
        Vector2D oVel = toCollide.getVelocity();

        if (toCollide.getType() == BoxType.DEATH_BOX && dynamic instanceof Player) {
            boolean isColliding = pPos.getX() < oPos.getX() + oDim.getX() &&
                    pPos.getX() + pDim.getX() > oPos.getX() &&
                    pPos.getY() < oPos.getY() + oDim.getY() &&
                    pPos.getY() + pDim.getY() > oPos.getY();
            if (!isColliding) return;
            ((Player) dynamic).respawn();
            return;
        }

        boolean isBoundingBoxColliding = pPos.getX() + pVel.getX() < oPos.getX() + oDim.getX() + oVel.getX() &&
                pPos.getX() + pDim.getX() + pVel.getX() > oPos.getX() + oVel.getX() &&
                pPos.getY() + pVel.getY() < oPos.getY() + oDim.getY() + oVel.getY() &&
                pPos.getY() + pDim.getY() + pVel.getY() > oPos.getY() + oVel.getY();

        if (!isBoundingBoxColliding) return;

        double overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        double overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

        if (overlapX < overlapY) {
            if (pPos.getX() < oPos.getX()) {
                pPos.addXY(-overlapX, 0);
                if (pPos.getX() + pDim.getX() <= oPos.getX()) {
                    dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                }
            } else {
                pPos.addXY(overlapX, 0);
                if (pPos.getX() >= oPos.getX() + oDim.getX()) {
                    dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                }
            }
        } else {
            if (pPos.getY() < oPos.getY()) {
                pPos.addXY(0, -overlapY);
                if(dynamic instanceof Player) ((Player) dynamic).setGrounded(true);
                float xModifier = (float) ((toCollide.getType() == BoxType.ICE) ? dynamic.getVelocity().getX() : dynamic.getVelocity().getX() * (1 - 0.03f * dtMod));
                dynamic.getVelocity().setXY(xModifier, oVel.getY() * 0.9f);
                if (toCollide.getType() == BoxType.TRAMPOLINE) {
                    dynamic.getVelocity().addXY(0, -36);
                    if(dynamic instanceof Player) ((Player) dynamic).setGrounded(false);
                }
            } else {
                pPos.addXY(0, overlapY);
                dynamic.getVelocity().setXY(dynamic.getVelocity().getX(), -dynamic.getVelocity().getY());
            }
        }

        if (toCollide.getType() == BoxType.CHECKPOINT && dynamic instanceof Player)
            ((Player) dynamic).setSpawnPoint(new Vector2D(oPos.getX() + (oDim.getX() / 2) - (pDim.getX() / 2), oPos.getY() - pDim.getY()));
        if (toCollide.getType() == BoxType.FINISH_LINE && dynamic.equals(clientPlayer)) clientPlayer.setGodMode(true);
    }
    private static void handlePlayerTipCollision(GameObject dynamic, Tip toCollide) {
        Vector2D pPos = dynamic.getPos();
        Vector2D pDim = dynamic.getDim();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();

        boolean isColliding = pPos.getX() < oPos.getX() + oDim.getX() &&
                pPos.getX() + pDim.getX() > oPos.getX() &&
                pPos.getY() < oPos.getY() + oDim.getY() &&
                pPos.getY() + pDim.getY() > oPos.getY();

        if (!isColliding) return;

        gamePanel.addTip(toCollide.getTipContent());
    }

    private static void createLevels() throws IOException {
        InputStream is = GameClient.class.getResourceAsStream("/levels.json");
        if (is == null) {
            throw new IOException("Could not find resource: " + "/levels.json");
        }

        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
        String jsonString = scanner.useDelimiter("\\A").next();
        scanner.close();

        JSONArray jsonArray = new JSONArray(jsonString);

        Map<String, Color> colors = Map.of(
        "checkpointColor", new Color(4, 126, 220),
        "deathBoxColor", new Color(239, 26, 26),
        "finishLineColor", new Color(26, 140, 41),
        "wallColor", new Color(32, 32, 45),
        "trampolineColor", new Color(255, 213, 0),
        "iceColor", new Color(4, 169, 222)
        );
        Map<String, BoxType> boxTypeMap = Map.of(
            "Wall", BoxType.WALL,
            "Checkpoint", BoxType.CHECKPOINT,
            "FinishLine", BoxType.FINISH_LINE,
            "DeathBox", BoxType.DEATH_BOX,
            "Trampoline", BoxType.TRAMPOLINE,
            "Ice", BoxType.ICE
        );

        ArrayList<GameObject> buffer = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray rowArray = jsonArray.getJSONArray(i);
            Vector2D spawnLoc = new Vector2D(100, 700);

            for (int j = 0; j < rowArray.length(); j++) {
                JSONObject jsonObject = rowArray.getJSONObject(j);

                String type = jsonObject.getString("type");
                if (type.equals("Spawn")) {
                    spawnLoc.setX(jsonObject.getInt("x"));
                    spawnLoc.setY(jsonObject.getInt("y"));
                    continue;
                }

                Vector2D boxPosition = new Vector2D(jsonObject.getInt("x"), jsonObject.getInt("y"));
                Vector2D boxDimensions = new Vector2D(jsonObject.getInt("w"), jsonObject.getInt("h"));

                if (type.equals("Tip")) {
                    buffer.add(new Tip(boxPosition, boxDimensions, jsonObject.getString("message")));
                    continue;
                }

                String boxEq = jsonObject.has("equation") ? jsonObject.getString("equation") : "#~#";
                Color boxCol = colors.get(jsonObject.getString("color"));

                buffer.add(new Box(boxPosition, boxDimensions, boxCol, boxTypeMap.get(type), boxEq));
            }
            levels.add(new Level((ArrayList<GameObject>) buffer.clone(), spawnLoc));
            buffer.clear();
        }
    }
    private static void loadCurrentLevel() {
        if(currentLevel < 0 || levels.size() <= currentLevel) return;

        ArrayList<GameObject> objects = levels.get(currentLevel).getObjects();
        if(objects.isEmpty()) return;
        for(int i = 10; i < gameObjects.length; i++) {
            gameObjects[i] = null;
        }
        for (int i = 0; i < objects.size(); i++) {
            gameObjects[i + 10] = objects.get(i);
        }
        if(clientPlayer != null) {
            clientPlayer.setSpawnPoint(levels.get(currentLevel).getSpawnPoint());
            clientPlayer.respawn();
            clientPlayer.setGodMode(false);
        }
    }
    public static void setLevel(int newLevel) {
        currentLevel = newLevel;
        loadCurrentLevel();
    }
    public static GameObject[] getGameObjects() {
        return gameObjects;
    }

}
