package client.utility;

import java.io.Serializable;

/**
 * Ping class for now just used to determine the latency between the client and the server.
 * Like the Message class I made a separate class in case I wanted to add more to a ping
 * instead of just using a serialized long, but for now it doesn't do much.
 */
public class Ping implements Serializable {
    private long timeSent;
    public Ping() {this.timeSent = System.currentTimeMillis();}
    public long getTimeSent() {return this.timeSent;}
}
