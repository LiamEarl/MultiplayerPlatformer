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
    private static final ClientHandler[] clientHandlers = new ClientHandler[10];
    private static Object[] updates = new Object[11];
    static int level = 0;
    static long lastCompletedLevel = System.currentTimeMillis();

    public static void main(String[] args) {

        ArrayList<Color> playerColorCodes = new ArrayList<>(Arrays.asList(
                new Color(230, 116, 255),
                new Color(255, 255, 255),
                new Color(0, 255, 0),
                new Color(0, 0, 255),
                new Color(255, 125, 0),
                new Color(100, 57, 39),
                new Color(255, 255, 0),
                new Color(0, 125, 255),
                new Color(203, 0, 203),
                new Color(1, 196, 196)
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
                new Vector2D(90, 27.7),
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
                    if(client == null) continue;
                    if(!client.getConnected()) {
                        System.out.println("Main server: Closed Client " + client.getPlayerData().getId());
                        clientHandlers[client.getPlayerData().getId()] = null;
                        continue;
                    }


                    Object clientUpdate = client.getClientUpdate();
                    if(clientUpdate == null) continue;
                    updates[client.getPlayerData().getId()] = clientUpdate;
                    client.wipeUpdate();
                }

                updates = nextLevelListener(updates);

                for(int i = 0; i < updates.length; i++) {
                    if(updates[i] != null) break;
                    if(i == clientHandlers.length - 1) continue mainLoop;
                }

                for(ClientHandler client : clientHandlers) {
                    if(client == null) continue;

                    Object[] modified = Arrays.copyOf(updates, updates.length);
                    // 10th index of updates is reserved for updating clients about the current Level, if it is not null then override client side player with server side player
                    if(updates[10] == null)
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

    private static Object[] nextLevelListener(Object[] updates) {
        Object[] output = updates.clone();

        if(System.currentTimeMillis() - lastCompletedLevel < 5000) return output;

        boolean hasClients = false;

        for(ClientHandler client : GameServer.clientHandlers) {
            if(client != null) {
                hasClients = true;
                break;
            }
        }

        if(!hasClients) return output;

        for(ClientHandler client : GameServer.clientHandlers) {
            if(client == null) continue;
            if(!client.getPlayerData().getGodMode()) return output;
        }

        for(ClientHandler client : GameServer.clientHandlers) {
            if(client == null) continue;
            Player player = client.getPlayerData();
            player.setGodMode(false);
            output[player.getId()] = player;
        }

        level ++;
        lastCompletedLevel = System.currentTimeMillis();
        output[10] = new Message("CurrentLevel;" + level);
        return output;
    }
}

class AcceptIncomingClients implements Runnable {
    private final ServerSocket serverSocket;
    private final ClientHandler[] handlers;
    private final ArrayList<Vector2D> dimensions;
    private final ArrayList<Color> playerColorCodes;
    AcceptIncomingClients(ServerSocket socket, ClientHandler[] handlers, ArrayList<Vector2D> dimensions, ArrayList<Color> playerColorCodes) {
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

                int id = -1;
                for(int i = 0; i < handlers.length; i++) {
                    if(this.handlers[i] == null) {
                        id = i;
                        break;
                    }
                }

                if(id == -1) {
                    clientSocket.close();
                    System.out.println("At client capacity, refused connection.");
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(clientSocket, id, this.dimensions, this.playerColorCodes);
                this.handlers[id] = clientHandler;

                System.out.println("New Client Connected: Assigning Id " + id + " Address: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}