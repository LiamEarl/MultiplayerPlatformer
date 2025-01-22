package Client;
import java.io.*;
import java.net.*;

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
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}