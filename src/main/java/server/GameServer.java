package server;

import client.objects.Player;
import client.utility.Message;
import client.utility.Vector2D;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Main Server class responsible for starting up and managing several client connections
 */
public class GameServer {
    // Array of clientHandler objects that each manage a single client connection
    private static final ClientHandler[] clientHandlers = new ClientHandler[10];
    private static Object[] updates = new Object[11]; // Array containing recent updates received from a client
    static int level = 0; // Level index for all clients
    static long lastCompletedLevel = System.currentTimeMillis(); // Time by which the last level was completed

    private static final ArrayList<Color> playerColorCodes = new ArrayList<>(Arrays.asList(
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

    private static final ArrayList<Vector2D> dimensions = new ArrayList<>(Arrays.asList(
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

    public static void main(String[] args) {
        // Shuffle default player color codes and dimensions to make things interesting.
        Collections.shuffle(playerColorCodes);
        Collections.shuffle(dimensions);

        try {
            ServerSocket serverSocket = new ServerSocket(8888); // Create the server socket
            System.out.println("Server started on port: " + serverSocket.getLocalPort());

            // Create a new AcceptIncomingClients object that will operate on a new thread and add new ClientHandler objects
            // to the array when new clients connect
            AcceptIncomingClients incoming = new AcceptIncomingClients(serverSocket, clientHandlers); // Create the object
            Thread incomingClients = new Thread(incoming); // Create the thread & pass in the object
            incomingClients.start(); // Start the thread

            mainLoop:
            while (true) {
                Thread.sleep(1);

                for(ClientHandler client : clientHandlers) { // Loop through all clients
                    if(client == null) continue; // Client doesn't exist, continue to the next
                    if(!client.getConnected()) {  // The client is now closed, so remove it from the list of clients
                        System.out.println("Main server: Closed Client " + client.getPlayerData().getId());
                        clientHandlers[client.getPlayerData().getId()] = null;
                        continue;
                    }

                    Object clientUpdate = client.getClientUpdate(); // Gets the most recent update from a client
                    if(clientUpdate == null) continue; // There is no update, continue to the next client
                    updates[client.getPlayerData().getId()] = clientUpdate; // There is an update, add it to the updates array
                    client.wipeUpdate(); // The update is acknowledged, so wipe it.
                }

                // If this method detects that it should move on to a new level, it will include the new level to the update
                // being sent to every single client
                updates = nextLevelListener(updates);

                // Loop through all updates
                for(int i = 0; i < updates.length; i++) {
                    if(updates[i] != null) break; // Hey, here is a non-null update, we can send this to everyone
                    if(i == clientHandlers.length - 1) continue mainLoop; // No updates, continue the main loop
                }

                for(ClientHandler client : clientHandlers) { // Loop through all clients now that we have an update
                    if(client == null) continue; // Client does not exist, continue to the next

                    // creates an update array that does not include the current client, so that the client player is not
                    // overwritten by the server side corresponding player
                    Object[] modified = Arrays.copyOf(updates, updates.length);

                    // 10th index of updates is reserved for updating clients about the current Level,
                    // if it is not null then override client side player with server side player
                    if(updates[10] == null)
                        modified[client.getPlayerData().getId()] = null;

                    client.uploadToClient(modified); // send the updates to the client
                }

                Arrays.fill(updates, null); // wipe the updates so there are no duplicates
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object[] nextLevelListener(Object[] updates) {
        // If it has not been at least 5 seconds since the level has been updated we don't check if we should change it again.
        if(System.currentTimeMillis() - lastCompletedLevel < 5000) return updates;
        Object[] output = updates.clone();

        boolean hasClients = false; // Boolean to determine if any clients are connected

        for(ClientHandler client : clientHandlers) { // Loop through clients to tell if any are active
            if(client != null) {
                hasClients = true;
                break;
            }
        }

        if(!hasClients) return output; // No clients? Then no new level.

        for(ClientHandler client : clientHandlers) { // Loop through clients to check if everyone is in spectate mode
            if(client == null) continue;
            if(!client.getPlayerData().getGodMode()) return output; // Someone is not done with the level
        }

        // Loop through clientHandlers to set everyone out of spectate mode
        for(ClientHandler client : clientHandlers) {
            if(client == null) continue;
            Player player = client.getPlayerData();
            player.setGodMode(false);
            output[player.getId()] = player;
        }

        level ++; // Increment level
        lastCompletedLevel = System.currentTimeMillis(); // Change when the level was last completed
        output[10] = new Message("CurrentLevel;" + level); // Send a message saying that the level is being updated
        return output;
    }
    static ArrayList<Color> getPlayerColorCodes() {
        return playerColorCodes;
    }
    static ArrayList<Vector2D> getPlayerDimensions() {
        return dimensions;
    }
}

/**
 * Utility class responsible for accepting new clients, creating + starting a handler object and adding it to the array
 * of handlers.
 */
class AcceptIncomingClients implements Runnable {
    private final ServerSocket serverSocket; // ServerSocket for use in connecting clients
    private final ClientHandler[] handlers;

    /**
     * Creates a new AcceptIncomingClients Object. Must be passed into a new thread.
     * @param socket Server socket used to connect with clients
     * @param handlers ClientHandler array that this object will add to
     */
    AcceptIncomingClients(ServerSocket socket, ClientHandler[] handlers) {
        this.serverSocket = socket;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Socket clientSocket = this.serverSocket.accept(); // Wait for a new client to accept a connection.

                // New prospective client, so try and find an empty spot to slot it into.
                int id = -1;
                for(int i = 0; i < handlers.length; i++) {
                    if(this.handlers[i] == null) { // Empty slot at index i
                        id = i;
                        break;
                    }
                }

                if(id == -1) { // This means that no slots are open. We can't add another client.
                    clientSocket.close();
                    System.out.println("At client capacity, refused prospective connection.");
                    continue;
                }

                // Create a new ClientHandler for managing the new client
                ClientHandler clientHandler = new ClientHandler(clientSocket, id);
                this.handlers[id] = clientHandler; // Pass that handler in to the array

                System.out.println("New Client Connected: Assigning Id " + id + " Address: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(clientHandler); // Create a new thread so that it runs independently
                clientThread.start(); // Start that thread up
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}