package server;
import client.objects.Player;
import client.utility.Vector2D;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import static server.GameServer.level;

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
                catch(Exception e) {
                    System.out.println("Client " + this.player.getId() + " Disconnected");
                    this.clientSocket.close();
                    connected = false;
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
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
