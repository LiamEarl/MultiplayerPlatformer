package client.game;
import client.model.*;
import client.model.Box;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.round;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.css.ElementCSSInlineStyle;
import server.Message;

public class Game extends JPanel implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {

    private Player player;
    private final boolean[] keyPressed = new boolean[256];
    private final GameObject[] gameObjects;
    private double zoomFactor = 1f;
    private final Vector2D mouseDragOffset = new Vector2D(0, 0);
    private float fps = 60;
    private boolean typingMessage = false, typingPort = false, typingIP = false;
    private String messagingInput = "", ipInput = "", portInput = "";
    private final ArrayList<String> messageHistory = new ArrayList<>();
    private int port;
    private String ip;

    private ServerHandler serverConnection;
    private final ArrayList<Level> levels;
    private int level;

    Message[] recentMessages = new Message[10];

    Game(GameObject[] gameObjects, ServerHandler serverConnection) throws IOException {
        this.serverConnection = serverConnection;
        JFrame frame = new JFrame("Liam's Platformer Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int WINDOW_WIDTH = 1300;
        int WINDOW_HEIGHT = 800;
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(true);
        frame.addKeyListener(this);
        frame.addMouseWheelListener(this);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.add(this);
        frame.setVisible(true);
        this.gameObjects = gameObjects;
        this.levels = new ArrayList<>();
        this.level = 0;
        this.player = null;
        createLevels("src/main/resources/levels.json");
        loadCurrentLevel();
    }

    void setPlayer(Player player) {
        this.player = player;
        this.player.setSpawnPoint(this.levels.getFirst().getSpawnPoint());
        this.player.respawn();
    }

    boolean initializedPlayer() {
        return !(this.player == null);
    }

    void createLevels(String jsonPath) throws IOException {

        String jsonString = new String(Files.readAllBytes(Paths.get(jsonPath)));
        JSONArray jsonArray = new JSONArray(jsonString);

        HashMap<String, Color> colors = new HashMap<>();
        colors.put("checkpointColor", new Color(4, 126, 220));
        colors.put("deathBoxColor", new Color(239, 26, 26));
        colors.put("finishLineColor", new Color(26, 140, 41));
        colors.put("drabWallColor", new Color(32, 32, 45));
        colors.put("trampolineColor", new Color(255, 213, 0));
        colors.put("iceColor", new Color(4, 169, 222));

        ArrayList<GameObject> buffer = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray rowArray = jsonArray.getJSONArray(i);
            Vector2D spawnLoc = new Vector2D(100, 700);
            // Iterate through each object in the row
            for (int j = 0; j < rowArray.length(); j++) {
                JSONObject jsonObject = rowArray.getJSONObject(j);

                // Get the type of object
                String type = jsonObject.getString("type");
                if(type.equals("Spawn")) {
                    spawnLoc.setX(jsonObject.getInt("x"));
                    spawnLoc.setY(jsonObject.getInt("y"));
                    continue;
                }

                int boxX = jsonObject.getInt("x");
                int boxY = jsonObject.getInt("y");
                int boxW = jsonObject.getInt("w");
                int boxH = jsonObject.getInt("h");
                String boxEq = jsonObject.has("equation") ? jsonObject.getString("equation") : "#~#";
                Color boxCol = colors.get(jsonObject.getString("color"));

                // Depending on the type, create the appropriate object and add it to the buffer
                switch (type) {
                    case "Box":
                        buffer.add(new Box(boxX, boxY, boxW, boxH, boxCol, boxEq));
                        break;
                    case "Trampoline":
                        buffer.add(new Trampoline(boxX, boxY, boxW, boxH, boxCol));
                        break;
                    case "Checkpoint":
                        buffer.add(new Checkpoint(boxX, boxY, boxW, boxH, boxCol));
                        break;
                    case "DeathBox":
                        buffer.add(new DeathBox(boxX, boxY, boxW, boxH, boxCol, boxEq));
                        break;
                    case "FinishLine":
                        buffer.add(new FinishLine(boxX, boxY, boxW, boxH, boxCol));
                        break;
                    case "Ice":
                        buffer.add(new Ice(boxX, boxY, boxW, boxH, boxCol, boxEq));
                        break;
                    // Add other cases as needed (for more object types)
                    default:
                        System.out.println("Unknown object");
                }
            }
            levels.add(new Level((ArrayList<GameObject>) buffer.clone(), spawnLoc));
            buffer.clear();
        }
    }

