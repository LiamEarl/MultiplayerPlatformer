package server;
import client.model.Player;
import client.model.Vector2D;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Player player;
    private Object clientUpdate;
    private long clientPing;
    private boolean connected = true;

    private ArrayList<Vector2D> dimensions;
    private ArrayList<Color> playerColorCodes;

    public ClientHandler(Socket clientSocket, int clientId, ArrayList<Vector2D> dimensions, ArrayList<Color> colorCodes) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.dimensions = dimensions;
        this.playerColorCodes = colorCodes;

        this.player = new Player(new Vector2D(100, 650), this.playerColorCodes.get(clientId), this.dimensions.get(clientId), clientId);
        uploadToClient(new Ping());
        Player[] initial = new Player[10];
        initial[clientId] = this.player;
        uploadToClient(initial);
    }

    @Override
    public void run() {
        try {
            while (connected) {
                try {
                    Object fromClient = this.in.readObject();
                    if (fromClient instanceof Player) {
                        this.player = (Player) fromClient;
                        clientUpdate = (Player) fromClient;
                    }else if(fromClient instanceof Ping) {
                        clientPing = System.currentTimeMillis() - ((Ping) fromClient).getTimeSent();
                        uploadToClient(new Message("SyncServer;" + (System.currentTimeMillis() + (clientPing / 2))));
                        System.out.println("Client " + this.player.getId() + " Latency: " + ((clientPing / 2)));
                    }else if(fromClient instanceof Message) {
                        String[] message = ((Message) fromClient).getMessage().split(";");
                        if(message[0].equals("Communication")) this.clientUpdate = fromClient;
                    }
                }catch(EOFException ignored) {}
                catch(SocketException e) {
                    System.out.println("Client " + this.player.getId() + " Disconnected");
                    this.clientSocket.close();
                    connected = false;
                }

            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    void uploadToClient(Object toWrite) throws IOException {
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
