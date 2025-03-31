package client.game;
import client.objects.Player;
import client.utility.Vector2D;
import client.utility.Message;
import client.utility.Ping;
import java.io.*;
import java.net.Socket;

/**
 * Handles the connection with a server in a separate thread.
 */
class ServerHandler implements Runnable {
    // Socket connecting to the server
    private final Socket serverSocket;

    // ObjectInputStream used to receive serialized objects from the server
    private final ObjectInputStream in;

    // ObjectOutputStream used to send serialized objects to the server
    private final ObjectOutputStream out;

    // The player that is associated with this client
    private Player clientPlayer;

    // The last velocity of the player, used for knowing when to update to the server
    private Vector2D lastVelocity;

    // Offset applied to System.currentTimeMillis() to synchronize with the server time
    private long serverOffset = 0;

    // If the connection is still active
    private boolean connected;

    /**
     * Creates a ServerHandler object that manages the connection with a server.
     * @param serverSocket the connection socket to the server.
     * @throws IOException
     */
    ServerHandler(Socket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        // Creates input and output streams, making sure to establish the output first to avoid a deadlock.
        this.out = new ObjectOutputStream(serverSocket.getOutputStream());
        this.in = new ObjectInputStream(serverSocket.getInputStream());
        this.connected = true;
        this.lastVelocity = new Vector2D(0, 0);
    }

    @Override
    public void run() {
        try {
            while (this.connected) { // Continue logic while the server is connected
                try {
                    Object fromServerObject = this.in.readObject(); // Get a new object from the server
                    if(fromServerObject == null) continue; // The object isn't anything, so next object.
                    if(fromServerObject instanceof Object[]) { // The object is a bundle of objects.
                        Object[] fromServer = (Object[]) fromServerObject;
                        for(Object update : fromServer) {
                            if(update == null) continue;
                            // It's a player, update the players using that information.
                            if(update instanceof Player) updatePlayers((Player) update);
                            else if(update instanceof Message) interpretMessage((Message) update); // It's a message
                        }
                    }else if(fromServerObject instanceof Ping) {
                        writeToServer(fromServerObject); // Send the ping back to the server
                    }else if(fromServerObject instanceof Message) {
                        interpretMessage((Message) fromServerObject); // Interpret a message from the server
                    }
                }catch(EOFException ignored) {} catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Ooops something went wrong, server is no longer connected.
            this.connected = false;
        }
    }

    boolean getConnected() {
        return this.connected;
    }

    private void interpretMessage(Message toInterpret) { // Interpret a message from the server
        String[] message = toInterpret.getMessage().split(";");
        if(message[0].equals("SyncServer")) {
            this.serverOffset = System.currentTimeMillis() - Long.parseLong(message[1]);
        }else if(message[0].equals("CurrentLevel")) {
            GameClient.setLevel(Integer.parseInt(message[1]));
        }if(message[0].equals("Communication")) {}
    }

    // Overwrite the Player if the server has a more recent version.
    private void updatePlayers(Player playerUpdate) {
        if(playerUpdate == null) return;

        int id = playerUpdate.getId();

        if (GameClient.getGameObjects()[id] == null) { // The Player doesn't exist yet, so create one
            GameClient.getGameObjects()[id] = playerUpdate;
            if(this.clientPlayer == null) this.clientPlayer = playerUpdate;
            System.out.println("INITIALIZED PLAYER " + playerUpdate.getId());
        } else {
            Player ghost = (Player) GameClient.getGameObjects()[id]; // Update the fields of the player with the new info
            ghost.setPos(playerUpdate.getPos());
            ghost.setVel(playerUpdate.getVelocity());
            ghost.setGodMode(playerUpdate.getGodMode());
            ghost.setColor(playerUpdate.getColor());
            ghost.setCommunication(playerUpdate.getCommunication());
        }
    }

    void writeToServer(Object toWrite) throws IOException { // Send and object up to the server
        this.out.reset();
        this.out.writeObject(toWrite);
        this.out.flush();
    }

    /**
     *
     * @param sendToServerTimer the last time that the player's info was updated to the server
     * @return the last time that the player's info was updated to the server
     * @throws IOException something went wrong uploading
     */
    long handleOutgoingUpdates(long sendToServerTimer) throws IOException {
        if(this.clientPlayer != null) {
            long diff = System.currentTimeMillis() - sendToServerTimer;
            Vector2D velocityDiff = new Vector2D(clientPlayer.getVelocity().getX() - this.lastVelocity.getX(), clientPlayer.getVelocity().getY() - this.lastVelocity.getY());
            double magnitude = velocityDiff.length();
            if ((magnitude > 0.1f && diff > 50) || diff > 1000 || magnitude > 5 || (this.clientPlayer.getGodMode() && diff > 50)) {
                writeToServer(this.clientPlayer);
                this.clientPlayer.setCommunication("");
                this.lastVelocity = this.clientPlayer.getVelocity().copy();
                return System.currentTimeMillis();
            }
        }
        return sendToServerTimer;
    }

    /**
     * Gets the estimated time of the server
     * @return the estimated time of the server
     */
    long getServerTime() {
        return System.currentTimeMillis() - serverOffset;
    }

    int getPlayerId() {
        if(this.clientPlayer == null) return -1;
        return this.clientPlayer.getId();
    }
}
