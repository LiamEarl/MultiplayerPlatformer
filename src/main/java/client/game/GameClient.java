package client.game;
import client.model.Obstacle;
import client.model.Player;
import client.model.GameObject;

import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

import static java.lang.System.currentTimeMillis;

public class GameClient {

    private static ServerHandler handleServerConnection(GameObject[] gameObjects) throws IOException {
        Socket serverSocket = new Socket("192.168.12.113", 8888); // Server IP and port
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

            final float dt = 0.5f;
            int currentTick = -1;

            Color drabWallColor = new Color(30, 20, 90);
            Color brightWallColor = new Color(255, 213, 0);
            gameObjects[9]  = new Obstacle(-100, 800, 600, 1000, drabWallColor);
            gameObjects[10] = new Obstacle(-1000, -500, 1050, 4000, drabWallColor);
            gameObjects[11] = new Obstacle(700, 700, 310, 4000, drabWallColor);
            gameObjects[12] = new Obstacle(1700, 700, 575, 4000, drabWallColor);
            gameObjects[13] = new Obstacle(1000, 400, 10, 4000, drabWallColor);
            gameObjects[14] = new Obstacle(990, 550, 10, 10, drabWallColor);
            gameObjects[15] = new Obstacle(2700, 600, 5, 5, brightWallColor);
            gameObjects[16] = new Obstacle(3250, 525, 5, 5, brightWallColor);
            gameObjects[17] = new Obstacle(3700, 525, 500, 3000, drabWallColor);
            gameObjects[18] = new Obstacle(4190, -700, 10, 3000, drabWallColor);
            gameObjects[19] = new Obstacle(3700, -700, 10, 1000, drabWallColor);
            gameObjects[20] = new Obstacle(3925, 425, 50, 100, drabWallColor);
            gameObjects[21] = new Obstacle(3710, 290, 10, 10, drabWallColor);

            while(true) {
                Thread.sleep(16);

                currentTick ++;

                if(serverConnection == null) {
                    try {
                        serverConnection = handleServerConnection(gameObjects);
                    } catch (ConnectException e) {
                        System.out.println("Failed To Connect To The Server. Listening For A Connection.");
                    }
                }

                if(game == null) {
                    if(serverConnection == null) continue;

                    if (serverConnection.getPlayerId() != -1) {
                        //System.out.println("ATTEMPTING GAME CREATION");
                        game = new Game((Player) gameObjects[serverConnection.getPlayerId()], gameObjects);
                    }
                    continue;
                }

                game.renderScene();
                game.handleKeyInputs();

                ((Player) gameObjects[serverConnection.getPlayerId()]).update();
                game.checkPlayerCollisions();

                if(serverConnection.getPlayerId() != -1) {
                    if (((Player) gameObjects[serverConnection.getPlayerId()]).getVel().length() > 0.01f || currentTick % 20 == 0) {
                        serverConnection.writeToServer();
                    }
                }


            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
