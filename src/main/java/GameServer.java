import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server started on port: " + serverSocket.getLocalPort());
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                System.out.println("New Client Connected: " + clientSocket.getInetAddress());
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}