package server;
import client.objects.Player;
import client.utility.Message;
import client.utility.Ping;
import client.utility.Vector2D;

import java.io.*;
import java.net.Socket;

import static server.GameServer.level;

/**
 * Client Handler class that handles the relationship with a client
 */
class ClientHandler implements Runnable {
    private Socket clientSocket; // Socket for connecting to the client
    private ObjectInputStream in; // ObjectInputStream for receiving serialized objects from the server
    private ObjectOutputStream out; // ObjectOutputStream for sending serialized objects to the server
    private Player player; // Player object captured from the client
    private Object clientUpdate; // Object containing a recent update from the client
    private long clientPing; // Latency of the client
    private boolean connected = true; // Whether the connection is still active

    /**
     * Creates a new ClientHandler object that manages the relationship with a single client.
     * @param clientSocket the socket used to connect to the client with.
     * @param clientId the ID of the client, 0 - 9
     * @throws IOException if something goes wrong while sending or receiving data
     */
    public ClientHandler(Socket clientSocket, int clientId) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());

        this.player = new Player(new Vector2D(100, 650), GameServer.getPlayerColorCodes().get(clientId), GameServer.getPlayerDimensions().get(clientId), clientId);

        Object[] initial = new Object[12];
        initial[10] = new Ping();
        initial[11] = new Message("CurrentLevel;" + level);
        initial[clientId] = this.player;
        uploadToClient(initial);
    }

    @Override
    public void run() {
        try {
            while (connected) {
                try {
                    Object fromClient = this.in.readObject(); // Wait for a new object to be received
                    if (fromClient instanceof Player) { // fromClient is a Player object, so make that our update
                        this.player = (Player) fromClient;
                        clientUpdate = fromClient;
                    }else if(fromClient instanceof Ping) { // It's a ping, which originally was sent from me
                        clientPing = System.currentTimeMillis() - ((Ping) fromClient).getTimeSent(); // get latency
                        uploadToClient(new Message("SyncServer;" + (System.currentTimeMillis() + (clientPing / 2))));
                        System.out.println("Client " + this.player.getId() + " Latency: " + ((clientPing / 2)));
                    }
                }catch(EOFException ignored) {}
                catch(Exception e) { // Something went wrong, the client is no longer connected
                    System.out.println("Client " + this.player.getId() + " Disconnected");
                    this.clientSocket.close();
                    connected = false;
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    void uploadToClient(Object toWrite) throws IOException { // Sends a serializable object to the client.
        this.out.reset();
        this.out.writeObject(toWrite);
        this.out.flush();
    }
    Object getClientUpdate() {
        return this.clientUpdate;
    }
    void wipeUpdate() {
        this.clientUpdate = null;
    }
    Player getPlayerData() {
        return this.player;
    }
    boolean getConnected() {
        return this.connected;
    }
}
