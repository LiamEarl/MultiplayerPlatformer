package server;

import client.model.Player;
import client.model.Vector2D;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameServer {
    public static void main(String[] args) {
        ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

        Object[] updates = new Player[10];

        ArrayList<Color> playerColorCodes = new ArrayList<>(Arrays.asList(
                new Color(230, 116, 255),
                new Color(255, 255, 255),
                new Color(0, 255, 0),
                new Color(0, 0, 255),
                new Color(255, 125, 0),
                new Color(125, 255, 0),
                new Color(255, 255, 0),
                new Color(0, 125, 255),
                new Color(255, 0, 255),
                new Color(0, 255, 255)
        ));

        ArrayList<Vector2D> dimensions = new ArrayList<>(Arrays.asList(
                new Vector2D(50, 50),
                new Vector2D(40, 62.5f),
                new Vector2D(62.5f, 40),
                new Vector2D(45, 55),
                new Vector2D(55, 45),
                new Vector2D(33.33f, 75f),
                new Vector2D(83.33f, 30),
                new Vector2D(27.7f, 90),
                new Vector2D(100, 25),
                new Vector2D(27.7f, 90)
        ));

        Collections.shuffle(playerColorCodes);
        Collections.shuffle(dimensions);

        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(8888));//192.168.12.113

            System.out.println("Server started on port: " + serverSocket.getLocalPort());

            AcceptIncomingClients incoming = new AcceptIncomingClients(serverSocket, clientHandlers, dimensions, playerColorCodes);
            Thread incomingClients = new Thread(incoming);
            incomingClients.start();

            mainLoop:
            while (true) {
                Thread.sleep(1);

                for(ClientHandler client : clientHandlers) {
                    Object clientUpdate = client.getClientUpdate();
                    if(clientUpdate == null) continue;
                    updates[client.getPlayerData().getId()] = clientUpdate;
                    client.wipeUpdate();
                }

                for(int i = 0; i < updates.length; i++) {
                    if(updates[i] != null) break;
                    if(i == clientHandlers.size() - 1) continue mainLoop;
                }

                for(ClientHandler client : clientHandlers) {
                    Object[] modified = Arrays.copyOf(updates, updates.length);
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
    private ArrayList<Vector2D> dimensions;
    private ArrayList<Color> playerColorCodes;
    AcceptIncomingClients(ServerSocket socket, ArrayList<ClientHandler> handlers, ArrayList<Vector2D> dimensions, ArrayList<Color> playerColorCodes) {
        this.serverSocket = socket;
        this.handlers = handlers;
        this.dimensions = dimensions;
        this.playerColorCodes = playerColorCodes;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Socket clientSocket = this.serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, handlers.size(), this.dimensions, this.playerColorCodes);
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