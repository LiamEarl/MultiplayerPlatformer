package Client.Game;
import Client.PhysicalObjects.GameObject;
import Client.PhysicalObjects.Player;
import Server.NetworkCommunicator;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

class ServerHandler extends NetworkCommunicator implements Runnable {
    private Socket serverSocket;
    private InputStream in;
    private OutputStream out;
    private GameObject[] gameObjects;
    private int playerId;


    ServerHandler(Socket serverSocket, GameObject[] gameObjects) throws IOException {
        this.serverSocket = serverSocket;
        this.in = serverSocket.getInputStream();
        this.out = serverSocket.getOutputStream();
        this.gameObjects = gameObjects;
        this.playerId = -1;
    }

    @Override
    public void run() {
        try {
            Color[] playerColorCodes = {new Color(255, 0, 0), new Color(0, 255, 0), new Color(255, 125, 0), new Color(0, 125, 255), new Color(125, 255, 0)};

            byte[] buffer = new byte[2000];
            String fromServer;

            while (this.serverSocket.isConnected()) {

                if(this.playerId != -1) {
                    GameObject p = this.gameObjects[this.playerId];
                    uploadToServer("PD." + (int)p.getPos().getX() + "." + (int)p.getPos().getY() + "." + (int)p.getDim().getX() + "." + (int)p.getDim().getY() + "|");
                }


                fromServer = super.receiveInput(buffer, this.in);


                if (!fromServer.isEmpty()) {
                    //System.out.println(fromServer);
                    String[] command = fromServer.split("[.]");

                    if (command[0].equals("PD")) {

                        int id = Integer.parseInt(command[1]);

                        if (this.gameObjects[id] == null)
                            this.gameObjects[id] = new Player(100, 700, playerColorCodes[id]);

                        this.gameObjects[id].getPos().setXY(Float.parseFloat(command[2]), Float.parseFloat(command[3]));
                        this.gameObjects[id].getDim().setXY(Float.parseFloat(command[4]), Float.parseFloat(command[5]));
                    } else if (command[0].equals("CID")) {
                        this.playerId = Integer.parseInt(command[1]);
                        this.gameObjects[this.playerId] = new Player(100, 700, playerColorCodes[this.playerId]);
                        System.out.println("INITIALIZED PLAYER");
                    }


                    buffer = new byte[2000];
                }

                Thread.sleep(16);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void uploadToServer(String toUpload) throws IOException {
        super.uploadToServer(toUpload, this.out);
    }

    int getPlayerId() {
        return this.playerId;
    }
}
