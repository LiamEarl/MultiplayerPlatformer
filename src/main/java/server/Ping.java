package server;

import java.io.Serializable;

public class Ping implements Serializable {
    private long timeSent;
    public Ping(long timeSent) {
        this.timeSent = timeSent;
    }
    long getTimeSent() {return this.timeSent;}
}
