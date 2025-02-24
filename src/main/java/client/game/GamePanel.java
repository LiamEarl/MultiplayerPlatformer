package client.game;
import client.objects.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import static java.lang.Math.round;
import client.utility.Vector2D;
import server.Message;

/**
 * GamePanel is the GUI for rendering the game scene, handling user input,
 * and managing the player's movement and messages. It extends JPanel and implements multiple
 * event listeners for keyboard and mouse interactions.
 */

public class GamePanel extends JPanel implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {
    // Client-side player. This is the player that you control vs the other players received from the server.
    private Player clientPlayer;

    // Boolean Array containing whether a certain KeyCode is being pressed at any given moment. Used for smooth movement.
    private final boolean[] keyPressed = new boolean[256];

    // Fixed-size array containing the GameObjects to be rendered on the window.
    private final GameObject[] gameObjects;

    // How much you are zoomed into the scene. It is changed multiplicatively while scrolling.
    private double zoomFactor = 1f;

    // How far your mouse has moved during a drag event. It's used for applying a pan effect to the camera's position.
    private final Vector2D mouseDragOffset = new Vector2D(0, 0);

    // Current frames per second, used for displaying FPS at the top left of the window
    private float fps = 60;

    // Booleans used to determine which window you are typing in.
    private boolean typingMessage = false, typingPort = false, typingIP = false;

    // Stores what is currently being typed into the message, ip, and port fields.
    private String messagingInput = "", ipInput = "", portInput = "";

    // Stores the entire message history that this specific client has observed from itself and other clients through the server.
    private final ArrayList<String> messageHistory = new ArrayList<>();

    // Stores the most recent message that each player has typed. Gets reset according to the MESSAGE_EXPIRATION_TIME.
    private final Message[] recentMessages = new Message[10];
    private static final int MESSAGE_EXPIRATION_TIME = 10000;

    // If the player is stepping on a tip box it will be stored here.
    // It is a string buffer so that it can be modified concurrently.
    private StringBuffer tipText = new StringBuffer();

    // Stores the typed Port and IP after it has been inputted.
    private int typedPort;
    private String typedIP;

    // integers containing the default width and height of the game window
    private int WINDOW_WIDTH = 1300;
    private int WINDOW_HEIGHT = 800;

