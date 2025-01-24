package client.game;
import client.model.GameObject;
import client.model.Player;
import client.model.EntityData;
import client.model.Vector2D;
import server.NetworkCommunicator;

import java.awt.*;
import java.io.*;
import java.net.Socket;

class ServerHandler extends NetworkCommunicator implements Runnable {
    private Socket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private GameObject[] gameObjects;
    private EntityData mainPlayerData;

    ServerHandler(Socket serverSocket, GameObject[] gameObjects) throws IOException {
        this.serverSocket = serverSocket;
        this.out = new ObjectOutputStream(serverSocket.getOutputStream());
        this.in = new ObjectInputStream(serverSocket.getInputStream());
        this.gameObjects = gameObjects;
    }

    @Override
    public void run() {
        try {
            Color[] playerColorCodes = {new Color(255, 0, 0), new Color(0, 255, 0), new Color(255, 125, 0), new Color(0, 125, 255), new Color(125, 255, 0)};
            Vector2D[] dimensions = {new Vector2D(41.5f, 60), new Vector2D(60, 41.5f),  new Vector2D(80, 31.25f), new Vector2D(50, 50), new Vector2D(55.6f, 45)};

            while (this.serverSocket.isConnected()) {

                try {
                    Object fromServerObject = this.in.readObject();
                    if (fromServerObject instanceof EntityData) {
                        EntityData fromServer = (EntityData) fromServerObject;

                        if(this.mainPlayerData == null) {
                            this.mainPlayerData = fromServer;
                            this.gameObjects[this.mainPlayerData.getId()] = new Player(this.mainPlayerData, playerColorCodes[this.mainPlayerData.getId()], dimensions[this.mainPlayerData.getId()]);
                            System.out.println("INITIALIZED PLAYER " + fromServer.getId());
                        }else {
                            if(gameObjects[fromServer.getId()] == null) {
                                this.gameObjects[fromServer.getId()] = new Player(fromServer, playerColorCodes[fromServer.getId()], dimensions[fromServer.getId()]);
                                System.out.println("INITIALIZED PLAYER " + fromServer.getId());
                            }else {
                                ((Player) this.gameObjects[fromServer.getId()]).setPlayerData(fromServer);
                                System.out.println("Receiving Position X:" + fromServer.getPos().getX() + " Y:" + fromServer.getPos().getY());
                            }
                        }
                    }
                }catch(EOFException ignored) {} catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeToServer() throws IOException {
        this.out.reset();
        EntityData stuff = new EntityData(new Vector2D(this.mainPlayerData.getPos().getX(), this.mainPlayerData.getPos().getY()), this.mainPlayerData.getId());
        this.out.writeObject(stuff);
        this.out.flush();
        //System.out.println("Writing To Server X:" + this.mainPlayerData.getPos().getX() + " Y:" + this.mainPlayerData.getPos().getY());
    }

    int getPlayerId() {
        if(this.mainPlayerData == null) return -1;
        return this.mainPlayerData.getId();
    }
}
