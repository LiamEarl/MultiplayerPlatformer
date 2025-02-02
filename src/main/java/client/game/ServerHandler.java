package client.game;
import client.model.GameObject;
import client.model.Player;
import client.model.EntityData;
import client.model.Vector2D;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Vector;

class ServerHandler implements Runnable {
    private Socket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private GameObject[] gameObjects;
    private EntityData mainPlayerData;

    private final Color[] playerColorCodes = {new Color(255, 0, 0), new Color(0, 255, 0), new Color(255, 125, 0), new Color(0, 125, 255), new Color(125, 255, 0)};
    private final Vector2D[] dimensions = {new Vector2D(41.5f, 60), new Vector2D(60, 41.5f),  new Vector2D(80, 31.25f), new Vector2D(50, 50), new Vector2D(31.25f, 80)};

    private long[] updateTimes = new long[10];
    private Vector2D[] lastPositions = new Vector2D[10];


    ServerHandler(Socket serverSocket, GameObject[] gameObjects) throws IOException {
        this.serverSocket = serverSocket;
        this.out = new ObjectOutputStream(serverSocket.getOutputStream());
        this.in = new ObjectInputStream(serverSocket.getInputStream());
        this.gameObjects = gameObjects;
    }

    @Override
    public void run() {
        try {
            while (this.serverSocket.isConnected()) {
                try {
                    Object fromServerObject = this.in.readObject();
                    if(fromServerObject == null) continue;

                    if (fromServerObject instanceof EntityData) {
                        EntityData fromServer = (EntityData) fromServerObject;
                        if(this.mainPlayerData == null) {
                            this.mainPlayerData = fromServer;
                            this.gameObjects[this.mainPlayerData.getId()] = new Player(this.mainPlayerData, playerColorCodes[this.mainPlayerData.getId()], dimensions[this.mainPlayerData.getId()]);
                            System.out.println("INITIALIZED PLAYER " + fromServer.getId());
                        }else {
                            updatePlayers(fromServer);
                        }
                    }else if(fromServerObject instanceof EntityData[]) {
                        EntityData[] fromServer = (EntityData[]) fromServerObject;
                        for(EntityData data : fromServer) {
                            if(data == null) continue;
                            updatePlayers(data);
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

    void updatePlayers(EntityData data) {
        int id = data.getId();

        if (gameObjects[id] == null) {
            this.gameObjects[id] = new Player(data, playerColorCodes[id], dimensions[id]);
            System.out.println("INITIALIZED PLAYER " + data.getId());
        } else {
            Player ghost = (Player) gameObjects[id];
            long timeDifference = System.currentTimeMillis() - updateTimes[id];

            Vector2D velocity = data.getPos().copy();
            velocity.subtract(lastPositions[id]);
            System.out.println("CurrentPosition + " + data.getPos() + " OldPosition: " + lastPositions[id] + " dt: " + timeDifference + " Change: " + velocity);
            velocity.divide((float) timeDifference);
            velocity.scale(16f); // Whatever the sleep method contains in the main client thread
            ghost.setPlayerData(data);
            ghost.setVel(velocity);
        }

        lastPositions[id] = new Vector2D(data.getPos().copy());
        updateTimes[id] = System.currentTimeMillis();

    }

    void writeToServer() throws IOException {
        this.out.reset();
        EntityData stuff = new EntityData(new Vector2D(this.mainPlayerData.getPos().getX(), this.mainPlayerData.getPos().getY()), this.mainPlayerData.getId());
        this.out.writeObject(stuff);
        this.out.flush();
    }

    int getPlayerId() {
        if(this.mainPlayerData == null) return -1;
        return this.mainPlayerData.getId();
    }
}
