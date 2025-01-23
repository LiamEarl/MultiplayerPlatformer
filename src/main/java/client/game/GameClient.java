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
            try {
                serverConnection = handleServerConnection(gameObjects);
            } catch (ConnectException e) {
                System.out.println("Failed To Connect To The Server");
            }

            gameObjects[9] = new Obstacle(-100, 800, 600, 200, new Color(14, 3, 46));
            gameObjects[10] = new Obstacle(0, 0, 50, 1000, new Color(14, 3, 46));
            gameObjects[11] = new Obstacle(650, 750, 100, 100, new Color(14, 3, 46));
            gameObjects[12] = new Obstacle(875, 700, 100, 100, new Color(14, 3, 46));

            Game game = null;

            while(true) {
                if(game == null && serverConnection.getPlayerId() != -1 && serverConnection != null)
                    game = new Game((Player) gameObjects[serverConnection.getPlayerId()], gameObjects);

                if(game == null) continue;

                game.renderScene();
                game.handleKeyInputs();
                ((Player) gameObjects[serverConnection.getPlayerId()]).update();
                game.checkPlayerCollisions();

                Thread.sleep(16);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
