package client.game;
import client.model.*;

import java.io.*;
import java.net.Socket;

public class GameClient {
    private static final float ASSUMED_UPDATE_TIME = 16;

    private static ServerHandler handleServerConnection(GameObject[] gameObjects, String ip, int port) throws IOException {
        Socket serverSocket = new Socket(ip, port); // Server IP and port the-tower.net
        System.out.println("Server Connected At IP: " + serverSocket.getRemoteSocketAddress());
        ServerHandler serverHandler = new ServerHandler(serverSocket, gameObjects);
        Thread serverThread = new Thread(serverHandler);
        serverThread.start();
        return serverHandler;
    }

    public static void main(String[] args) {
        try {
            GameObject[] gameObjects = new GameObject[200];
            ServerHandler serverConnection = null;
            Game game = new Game(gameObjects);
            int currentTick = 0;

            long lastUpdate = System.currentTimeMillis();
            long sendToServerTimer = System.currentTimeMillis();
            float fps;

            boolean previouslyConnected = false;

            while(true) {
                long dt = System.currentTimeMillis() - lastUpdate;
                float dtMod = dt / ASSUMED_UPDATE_TIME;
                fps = (float) 1 / dt * 1000;
                lastUpdate = System.currentTimeMillis();
                currentTick ++;

                if(serverConnection == null) {

                    try {
                        serverConnection = handleServerConnection(gameObjects, game.getIP(), game.getPort());
                    } catch (Exception e) {
                        if (currentTick % 60 == 0) {
                            System.out.println("Failed To Connect To The Server. Listening For A Connection.");
                        }
                    }

                    if(previouslyConnected) {
                        game.handleKeyInputs(dtMod);
                        game.updateGameObjects(dtMod, System.currentTimeMillis());
                        game.checkPlayerCollisions(dtMod);
                    }

                    game.renderScene(fps);

                    Thread.sleep(16);
                }else {
                    previouslyConnected = true;

                    if(!serverConnection.getConnected()) {
                        serverConnection = null;
                        continue;
                    }

                    if(!game.initializedPlayer()) {
                        if(serverConnection.getPlayerId() != -1) {
                            Player mainPlayer = (Player) gameObjects[serverConnection.getPlayerId()];
                            game.setGameObjects(mainPlayer);
                        }

                        Thread.sleep(16);
                    }else {
                        game.handleKeyInputs(dtMod);
                        game.updateGameObjects(dtMod, serverConnection.getServerTime());
                        game.checkPlayerCollisions(dtMod);
                        game.renderScene(fps);
                        sendToServerTimer = serverConnection.handleOutgoingUpdates(sendToServerTimer);

                        Thread.sleep(1);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
