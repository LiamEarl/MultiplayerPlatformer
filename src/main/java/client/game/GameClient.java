package client.game;
import client.model.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class GameClient {
    private static final float ASSUMED_UPDATE_TIME = 16;

    private static ServerHandler handleServerConnection(GameObject[] gameObjects) throws IOException {
        Socket serverSocket = new Socket("localhost", 8888); // Server IP and port the-tower.net
        System.out.println("Server Connected At IP: " + serverSocket.getRemoteSocketAddress());
        ServerHandler serverHandler = new ServerHandler(serverSocket, gameObjects);
        Thread serverThread = new Thread(serverHandler);
        serverThread.start();
        return serverHandler;
    }

    public static void main(String[] args) {
        try {
            GameObject[] gameObjects = new GameObject[100];
            ServerHandler serverConnection = null;
            Game game = null;
            int currentTick = 0;

            long lastUpdate = System.currentTimeMillis();
            long sendToServerTimer = System.currentTimeMillis();

            while(true) {
                long dt = System.currentTimeMillis() - lastUpdate;
                float dtMod = dt / ASSUMED_UPDATE_TIME;
                lastUpdate = System.currentTimeMillis();
                currentTick ++;

                if(serverConnection == null) {
                    try {
                        serverConnection = handleServerConnection(gameObjects);
                    } catch (ConnectException e) {
                        if(currentTick % 60 == 0)
                            System.out.println("Failed To Connect To The Server. Listening For A Connection.");
                    }
                }

                if(game == null) {
                    if(serverConnection == null) continue;
                    if (serverConnection.getPlayerId() != -1) {
                        System.out.println("ATTEMPTING GAME CREATION");
                        Player mainPlayer = (Player) gameObjects[serverConnection.getPlayerId()];
                        game = new Game(mainPlayer, gameObjects);
                    }
                    continue;
                }

                game.handleKeyInputs(dtMod);
                game.updateGameObjects(dtMod);
                game.checkPlayerCollisions(dtMod);
                game.renderScene();

                sendToServerTimer = serverConnection.handleOutgoingUpdates(sendToServerTimer);

                Thread.sleep(2);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