    private void loadCurrentLevel() {

        ArrayList<GameObject> objects = this.levels.get(this.level).getObjects();
        if(objects.isEmpty()) return;
        for(int i = 10; i < this.gameObjects.length; i++) {
            this.gameObjects[i] = null;
        }
        for (int i = 0; i < objects.size(); i++) {
            this.gameObjects[i + 10] = objects.get(i);
        }
        if(this.player != null) {
            this.player.setSpawnPoint(this.levels.get(this.level).getSpawnPoint());
            this.player.respawn();
            this.player.setGodMode(false);
        }
    }

    void renderScene(float curFps) {
        fps = curFps;
        repaint();
    }

    void handleKeyInputs(float dtMod) {
        if (typingMessage || this.player == null) return;


        if (this.keyPressed[KeyEvent.VK_BACK_SLASH] && this.keyPressed[KeyEvent.VK_SHIFT]) {
            this.player.setGodMode(true);
        } else if (this.keyPressed[KeyEvent.VK_SLASH]) {
            this.player.setGodMode(false);
        }

        if (!player.getGodMode()) {
            if (this.keyPressed[KeyEvent.VK_LEFT] || this.keyPressed[KeyEvent.VK_A]) {
                this.player.getVelocity().addXY(-player.getSpeed() * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT] || this.keyPressed[KeyEvent.VK_D]) {
                this.player.getVelocity().addXY(player.getSpeed() * dtMod, 0);
            }
            if ((this.keyPressed[KeyEvent.VK_UP] || this.keyPressed[KeyEvent.VK_W] || this.keyPressed[KeyEvent.VK_SPACE]) && player.getGrounded()) {
                this.player.getVelocity().addXY(0, -17);
            }
        } else {
            this.player.setVel(new Vector2D(0, 0));
            if (this.keyPressed[KeyEvent.VK_LEFT] || this.keyPressed[KeyEvent.VK_A]) {
                this.player.getPos().addXY(-25 * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT] || this.keyPressed[KeyEvent.VK_D]) {
                this.player.getPos().addXY(25 * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_UP] || this.keyPressed[KeyEvent.VK_W]) {
                this.player.getPos().addXY(0, -25 * dtMod);
            }
            if (this.keyPressed[KeyEvent.VK_DOWN] || this.keyPressed[KeyEvent.VK_S]) {
                this.player.getPos().addXY(0, 25 * dtMod);
            }
        }
    }

    void updateGameObjects(float dtMod, long currentTime) {
        if(this.gameObjects == null) return;

        for (GameObject gameObject : this.gameObjects) {
            if (gameObject == null) continue;
            if (gameObject instanceof Player) {
                Player playerObject = (Player) gameObject;
                playerObject.update(dtMod, currentTime);
                if (!playerObject.getCommunication().equals("") && playerObject.getId() != this.player.getId()) {
                    String communication = playerObject.getCommunication();
                    this.messageHistory.add(communication);
                    if(this.serverConnection != null)
                        this.recentMessages[playerObject.getId()] = new Message(communication + ";" + this.serverConnection.getServerTime());
                    playerObject.setCommunication("");
                }
                continue;
            }
            gameObject.update(dtMod, currentTime);
        }
    }

    void checkPlayerCollisions(float dtMod) {
        if(this.gameObjects == null) return;
        for (GameObject obj : this.gameObjects) {

            if (obj == null) continue;
            if (!(obj instanceof Player)) {
                for (int i = 0; i < 10; i++) {
                    if (!(this.gameObjects[i] instanceof Player)) continue;
                    Player currentPlayer = (Player) this.gameObjects[i];
                    if (currentPlayer == null) continue;
                    if (currentPlayer.getGodMode()) continue;
                    handleDynamicOnStatic(currentPlayer, obj, dtMod);
                    if (currentPlayer.getPos().getY() > 3000) currentPlayer.respawn();
                }
            }
        }
    }

