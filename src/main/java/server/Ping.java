package server;

public class Ping {
    private long timeSent;
    public Ping(long timeSent) {
        this.timeSent = timeSent;
    }
    long getTimeSent() {return this.timeSent;}
}
