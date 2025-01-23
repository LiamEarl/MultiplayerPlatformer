package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer {
    public static void main(String[] args) {
        ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server started on port: " + serverSocket.getLocalPort());

            AcceptIncomingClients incoming = new AcceptIncomingClients(serverSocket, clientHandlers);
            Thread incomingClients = new Thread(incoming);
            incomingClients.start();

            while (true) {
                for(ClientHandler client : clientHandlers) {
                    for(ClientHandler toSend: clientHandlers) {
                        if(client == toSend) continue;
                        toSend.uploadToClient(
                                "PD."
                                        + client.getClientId() + "."
                                        + (int)client.getPlayerLocation().getX() + "."
                                        + (int)client.getPlayerLocation().getY() + "."
                                        + (int)client.getPlayerDimensions().getX() + "."
                                        + (int)client.getPlayerDimensions().getY() + "|");
                    }
                }
                Thread.sleep(16);
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

class AcceptIncomingClients implements Runnable {
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> handlers;
    AcceptIncomingClients(ServerSocket socket, ArrayList<ClientHandler> handlers) {
        this.serverSocket = socket;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Socket clientSocket = this.serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, handlers.size());
                handlers.add(clientHandler);

                System.out.println("New Client Connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}