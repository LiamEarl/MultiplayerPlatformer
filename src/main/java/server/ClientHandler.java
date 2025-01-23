package server;

import client.model.Vector2D;

import java.io.*;
import java.net.Socket;

class ClientHandler extends NetworkCommunicator implements Runnable {
    private Socket clientSocket;
    private int clientId;
    private InputStream in;
    private OutputStream out;
    private Vector2D playerLocation;
    private Vector2D playerDimensions;

    public ClientHandler(Socket clientSocket, int clientId) throws IOException {
        this.clientSocket = clientSocket;
        this.in = clientSocket.getInputStream();
        this.out = clientSocket.getOutputStream();
        this.clientId = clientId;
        uploadToClient("CID." + clientId + "|");
        this.playerLocation = new Vector2D(-1000, -1000);
        this.playerDimensions = new Vector2D(0, 0);
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[2000];
            String fromClient;

            while (this.clientSocket.isConnected()) {
                //uploadToClient("You are a basic client. I have absolute authority over you.|");

                fromClient = super.receiveInput(buffer, this.in); // Get Messages From

                if(!fromClient.isEmpty()) {
                    //System.out.println(fromClient);
                    String[] command = fromClient.split("[.]");

                    if(command[0].equals("PD")) {
                        this.playerLocation.setXY(Float.parseFloat(command[1]), Float.parseFloat(command[2]));
                        this.playerDimensions.setXY(Float.parseFloat(command[3]), Float.parseFloat(command[4]));
                    }

                    buffer = new byte[2000];
                }
                Thread.sleep(16);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    int getClientId() {
        return this.clientId;
    }

    void uploadToClient(String toUpload) throws IOException {
        super.uploadToServer(toUpload, this.out);
    }
    Vector2D getPlayerLocation() {
        return this.playerLocation;
    }
    Vector2D getPlayerDimensions() {
        return this.playerDimensions;
    }
}
