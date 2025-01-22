package Server;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class NetworkCommunicator {
    protected void uploadToServer(String toUpload, OutputStream out) throws IOException {
        byte[] bytesToWrite = toUpload.getBytes();
        // Write individual bytes
        for (byte b : bytesToWrite) {
            out.write(b);
        }
    }

    protected String receiveInput(byte[] buffer, InputStream in) throws IOException {
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