    private void handleDynamicOnStatic(GameObject dynamic, GameObject toCollide, float dtMod) {
        Vector2D pPos = dynamic.getPos();
        Vector2D pDim = dynamic.getDim();
        Vector2D pVel = dynamic.getVelocity();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();
        Vector2D oVel = toCollide.getVelocity();

        if (toCollide instanceof DeathBox && dynamic instanceof Player) {
            boolean isColliding = pPos.getX() < oPos.getX() + oDim.getX() &&
                    pPos.getX() + pDim.getX() > oPos.getX() &&
                    pPos.getY() < oPos.getY() + oDim.getY() &&
                    pPos.getY() + pDim.getY() > oPos.getY();
            if (!isColliding) return;
            ((Player) dynamic).respawn();
            return;
        }

        boolean isBoundingBoxColliding = pPos.getX() + pVel.getX() < oPos.getX() + oDim.getX() + oVel.getX() &&
                pPos.getX() + pDim.getX() + pVel.getX() > oPos.getX() + oVel.getX() &&
                pPos.getY() + pVel.getY() < oPos.getY() + oDim.getY() + oVel.getY() &&
                pPos.getY() + pDim.getY() + pVel.getY() > oPos.getY() + oVel.getY();

        if (!isBoundingBoxColliding) return;

        double overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        double overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

        if (overlapX < overlapY) {
            if (pPos.getX() < oPos.getX()) {
                pPos.addXY(-overlapX, 0);
                if (pPos.getX() + pDim.getX() <= oPos.getX()) {
                    dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                }
            } else {
                pPos.addXY(overlapX, 0);
                if (pPos.getX() >= oPos.getX() + oDim.getX()) {
                    dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                }
            }
        } else {
            if (pPos.getY() < oPos.getY()) {
                pPos.addXY(0, -overlapY);
                if(dynamic instanceof Player) ((Player) dynamic).setGrounded(true);
                float xModifier = (float) ((toCollide instanceof Ice) ? dynamic.getVelocity().getX() : dynamic.getVelocity().getX() * (1 - 0.03f * dtMod));
                dynamic.getVelocity().setXY(xModifier, oVel.getY() * 0.9f);
                if (toCollide instanceof Trampoline) {
                    dynamic.getVelocity().addXY(0, -36);
                    if(dynamic instanceof Player) ((Player) dynamic).setGrounded(false);
                }
            } else {
                pPos.addXY(0, overlapY);
                dynamic.getVelocity().setXY(dynamic.getVelocity().getX(), -dynamic.getVelocity().getY());
            }
        }

        if (toCollide instanceof Checkpoint && dynamic instanceof Player)
            ((Player) dynamic).setSpawnPoint(new Vector2D(oPos.getX() + (oDim.getX() / 2) - (pDim.getX() / 2), oPos.getY() - pDim.getY()));
        if (toCollide instanceof FinishLine && dynamic.equals(this.player)) this.player.setGodMode(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font font = new Font("Courier New", Font.PLAIN, 18);
        if (this.player != null && this.gameObjects != null) {
            renderGameObjects(g, font);
            renderMessageSystem(g, font);
            g.setColor(Color.WHITE);
            g.drawString("FPS" + ((round(fps) > 144) ? ">144" : round(fps)), 10, 20);
        } else {
            renderHomeScreen(g);
        }
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

        int windowWidth = 300;
        int windowHeight = 300;

        if(typingMessage) {
            windowWidth = 450;
            windowHeight = getHeight() - 200;
        }

        int colsAvailable = (int) Math.floor((double) (windowWidth - 20) / metrics.charWidth('_'));
        int rowsAvailable = (int) Math.floor((double) (windowHeight - 20) / (metrics.charWidth('_') * 2));

        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(0, getHeight() - windowHeight, windowWidth, windowHeight);
        g.setColor(new Color(255, 255, 255, 100));
        g.fillRect(0, getHeight() - windowHeight - 5, windowWidth, 5);
        g.fillRect(windowWidth, getHeight() - windowHeight - 5, 5, windowHeight + 10);

        g.setColor(Color.BLACK);

        ArrayList<String> lines = new ArrayList<>();
        for(String message : messageHistory) {
            int id = Character.getNumericValue(message.charAt(6));

            for(int i = 0; i < message.length(); i += colsAvailable) {
                lines.add(id + message.substring(i, Math.min(i + colsAvailable, message.length())));
            }
        }

        String ticker = (typingMessage && (System.currentTimeMillis() % 1000 < 500) ? "_" : "");
        lines.add("");
        lines.add((this.player.getId() + 1) + "Send Message:" + (messagingInput.isEmpty() ? ticker : ""));
        String typingText = messagingInput + (messagingInput.isEmpty() ? "" : ticker);

        for(int i = 0; i < typingText.length(); i += colsAvailable) {
            lines.add((this.player.getId() + 1) + typingText.substring(i, Math.min(i + colsAvailable, typingText.length())));
        }

        for(int i = lines.size() - 1; i >= Math.max(lines.size() - (1 + rowsAvailable), 0); i--) {
            if(lines.get(i).isEmpty()) continue;

            int id = Character.getNumericValue(lines.get(i).charAt(0));

            g.setColor(Color.BLACK);

            try {
                if (id != -1 && this.gameObjects[id - 1] != null) {
                    g.setColor(this.gameObjects[id - 1].getColor());
                }
            } catch(Exception e) {}

            g.drawString(lines.get(i).substring(1), 10, getHeight() - 10 - ((lines.size() - i) * 20));
        }
    }

    private void renderGameObjects(Graphics g, Font font) {
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics metrics = g.getFontMetrics(font);
        g.setFont(font);

        Vector2D cameraOffset = new Vector2D(
                (double) getWidth() / 2 - this.player.getPos().getX() - (this.player.getDim().getX() / 2) - mouseDragOffset.getX(),
                (double) getHeight() / 2 - this.player.getPos().getY() - (this.player.getDim().getY() / 2) - mouseDragOffset.getY());

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for(GameObject r : this.gameObjects) {

            if(r == null) continue;

            Vector2D topLeft = new Vector2D(
                    r.getPos().getX() + cameraOffset.getX(),
                    r.getPos().getY() + cameraOffset.getY());
            Vector2D botRight = new Vector2D(
                    r.getPos().getX() + cameraOffset.getX() + r.getDim().getX(),
                    r.getPos().getY() + cameraOffset.getY() + r.getDim().getY());

            Vector2D topLeftTransform = topLeft.copy();
            Vector2D botRightTransform = botRight.copy();
            topLeftTransform.subtract((float) getWidth() / 2, (float) getHeight() / 2);
            botRightTransform.subtract((float) getWidth() / 2, (float) getHeight() / 2);
            topLeftTransform.scale(zoomFactor - 1);
            botRightTransform.scale(zoomFactor - 1);
            topLeft.add(topLeftTransform);
            botRight.add(botRightTransform);

            if(topLeft.getX() > getWidth() || botRight.getX() < 0 || topLeft.getY() > getHeight() || botRight.getY() < 0) continue;

            // Draw the rectangle
            g.setColor(r.getColor());

            if(r instanceof Player) {
                int id = ((Player) r).getId();
                if(recentMessages[id] != null && this.serverConnection != null) {
                    String[] mData = recentMessages[id].getMessage().split(";");

                    //cut off the Player1: section
                    String theMessage = mData[0].substring(mData[0].indexOf(":") + 1);

                    double xTransform = topLeft.getX() + (botRight.getX() - topLeft.getX()) / 2;
                    double yTransform = topLeft.getY() - (metrics.charWidth('_') * zoomFactor);

                    int lines = (int) Math.ceil(((double) theMessage.length() / 20));

                    g2d.setColor(r.getColor());
                    g2d.translate(xTransform, yTransform);
                    g2d.scale(zoomFactor, zoomFactor);

                    for (int i = 0; i < theMessage.length(); i += 20) {
                        String line = theMessage.substring(i, Math.min(i + 20, theMessage.length()));
                        int lineY = (int) (-(lines - (i / 20)) * (metrics.charWidth('_') * 1.25));
                        g2d.drawString(line, -metrics.stringWidth(line) / 2, lineY);
                    }

                    g2d.scale(1 / zoomFactor, 1 / zoomFactor);
                    g2d.translate(-xTransform, -yTransform);

                    if(this.serverConnection.getServerTime() - Long.parseLong(mData[1]) > 7000) recentMessages[id] = null;
                }

                if(((Player) r).getGodMode()) {
                    Color oldColor = r.getColor();
                    g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), 100));
                }
            }

