package server;

import client.model.EntityData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer {
    public static void main(String[] args) {
        ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("192.168.12.113", 8888));

            System.out.println("Server started on port: " + serverSocket.getLocalPort());

            AcceptIncomingClients incoming = new AcceptIncomingClients(serverSocket, clientHandlers);
            Thread incomingClients = new Thread(incoming);
            incomingClients.start();

            while (true) {
                //System.out.println("Updating");
                for(ClientHandler client : clientHandlers) {

                    for(EntityData update : client.getUpdates()) {

                        for (ClientHandler toSend : clientHandlers) {
                            if (client == toSend) continue;
                            toSend.uploadToClient(update);
                        }
                    }
                    client.wipeUpdates();
                }
                Thread.sleep(5);
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