import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static java.lang.System.currentTimeMillis;

public class ClientHandler implements Runnable {
    private Socket clientConnection;
    private boolean isConnected;
    private long lastMessage;

    public ClientHandler(Socket clientConnection) {
        this.clientConnection = clientConnection;
        this.lastMessage = currentTimeMillis();
        this.isConnected = true;
    }

    @Override
    public void run() {
        try {
            InputStream in = clientConnection.getInputStream();
            OutputStream out = clientConnection.getOutputStream();

            byte[] buffer = new byte[2000];
            String message;

            while (clientConnection.isConnected()) {
                byte[] bytesToWrite = "You are a basic client. I have absolute authority over you.".getBytes();

                // Write individual bytes
                for (byte b : bytesToWrite) {
                    out.write(b);
                }

                message = receiveInput(in, buffer); // Get Messages From

                if(!message.isEmpty()) {
                    //Game Logic
                    System.out.println(message.length());
                    System.out.println(message);
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
    private String receiveInput(InputStream in, byte[] buffer) throws IOException {
        for(int i = 0; i < buffer.length; i++) {
            int thisByte = in.read();
            if(thisByte == 124) {
                return new String(buffer, StandardCharsets.UTF_8).replace("\u0000", "");
            }

            if (thisByte == -1) break;
            buffer[i] = (byte) thisByte;
        }
        return "";
    }

}