    /** Constructs a GamePanel object that can be used for GUI purposes.
     *
     * @param gameObjects The game objects to be rendered.
     */
    GamePanel(GameObject[] gameObjects) {
        JFrame frame = new JFrame("Liam's Platformer Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(true);
        frame.addKeyListener(this);
        frame.addMouseWheelListener(this);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.add(this);
        frame.setVisible(true);
        this.gameObjects = gameObjects;
        this.clientPlayer = null;
    }

    /**
     * Adds to the player's velocity depending on what direction you are trying to move.
     * @param dtMod
     */
    void handlePlayerMovementInputs(float dtMod) {
        if (typingMessage || this.clientPlayer == null) return;


        if (this.keyPressed[KeyEvent.VK_BACK_SLASH] && this.keyPressed[KeyEvent.VK_SHIFT]) {
            this.clientPlayer.setGodMode(true);
        } else if (this.keyPressed[KeyEvent.VK_SLASH]) {
            this.clientPlayer.setGodMode(false);
        }

        if (!clientPlayer.getGodMode()) {
            if (this.keyPressed[KeyEvent.VK_LEFT] || this.keyPressed[KeyEvent.VK_A]) {
                this.clientPlayer.getVelocity().addXY(-clientPlayer.getSpeed() * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT] || this.keyPressed[KeyEvent.VK_D]) {
                this.clientPlayer.getVelocity().addXY(clientPlayer.getSpeed() * dtMod, 0);
            }
            if ((this.keyPressed[KeyEvent.VK_UP] || this.keyPressed[KeyEvent.VK_W] || this.keyPressed[KeyEvent.VK_SPACE]) && clientPlayer.getGrounded()) {
                this.clientPlayer.getVelocity().addXY(0, -17);
            }
        } else {
            this.clientPlayer.setVel(new Vector2D(0, 0));
            if (this.keyPressed[KeyEvent.VK_LEFT] || this.keyPressed[KeyEvent.VK_A]) {
                this.clientPlayer.getPos().addXY(-25 * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT] || this.keyPressed[KeyEvent.VK_D]) {
                this.clientPlayer.getPos().addXY(25 * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_UP] || this.keyPressed[KeyEvent.VK_W]) {
                this.clientPlayer.getPos().addXY(0, -25 * dtMod);
            }
            if (this.keyPressed[KeyEvent.VK_DOWN] || this.keyPressed[KeyEvent.VK_S]) {
                this.clientPlayer.getPos().addXY(0, 25 * dtMod);
            }
        }
    }
    void setClientPlayer(Player clientPlayer) {
        this.clientPlayer = clientPlayer;
    }
    boolean initializedPlayer() {
        return !(this.clientPlayer == null);
    }
    /**
     * Displays everything to the screen by calling the repaint() method, also updates the fps field.
     * @param curFps current fps of the game loop
     */
    void renderScene(float curFps) {
        fps = curFps;
        repaint();
    }

    private void renderHomeScreen(Graphics g) {
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Server Connection", 70, 50);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("IP Address:", 50, 100);
        g.setColor(typingIP ? Color.LIGHT_GRAY : Color.GRAY);
        g.fillRect(150, 80, 200, 30);
        g.setColor(Color.BLACK);
        g.drawString(ipInput.isEmpty() ? "Enter IP..." : ipInput, 160, 100);

        g.setColor(Color.WHITE);
        g.drawString("Port:", 50, 150);
        g.setColor(typingPort ? Color.LIGHT_GRAY : Color.GRAY);
        g.fillRect(150, 130, 200, 30);
        g.setColor(Color.BLACK);
        g.drawString(portInput.isEmpty() ? "Enter Port..." : portInput, 160, 150);

        g.setColor(new Color(50, 200, 50));
        g.fillRect(150, 200, 100, 40);
        g.setColor(Color.BLACK);
        g.drawString("Connect", 170, 225);
    }

    private void renderMessageSystem(Graphics g, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        // Render tips
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(tipText), (getWidth() / 2) - (metrics.stringWidth(String.valueOf(tipText)) / 2), 200);
        tipText.setLength(0);

        // Default messaging window width and height. Changes if you are typing a message.
        int messagingWindowWidth = 300;
        int messagingWindowHeight = 300;

        if(typingMessage) {
            messagingWindowWidth = 450;
            messagingWindowHeight = getHeight() - 200;
        }

        // Calculating the number of rows and columns of text that will fit inside the messaging window using font metrics.
        int colsAvailable = (int) Math.floor((double) (messagingWindowWidth - 20) / metrics.charWidth('_'));
        int rowsAvailable = (int) Math.floor((double) (messagingWindowHeight - 20) / (metrics.charWidth('_') * 2));

        // Display Messaging window background
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(0, getHeight() - messagingWindowHeight, messagingWindowWidth, messagingWindowHeight);
        g.setColor(new Color(255, 255, 255, 100));
        g.fillRect(0, getHeight() - messagingWindowHeight - 5, messagingWindowWidth, 5);
        g.fillRect(messagingWindowWidth, getHeight() - messagingWindowHeight - 5, 5, messagingWindowHeight + 10);
        g.setColor(Color.BLACK);

        ArrayList<String> lines = new ArrayList<>(); // ArrayList of Strings containing lines of text to be displayed

        // Loops through the messageHistory and adds the most recent messages to the lines list
        for(String message : messageHistory) {
            int id = Character.getNumericValue(message.charAt(6));

            for(int i = 0; i < message.length(); i += colsAvailable) {
                lines.add(id + message.substring(i, Math.min(i + colsAvailable, message.length())));
            }
        }

        // A ticker that will alternate between "_" and "" every 500 ms when the player is typing.
        String ticker = (typingMessage && (System.currentTimeMillis() % 1000 < 500) ? "_" : "");
        // Add a gap between the message history and the player's current message
        lines.add("");
        // Adds a line saying "Send Message"
        lines.add((this.clientPlayer.getId() + 1) + "Send Message:" + (messagingInput.isEmpty() ? ticker : ""));
        // Adds the ticker to the current message being written
        String typingText = messagingInput + (messagingInput.isEmpty() ? "" : ticker);
        // Now add the player's current message to the lines list
        for(int i = 0; i < typingText.length(); i += colsAvailable) {
            lines.add((this.clientPlayer.getId() + 1) + typingText.substring(i, Math.min(i + colsAvailable, typingText.length())));
        }
        // Loop through all visible lines and display them as text to the window
        for(int i = lines.size() - 1; i >= Math.max(lines.size() - (1 + rowsAvailable), 0); i--) {
            if(lines.get(i).isEmpty()) continue;

            int id = Character.getNumericValue(lines.get(i).charAt(0));

            g.setColor(Color.BLACK);

            try {
                if (id != -1 && this.gameObjects[id - 1] != null) {
                    g.setColor(this.gameObjects[id - 1].getColor()); // Set the color of the line based on what player sent the message
                }
            } catch(Exception ignored) {}
            // Display the line
            g.drawString(lines.get(i).substring(1), 10, getHeight() - 10 - ((lines.size() - i) * 20));
        }
    }

