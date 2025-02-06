package server;

import java.io.Serializable;

public class Ping implements Serializable {
    private long timeSent;
    public Ping() {this.timeSent = System.currentTimeMillis();}
    long getTimeSent() {return this.timeSent;}
}
