package server;

import client.model.EntityData;
import client.model.Vector2D;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

class ClientHandler extends NetworkCommunicator implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private EntityData pData;
    private CopyOnWriteArrayList<EntityData> updates;

    public ClientHandler(Socket clientSocket, int clientId) throws IOException {
        this.updates = new CopyOnWriteArrayList<>();
        this.clientSocket = clientSocket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.pData = new EntityData(new Vector2D(100, 650), clientId);
        uploadToClient(this.pData);
    }

    @Override
    public void run() {
        try {

            while (this.clientSocket.isConnected()) {
                //Thread.sleep(16);
                try {
                    Object fromClient = this.in.readObject();
                    if (fromClient instanceof EntityData) {
                        //System.out.println("Receiving Client Info" + ((PlayerData) fromClient).getPos().getX() + " " + ((PlayerData) fromClient).getPos().getY());
                        this.pData = (EntityData) fromClient;
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

    void uploadToClient(EntityData entityData) throws IOException {
        this.out.reset();
        this.out.writeObject(entityData);
        this.out.flush();
    }

    public CopyOnWriteArrayList<EntityData> getUpdates() {
        return this.updates;
    }
    public void wipeUpdates() {
        this.updates.clear();
    }
    EntityData getPlayerData() {
        return this.pData;
    }

}
