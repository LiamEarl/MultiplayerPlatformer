package client.game;
import client.model.Obstacle;
import client.model.Player;
import client.model.GameObject;

import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

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
            try {
                serverConnection = handleServerConnection(gameObjects);
            } catch (ConnectException e) {
                System.out.println("Failed To Connect To The Server");
            }

            Color wallColor = new Color(14, 3, 46);
            gameObjects[9] = new Obstacle(-100, 800, 600, 1000, wallColor);
            gameObjects[10] = new Obstacle(-1000, -500, 1050, 4000, wallColor);
            gameObjects[11] = new Obstacle(900, 750, 100, 100, wallColor);
            gameObjects[12] = new Obstacle(1350, 700, 100, 100, wallColor);

            Game game = null;

            while(true) {
                Thread.sleep(16);

                //System.out.println(1);
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
                ((Player) gameObjects[serverConnection.getPlayerId()]).update();
                game.checkPlayerCollisions();

                if(serverConnection.getPlayerId() != -1) {
                    if (((Player) gameObjects[serverConnection.getPlayerId()]).getVel().length() > 0.2f) {
                        serverConnection.writeToServer();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
