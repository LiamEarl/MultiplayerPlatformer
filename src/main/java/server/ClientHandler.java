package server;

import client.model.Player;
import client.model.PlayerData;
import client.model.Vector2D;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

class ClientHandler extends NetworkCommunicator implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PlayerData pData;
    private CopyOnWriteArrayList<PlayerData> updates;

    public ClientHandler(Socket clientSocket, int clientId) throws IOException {
        this.updates = new CopyOnWriteArrayList<>();
        this.clientSocket = clientSocket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.pData = new PlayerData(new Vector2D(100, 650), clientId);
        uploadToClient(this.pData);
    }

    @Override
    public void run() {
        try {

            while (this.clientSocket.isConnected()) {
                //Thread.sleep(16);
                try {
                    Object fromClient = this.in.readObject();
                    if (fromClient instanceof PlayerData) {
                        //System.out.println("Receiving Client Info" + ((PlayerData) fromClient).getPos().getX() + " " + ((PlayerData) fromClient).getPos().getY());
                        this.pData = (PlayerData) fromClient;
                        this.updates.add(this.pData);
                    }
                }catch(EOFException ignored) {}


            }

        } catch(IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    void uploadToClient(PlayerData playerData) throws IOException {
        this.out.reset();
        this.out.writeObject(playerData);
        this.out.flush();
    }

    public CopyOnWriteArrayList<PlayerData> getUpdates() {
        return this.updates;
    }
    public void wipeUpdates() {
        this.updates.clear();
    }
    PlayerData getPlayerData() {
        return this.pData;
    }

}
