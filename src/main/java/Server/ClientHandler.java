package Server;

import java.io.*;
import java.net.Socket;

class ClientHandler extends NetworkCommunicator implements Runnable {
    private Socket clientSocket;
    private InputStream in;
    private OutputStream out;
    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.in = clientSocket.getInputStream();
        this.out = clientSocket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[2000];
            String fromClient;

            while (this.clientSocket.isConnected()) {
                uploadToClient("You are a basic client. I have absolute authority over you.|");

                fromClient = super.receiveInput(buffer, this.in); // Get Messages From

                if(!fromClient.isEmpty()) {
                    //Game Logic
                    System.out.println("Message Received From Client: " + fromClient);
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
    void uploadToClient(String toUpload) throws IOException {
        super.uploadToServer(toUpload, this.out);
    }
}
