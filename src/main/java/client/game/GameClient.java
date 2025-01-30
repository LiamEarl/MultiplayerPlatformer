package client.game;
import client.model.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class GameClient {

    private static ServerHandler handleServerConnection(GameObject[] gameObjects) throws IOException {
        Socket serverSocket = new Socket("localhost", 8888); // Server IP and port
        System.out.println("Server Connected At IP: " + serverSocket.getLocalSocketAddress());
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

            //for(float i = 0; i < 6.28; i+= 0.005) {
            //    System.out.println(MathParser.pieceWiseFast(i));            }

            while(true) {
                Thread.sleep(16);
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
                        game = new Game((Player) gameObjects[serverConnection.getPlayerId()], gameObjects);
                    }
                    continue;
                }

                game.renderScene();
                game.handleKeyInputs();
                game.updateGameObjects();
                game.checkPlayerCollisions();

                if(serverConnection.getPlayerId() != -1) {
                    if (gameObjects[serverConnection.getPlayerId()].getVelocity().length() > 0.5f || currentTick % 10 == 0) {
                        serverConnection.writeToServer();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
