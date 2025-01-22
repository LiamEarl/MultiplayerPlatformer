package Client.Game;
import Client.PhysicalObjects.Obstacle;
import Client.PhysicalObjects.Player;
import Client.PhysicalObjects.Renderable;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class GameClient {

    private static void handleServerConnection() throws IOException {
        Socket serverSocket = new Socket("localhost", 8888); // Server IP and port
        System.out.println("Server Connected At IP: " + serverSocket.getLocalSocketAddress());
        ServerHandler serverHandler = new ServerHandler(serverSocket);
        Thread serverThread = new Thread(serverHandler);
        serverThread.start();
    }

    public static void main(String[] args) {
        try {
            handleServerConnection();
            Player player = new Player(200, 100, new Color(255, 0, 0));
            ArrayList<Renderable> toRender = new ArrayList<Renderable>();
            Obstacle testObstacle = new Obstacle(500, 600, 300, 20, new Color(0, 255, 0));

            toRender.add(player);
            toRender.add(testObstacle);

            Game game = new Game(player, toRender);

            while(true) {
                game.renderScene();
                game.handleKeyInputs();
                player.update();
                Thread.sleep(16);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
