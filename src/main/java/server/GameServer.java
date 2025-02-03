package server;

import client.model.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class GameServer {
    public static void main(String[] args) {
        ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

        Player[] updates = new Player[10];

        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(8888));//192.168.12.113

            System.out.println("Server started on port: " + serverSocket.getLocalPort());

            AcceptIncomingClients incoming = new AcceptIncomingClients(serverSocket, clientHandlers);
            Thread incomingClients = new Thread(incoming);
            incomingClients.start();

            mainLoop:
            while (true) {
                Thread.sleep(4);

                for(ClientHandler client : clientHandlers) {
                    Player playerUpdate = client.getClientUpdate();
                    if(playerUpdate == null) continue;
                    updates[client.getPlayerData().getId()] = playerUpdate;
                    client.wipeUpdate();
                }

                for(int i = 0; i < updates.length; i++) {
                    if(updates[i] != null) break;
                    if(i == clientHandlers.size() - 1) continue mainLoop;
                }
                System.out.println("recieving update from client" + System.currentTimeMillis());

                for(ClientHandler client : clientHandlers) {
                    Player[] modified = Arrays.copyOf(updates, updates.length);
                    modified[client.getPlayerData().getId()] = null;
                    client.uploadToClient(modified);
                }

                Arrays.fill(updates, null);
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