    private void renderGameObjects(Graphics g, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        // Vector containing the offset applied to every object rendered to the screen based on the player's location.
        // Used for centering the player on the screen.
        Vector2D cameraOffset = new Vector2D(
                (double) getWidth() / 2 - this.clientPlayer.getPos().getX() - (this.clientPlayer.getDim().getX() / 2) - mouseDragOffset.getX(),
                (double) getHeight() / 2 - this.clientPlayer.getPos().getY() - (this.clientPlayer.getDim().getY() / 2) - mouseDragOffset.getY());

        // Draw the background as a black rectangle
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Render all game objects
        for(GameObject r : this.gameObjects) {

            if(r == null) continue;

            // Vector containing the position of the top left vertex of the rectangular GameObject.
            Vector2D topLeft = new Vector2D(
                    r.getPos().getX() + cameraOffset.getX(),
                    r.getPos().getY() + cameraOffset.getY());
            // Vector containing the position of the bottom right vertex of the rectangular GameObject.
            Vector2D botRight = new Vector2D(
                    r.getPos().getX() + cameraOffset.getX() + r.getDim().getX(),
                    r.getPos().getY() + cameraOffset.getY() + r.getDim().getY());

            // Apply a scaling factor to those two coordinates based on their location relative to the center of the screen
            // Practically, this zooms the window in and out depending on the zoom factor
            Vector2D topLeftTransform = topLeft.copy();
            Vector2D botRightTransform = botRight.copy();
            topLeftTransform.subtract((float) getWidth() / 2, (float) getHeight() / 2);
            botRightTransform.subtract((float) getWidth() / 2, (float) getHeight() / 2);
            topLeftTransform.scale(zoomFactor - 1);
            botRightTransform.scale(zoomFactor - 1);
            topLeft.add(topLeftTransform);
            botRight.add(botRightTransform);

            // If the rectangle is outside the bounds of the window, ignore rendering it and move on to the next.
            if(topLeft.getX() > getWidth() || botRight.getX() < 0 || topLeft.getY() > getHeight() || botRight.getY() < 0) continue;

            g.setColor(r.getColor()); // Set the color of the rectangle to the color of the game object

            // If the game object is that of a player, if they have a recent message display that above them
            if(r instanceof Player) {
                double xTransform = topLeft.getX() + (botRight.getX() - topLeft.getX()) / 2;
                double yTransform = topLeft.getY() - (metrics.charWidth('_') * zoomFactor);
                handleMessagePreview(g, font, (Player) r, new Vector2D(xTransform, yTransform));

                if(((Player) r).getGodMode()) {
                    Color oldColor = r.getColor();
                    g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), 100));
                }
            }
            // Display the game object to the screen. I made each game object be 2 x 2 pixels wider than it would be
            // ordinarily, because I was encountering issues with 1 pixel gaps appearing between walls when the player
            // would zoom. It's not elegant but it works.
            g.fillRect((int) topLeft.getX() - 1, (int) topLeft.getY() - 1, (int) (botRight.getX() - topLeft.getX()) + 1, (int) (botRight.getY() - topLeft.getY()) + 1);
        }
    }

    private void handleMessagePreview(Graphics g, Font font, Player messageOwner, Vector2D messageRenderLocation) {
        FontMetrics metrics = g.getFontMetrics(font);
        Graphics2D g2d = (Graphics2D) g;

        int id = messageOwner.getId();

        if (recentMessages[id] == null) return; // If there is no recent message by that player return

        String[] mData = recentMessages[id].getMessage().split(";");

        // Cut off the Player1: section of the message
        String theMessage = mData[0].substring(mData[0].indexOf(":") + 1);
        // Determine the number of lines that the player's message will be
        int lines = (int) Math.ceil(((double) theMessage.length() / 20));


        g2d.setColor(messageOwner.getColor()); // Set the color of the message to the owner's color.
        g2d.translate(messageRenderLocation.getX(), messageRenderLocation.getY());
        g2d.scale(zoomFactor, zoomFactor); // Scale the message based on the zoom factor.

        // Goes through every line and renders it centered above the player's position.
        for (int i = 0; i < theMessage.length(); i += 20) {
            String line = theMessage.substring(i, Math.min(i + 20, theMessage.length()));
            int lineY = (int) (-(lines - (i / 20)) * (metrics.charWidth('_') * 1.25));
            g2d.drawString(line, -metrics.stringWidth(line) / 2, lineY);
        }

        g2d.scale(1 / zoomFactor, 1 / zoomFactor);
        g2d.translate(-messageRenderLocation.getX(), -messageRenderLocation.getY());

        // If the message is past its expiration date, remove it from the array.
        if (GameClient.getTime() - Long.parseLong(mData[1]) > MESSAGE_EXPIRATION_TIME)
            recentMessages[id] = null;
    }

    int getTypedPort() {
        return this.typedPort;
    }

    public String getIP() {
        return this.typedIP;
    }

    /**
     * Updates the message history and recent messages to contain a new message from a player object.
     * @param playerObject player to detect the message from
     */
    public void newMessageFromServer(Player playerObject) {
        String communication = playerObject.getCommunication();
        this.messageHistory.add(communication);
        this.recentMessages[playerObject.getId()] = new Message(communication + ";" + GameClient.getTime());
        playerObject.setCommunication("");
    }

    private String handleTypingInput(char c, String input) {
        String output = input;
        if(c == '\n') return output;
        if (c == '\b' && !output.isEmpty()) {
            output = output.substring(0, output.length() - 1);
        } else if (isCharAllowed(c) && output.length() < 500) {
            output += c;
        }
        return output;
    }

    private boolean isCharAllowed(char toCheck) {
        if(Character.isLetterOrDigit(toCheck) || Character.isWhitespace(toCheck)) return true;
        String allowed = "!@#$%^&*()_+-=|}]{[':;'/>.<,~`?";
        for(char allowedChar : allowed.toCharArray()) {
            if(toCheck == allowedChar) return true;
        }
        return false;
    }

    void addTip(String tip) {tipText.replace(0, tipText.length(), tip);}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font font = new Font("Courier New", Font.PLAIN, 18);
        FontMetrics metrics = g.getFontMetrics(font);
        if (this.clientPlayer != null && this.gameObjects != null) {
            renderGameObjects(g, font);
            renderMessageSystem(g, font);
            g.setColor(Color.WHITE);
            g.drawString("FPS" + ((round(fps) > 144) ? ">144" : round(fps)), 10, 20);
            if(this.clientPlayer.getGodMode()) {
                g.setColor(Color.WHITE);
                g.drawString("SPECTATE MODE, Wait for everyone else to finish" , (getWidth() / 2) - (metrics.stringWidth("SPECTATE MODE, Wait for everyone else to finish") / 2), 30);
            }
        } else {
            renderHomeScreen(g);
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getKeyCode() > 254) return; // Prevent accessing an index outside the bounds of keyPressed

        keyPressed[keyEvent.getKeyCode()] = true; // Set the corresponding key to pressed

        char c = keyEvent.getKeyChar(); // Get the character that was just entered.

        if(typingMessage && this.clientPlayer != null) {
            if(c == '\n') typingMessage = false; // If you type enter set typingMessage to false.

            // If you type enter and your message was not empty, add it to message history and recent messages
            if(c == '\n' && !this.messagingInput.isEmpty()) {
                String toAdd = "Player" + (this.clientPlayer.getId() + 1) + ":" + messagingInput;
                this.messageHistory.add(toAdd);
                this.recentMessages[this.clientPlayer.getId()] = new Message(toAdd + ";" + GameClient.getTime());
                this.clientPlayer.setCommunication(toAdd);
                messagingInput = "";
            }

            messagingInput = handleTypingInput(c, messagingInput); // Otherwise add whatever you typed to messagingInput
        }else {
            // If you pressed enter, and you were not previously typing, enter typing mode.
            if(c == '\n') typingMessage = true;
        }

        if(this.clientPlayer == null) { // Handle capturing the Port and IP
            if(typingIP) {
                if (c == '\n') {
                    typingIP = false;
                }
                ipInput = handleTypingInput(c, ipInput);
            }else if(typingPort && Character.isDigit(c) || c == '\b') {
                if (c == '\n') {
                    typingPort = false;
                }
                portInput = handleTypingInput(c, portInput);
            }
            repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double change = 0.1;
        if (e.getWheelRotation() < 0 && (zoomFactor < 10 || this.clientPlayer.getGodMode())) {
            zoomFactor *= 1 + change;

        } else if(e.getWheelRotation() > 0 && (zoomFactor > 0.3 || this.clientPlayer.getGodMode())){
            zoomFactor *= 1 - change;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() > 254) return;
        keyPressed[e.getKeyCode()] = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(this.clientPlayer == null || this.gameObjects == null) return;

        int x = e.getX();
        int y = e.getY();
        if(x > getWidth()) x = getWidth();
        if(x < 0) x = 0;
        if(y > getHeight()) y = getHeight();
        if(y < 0) y = 0;
        mouseDragOffset.setXY((x - (double) getWidth() / 2), (y - (double) getHeight() / 2));
        mouseDragOffset.divide(zoomFactor);
    }
    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {mouseDragOffset.setXY(0, 0);}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {
        if(this.clientPlayer != null) {
            if (e.getX() < 200 && e.getY() > getHeight() - 100) { // The player has clicked the messaging window
                typingMessage = true;
            } else {
                typingMessage = false;
            }
        }else {
            if(e.getX() > 157 && e.getX() < 257 && e.getY() > 230 && e.getY() < 272) { // Clicked the Connect button
                this.typedPort = Integer.parseInt(portInput);
                this.typedIP = ipInput;
            }

            if(e.getX() > 150 && e.getX() < 360 ) {
                if (e.getY() > 111 && e.getY() < 141) { // Clicked the IP text field
                    typingIP = !typingIP;
                    typingPort = false;
                    repaint();
                    return;
                } else if (e.getY() > 161 && e.getY() < 191) { // Clicked the Port text field
                    typingPort = !typingPort;
                    typingIP = false;
                    repaint();
                    return;
                }
            }

            typingPort = false;
            typingIP = false;
            repaint();
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}
}
