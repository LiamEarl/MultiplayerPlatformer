package Client.Game;
import Server.NetworkCommunicator;

import java.io.*;
import java.net.Socket;

class ServerHandler extends NetworkCommunicator implements Runnable {
    private Socket serverSocket;
    private InputStream in;
    private OutputStream out;

    ServerHandler(Socket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        this.in = serverSocket.getInputStream();
        this.out = serverSocket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[2000];
            String fromServer;

            while (this.serverSocket.isConnected()) {
                uploadToServer("Yello: :)|");
                fromServer = super.receiveInput(buffer, this.in);

                if(!fromServer.isEmpty()) {
                    System.out.println("Message Received From Server: " + fromServer);
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


}
