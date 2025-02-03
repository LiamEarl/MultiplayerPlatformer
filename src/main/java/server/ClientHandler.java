package server;
import client.model.Player;
import client.model.Vector2D;
import java.awt.*;
import java.io.*;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Player player;
    private Player clientUpdate;

    private final Color[] playerColorCodes = {new Color(255, 0, 0), new Color(0, 255, 0), new Color(255, 125, 0), new Color(0, 125, 255), new Color(125, 255, 0)};
    private final Vector2D[] dimensions = {new Vector2D(41.5f, 60), new Vector2D(60, 41.5f),  new Vector2D(80, 31.25f), new Vector2D(50, 50), new Vector2D(31.25f, 80)};

    public ClientHandler(Socket clientSocket, int clientId) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.player = new Player(new Vector2D(100, 650), playerColorCodes[clientId], dimensions[clientId], clientId);
        Player[] initial = new Player[10];
        initial[clientId] = this.player;
        uploadToClient(initial);
    }

    @Override
    public void run() {
        try {

            while (this.clientSocket.isConnected()) {
                try {
                    Object fromClient = this.in.readObject();
                    if (fromClient instanceof Player) {
                        //System.out.println("Receiving Client Info" + ((PlayerData) fromClient).getPos().getX() + " " + ((PlayerData) fromClient).getPos().getY());
                        this.player = (Player) fromClient;
                        clientUpdate = (Player) fromClient;
                    }
                }catch(EOFException ignored) {}
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    void uploadToClient(Player[] entityData) throws IOException {
        this.out.reset();
        this.out.writeObject(entityData);
        this.out.flush();
    }

    public Player getClientUpdate() {
        return this.clientUpdate;
    }
    public void wipeUpdate() {
        this.clientUpdate = null;
    }
    Player getPlayerData() {
        return this.player;
    }

}
