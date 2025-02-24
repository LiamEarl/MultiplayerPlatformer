package client.utility;

import java.io.Serializable;

/**
 * Message class for general use, I added this in case I want to add anything besides a String to a message, but for
 * Now it doesn't do anything but hold a string unfortunately
 */
public class Message implements Serializable {
    private String message;
    public Message(String message) {this.message = message;}
    public String getMessage() {return this.message;}
}