            g.fillRect((int) topLeft.getX(), (int) topLeft.getY(), (int) (botRight.getX() - topLeft.getX()), (int) (botRight.getY() - topLeft.getY()));
        }

        if(this.player.getGodMode()) {
            g.setColor(Color.WHITE);
            g.drawString("SPECTATE MODE" , (getWidth() / 2) - 50, 30);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressed[e.getKeyCode()] = true;

        char c = e.getKeyChar();

        if(typingMessage && this.player != null) {
            if(c == '\n') typingMessage = false;
            if(c == '\n' && !this.messagingInput.isEmpty()) {
                String toAdd = "Player" + (this.player.getId() + 1) + ":" + messagingInput;
                this.messageHistory.add(toAdd);
                if(this.serverConnection != null)
                    this.recentMessages[this.player.getId()] = new Message(toAdd + ";" + this.serverConnection.getServerTime());
                this.player.setCommunication(toAdd);
                messagingInput = "";
            }
           messagingInput = handleTyping(c, messagingInput);
        }else {
            if(c == '\n') typingMessage = true;
        }

        if(this.player == null) {
            if(typingIP) {
                if (c == '\n') {
                    typingIP = false;
                }
                ipInput = handleTyping(c, ipInput);
            }else if(typingPort && Character.isDigit(c) || c == '\b') {
                if (c == '\n') {
                    typingPort = false;
                }
                portInput = handleTyping(c, portInput);
            }
            repaint();
        }
    }

    private String handleTyping(char c, String input) {
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

    public int getPort() {
        return this.port;
    }
    public String getIP() {
        return this.ip;
    }
    public GameObject[] getGameObjects() {
        return this.gameObjects;
    }
    public void setServerConnection(ServerHandler toSet) {
        this.serverConnection = toSet;
    }
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        loadCurrentLevel();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
       // double change = Math.pow(2, -0.5 * Math.pow(zoomFactor - 2.5, 2)) / 5;
        double change = 0.1;
        if (e.getWheelRotation() < 0 && (zoomFactor < 10 || this.player.getGodMode())) {
            zoomFactor *= 1 + change;

        } else if(e.getWheelRotation() > 0 && (zoomFactor > 0.3 || this.player.getGodMode())){
            zoomFactor *= 1 - change;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        keyPressed[e.getKeyCode()] = false;
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        if(this.player == null || this.gameObjects == null) return;

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
        System.out.println(e.getX() + " " + e.getY());
        if(this.player != null) {
            if (e.getX() < 200 && e.getY() > getHeight() - 100) {
                typingMessage = true;
            } else {
                typingMessage = false;
            }
        }else {
            if(e.getX() > 157 && e.getX() < 257 && e.getY() > 230 && e.getY() < 272) {
                this.port = Integer.parseInt(portInput);
                this.ip = ipInput;
            }

            if(e.getX() > 150 && e.getX() < 360) {
                if (e.getY() > 111 && e.getY() < 141) {
                    typingIP = !typingIP;
                    typingPort = false;
                    repaint();
                    return;
                } else if (e.getY() > 161 && e.getY() < 191) {
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
