package client.game;
import client.model.GameObject;
import client.model.Player;
import client.model.Vector2D;
import server.Message;
import server.Ping;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

class ServerHandler implements Runnable {
    private Socket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private GameObject[] gameObjects;
    private Player mainPlayer;
    private Vector2D lastVelocity;
    private long serverOffset = 0;

    ServerHandler(Socket serverSocket, GameObject[] gameObjects) throws IOException {
        this.serverSocket = serverSocket;
        this.out = new ObjectOutputStream(serverSocket.getOutputStream());
        this.in = new ObjectInputStream(serverSocket.getInputStream());
        this.gameObjects = gameObjects;
        this.lastVelocity = new Vector2D(0, 0);
    }

    @Override
    public void run() {
        try {
            while (this.serverSocket.isConnected()) {
                try {
                    Object fromServerObject = this.in.readObject();
                    if(fromServerObject == null) continue;
                    if(fromServerObject instanceof Player[]) {
                        Player[] fromServer = (Player[]) fromServerObject;
                        for(Player playerUpdate : fromServer) {
                            if(playerUpdate == null) continue;
                            updatePlayers(playerUpdate);
                        }
                    }else if(fromServerObject instanceof Ping) {
                        writeToServer(fromServerObject);
                    }else if(fromServerObject instanceof Message) {
                        String[] message = ((Message) fromServerObject).getMessage().split(";");
                        if(message[0].equals("SyncServer")) {
                            this.serverOffset = System.currentTimeMillis() - Long.parseLong(message[1]);
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

    void updatePlayers(Player playerUpdate) {
        if(playerUpdate == null) return;

        int id = playerUpdate.getId();

        if (gameObjects[id] == null) {
            this.gameObjects[id] = playerUpdate;
            if(this.mainPlayer == null) this.mainPlayer = playerUpdate;
            System.out.println("INITIALIZED PLAYER " + playerUpdate.getId());
        } else {
            Player ghost = (Player) gameObjects[id];
            ghost.setPos(playerUpdate.getPos());
            ghost.setVel(playerUpdate.getVelocity());
        }
    }

    void writeToServer(Object toWrite) throws IOException {
        this.out.reset();
        this.out.writeObject(toWrite);
        this.out.flush();
    }

    long handleOutgoingUpdates(long sendToServerTimer) throws IOException {
        if(this.mainPlayer != null) {
            long diff = System.currentTimeMillis() - sendToServerTimer;
            Vector2D velocityDiff = new Vector2D(mainPlayer.getVelocity().getX() - this.lastVelocity.getX(), mainPlayer.getVelocity().getY() - this.lastVelocity.getY());
            float magnitude = velocityDiff.length();
            if ((magnitude > 0.1f && diff > 50) || diff > 1000 || magnitude > 5) {
                writeToServer(this.mainPlayer);
                this.lastVelocity = this.mainPlayer.getVelocity().copy();
                return System.currentTimeMillis();
            }
        }
        return sendToServerTimer;
    }

    long getServerTime() {
        return System.currentTimeMillis() + serverOffset;
    }

    int getPlayerId() {
        if(this.mainPlayer == null) return -1;
        return this.mainPlayer.getId();
    }
}
