package client.game;
import client.model.*;
import client.model.Box;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import static java.lang.Math.round;

public class Game extends JPanel implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {

    private Player player;
    private boolean[] keyPressed = new boolean[256];
    private GameObject[] gameObjects;
    private final int WINDOW_WIDTH = 1300;
    private final int WINDOW_HEIGHT = 800;
    private double zoomFactor = 1f;
    private Vector2D mouseDragOffset = new Vector2D(0, 0);
    private float fps = 60;
    private boolean typingMessage = false, typingPort = false, typingIP = false;
    private String messagingInput = "", ipInput = "", portInput = "";
    private ArrayList<String> messageHistory = new ArrayList<>();
    private int port;
    private String ip;

    Game(GameObject[] gameObjects) {
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
        createGameObjects();;
        this.player = null;
    }

    void setGameObjects(Player player) {
        this.player = player;
        System.out.println("Initialized" + (this.player == null) + (this.gameObjects == null));
    }

    boolean initializedPlayer() {
        return !(this.player == null);
    }

    void createGameObjects() {
        ArrayList<GameObject> buffer = new ArrayList<>();
        ArrayList<GameObject> dynamicObjects = new ArrayList<>();

        Color drabWallColor = new Color(30, 20, 90);
        Color trampolineColor = new Color(255, 213, 0);
        Color checkpointColor = new Color(4, 126, 220);
        Color deathBoxColor = new Color(239, 26, 26);
        Color dynamicBoxColor = new Color(26, 140, 41);

        //dynamicObjects.add(new DynamicBox(new Vector2D(100, 500), dynamicBoxColor, new Vector2D(100, 100), 0));
/*
        buffer.add(new Box(0, 800, 1000, 100, drabWallColor,"#~#"));
        buffer.add(new Box(-214, 702, 162, 50, drabWallColor,"#~#"));
        buffer.add(new Box(120, 588, 106, 24, drabWallColor,"#~#"));
        buffer.add(new Box(466, 446, 182, 36, drabWallColor,"#~#"));
        buffer.add(new Box(230, 326, 104, 24, drabWallColor,"#~#"));
        buffer.add(new Box(-70, 190, 164, 116, drabWallColor,"#~#"));
        buffer.add(new Trampoline(876, 770, 124, 30, trampolineColor));
        buffer.add(new DeathBox(1000, 800, 1100, 100, deathBoxColor,"#~#"));
        buffer.add(new Box(2100, 800, 500, 100, drabWallColor,"#~#"));
        buffer.add(new Box(2900, 700, 200, 100, drabWallColor,"#~#"));
        buffer.add(new Box(3400, 700, 200, 100, drabWallColor,"#~#"));
        buffer.add(new Box(4000, 400, 200, 2400, drabWallColor,"#~#"));
        buffer.add(new Box(3600, 700, 200, 2300, drabWallColor,"#~#"));
        buffer.add(new Box(3600, 3000, 900, 100, drabWallColor,"#~#"));
        buffer.add(new Box(4870, 3004, 1, 1, drabWallColor,"#~#"));
        buffer.add(new Box(5230, 3000, 1, 1, drabWallColor,"#~#"));
        buffer.add(new Box(5578, 2998, 422, 102, drabWallColor,"#~#"));
        buffer.add(new Trampoline(5900, 2982, 100, 16, trampolineColor));
        buffer.add(new Checkpoint(3810, 2982, 108, 20, checkpointColor));
*/

        buffer.add(new Box(-100, 800, 600, 4000, drabWallColor, "#~#"));
        buffer.add(new Box(-1000, -500, 1050, 4000, drabWallColor, "#~#"));
        buffer.add(new Box(700, 700, 310, 4000, drabWallColor, "#~#"));
        buffer.add(new Box(1700, 700, 575, 4000, drabWallColor, "#~#"));
        buffer.add(new DeathBox(495, 1500, 210, 3000, deathBoxColor, "#~-1000picf0.5"));
        buffer.add(new Box(1000, 400, 50, 4000, drabWallColor, "#~#"));
        buffer.add(new Box(990, 550, 10, 10, drabWallColor, "#~#"));
        buffer.add(new Box(3600, 525, 70, 4000, drabWallColor, "#~#"));
        buffer.add(new Checkpoint(3600f, 515, 70, 10, checkpointColor));
        buffer.add(new DeathBox(4145, -99, 3500, 10000, deathBoxColor, "#~#"));
        buffer.add(new Box(4140, -100, 100, 6000, drabWallColor, "-100pls1.25~#"));
        buffer.add(new Box(3610, -700, 100, 1000, drabWallColor, "+100pls0.3~#"));
        buffer.add(new Box(3610, -800, 4000, 200, drabWallColor, "#~#"));
        buffer.add(new DeathBox(3670, 900, 4140 - 3670, 4000, deathBoxColor, "#~#"));
        buffer.add(new Box(3925, 435, 50, 50, drabWallColor, "#~#"));
        buffer.add(new Box(3710, 290, 10, 10, drabWallColor, "#~#"));
        buffer.add(new Box(3950, 190, 20, 5, drabWallColor, "#~#"));
        buffer.add(new Box(3710, 45, 10, 10, drabWallColor, "#~#"));
        buffer.add(new Box(4100, 20, 40, 10, drabWallColor, "#~#"));
        buffer.add(new Box(2900, 600, 200, 50, drabWallColor, "+300picn0.125~#"));
        buffer.add(new Box(4141, -101, 3501, 5, drabWallColor, "#~#"));
        buffer.add(new Checkpoint(7400, -111, 100, 10, checkpointColor));
        buffer.add(new Box(4240, -700, 1400, 500, drabWallColor, "#~+100picn0.32"));
        buffer.add(new Box(4240 + 1600, -700, 1400, 500, drabWallColor, "#~+100picn0.32"));

        for (int i = 0; i < dynamicObjects.size(); i++) {
            this.gameObjects[i + 10] = dynamicObjects.get(i);
        }
        for (int i = 0; i < buffer.size(); i++) {
            this.gameObjects[i + 20] = buffer.get(i);
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
                    /*for(int i = 0; i < communication.length(); i += 29) {
                        this.messageHistory.add(communication.substring(i, Math.min(i+29, communication.length())));
                    }*/
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
            if(!(obj instanceof DynamicBox)) {
                for (int i = 10; i < 20; i++) {
                    if (!(this.gameObjects[i] instanceof DynamicBox)) continue;
                    DynamicBox currentBox = (DynamicBox) this.gameObjects[i];
                    if (currentBox == null) continue;
                    if(obj instanceof Player) handleDymamicOnDynamic(currentBox, obj, dtMod);
                    else handleDynamicOnStatic(currentBox, obj, dtMod);
                }
            }
            if (!(obj instanceof Player)) {
                for (int i = 0; i < 10; i++) {
                    if (!(this.gameObjects[i] instanceof Player)) continue;
                    Player currentPlayer = (Player) this.gameObjects[i];
                    if (currentPlayer == null) continue;
                    if (currentPlayer.getGodMode()) continue;
                    if(obj instanceof DynamicBox) handleDymamicOnDynamic(currentPlayer, obj, dtMod);
                    else handleDynamicOnStatic(currentPlayer, obj, dtMod);
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
                    if(toCollide instanceof DynamicBox) {
                        dynamic.getVelocity().setXY(0, dynamic.getVelocity().getY());
                    }else {
                        dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                    }
                }
            } else {
                pPos.addXY(overlapX, 0);
                if (pPos.getX() >= oPos.getX() + oDim.getX()) {
                    if(toCollide instanceof DynamicBox) {
                        dynamic.getVelocity().setXY(0, dynamic.getVelocity().getY());
                    }else {
                        dynamic.getVelocity().setXY(oVel.getX(), dynamic.getVelocity().getY());
                    }
                }
            }
        } else {
            if (pPos.getY() < oPos.getY()) {
                pPos.addXY(0, -overlapY);
                if(dynamic instanceof Player) ((Player) dynamic).setGrounded(true);
                dynamic.getVelocity().setXY(dynamic.getVelocity().getX() * (1 - 0.03f * dtMod), oVel.getY() * 0.5f);
                if (toCollide instanceof Trampoline) {
                    dynamic.getVelocity().addXY(0, -30);
                    if(dynamic instanceof Player) ((Player) dynamic).setGrounded(false);
                }
            } else {
                pPos.addXY(0, overlapY);
                dynamic.getVelocity().setXY(dynamic.getVelocity().getX(), 0);
            }
        }

        if (toCollide instanceof Checkpoint && dynamic instanceof Player)
            ((Player) dynamic).setSpawnPoint(new Vector2D(oPos.getX() + (oDim.getX() / 2) - (pDim.getX() / 2), oPos.getY() - pDim.getY()));
    }

    private void handleDymamicOnDynamic(GameObject dynamic, GameObject toCollide, float dtMod) {
        Vector2D pPos = dynamic.getPos();
        Vector2D pDim = dynamic.getDim();
        Vector2D pVel = dynamic.getVelocity();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();
        Vector2D oVel = toCollide.getVelocity();

        boolean isBoundingBoxColliding = pPos.getX() + pVel.getX() < oPos.getX() + oDim.getX() + oVel.getX() &&
                pPos.getX() + pDim.getX() + pVel.getX() > oPos.getX() + oVel.getX() &&
                pPos.getY() + pVel.getY() < oPos.getY() + oDim.getY() + oVel.getY() &&
                pPos.getY() + pDim.getY() + pVel.getY() > oPos.getY() + oVel.getY();

        if (!isBoundingBoxColliding) return;

        double overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        double overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

        if (overlapX < overlapY) {
            if (pPos.getX() < oPos.getX()) {
                pPos.addXY(-overlapX/2, 0);
                oPos.addXY(overlapX/2, 0);
                pVel.setX(oVel.getX());
                oVel.setX(pVel.getX() );
            } else {
                pPos.addXY(overlapX/2, 0);
                oPos.addXY(-overlapX/2, 0);
                pVel.setX(oVel.getX());
                oVel.setX(pVel.getX());
            }
        } else {
            if (pPos.getY() < oPos.getY()) {
                pPos.addXY(0, -overlapY/2);
                oPos.addXY(0, overlapY/2);
                if(dynamic instanceof Player) ((Player) dynamic).setGrounded(true);
                dynamic.getVelocity().setXY(dynamic.getVelocity().getX() * (1 - 0.03f * dtMod), 0);
            } else {
                pPos.addXY(0, overlapY/2);
                oPos.addXY(0, -overlapY/2);
                pVel.setY(0);
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font font = new Font("Courier New", Font.PLAIN, 18);
        if (this.player != null && this.gameObjects != null) {
            renderGameObjects(g);
            renderMessageSystem(g, font);
            //System.out.println("Yessssriiiiiiiiasdfasdfa");
            g.setColor(Color.WHITE);
            g.drawString("FPS" + ((round(fps) > 144) ? ">144" : round(fps)), 10, 20);
        } else {
            renderHomeScreen(g);
            ///ystem.out.println("Getting no where" + System.currentTimeMillis());
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

        g.setColor(new Color(128, 128, 128, 100));
        g.fillRect(0, getHeight() - windowHeight, windowWidth, windowHeight);

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
            if(id != -1 && this.gameObjects[id - 1] != null) {
                g.setColor(this.gameObjects[id - 1].getColor());
            }else {
                g.setColor(Color.BLACK);
            }

            g.drawString(lines.get(i).substring(1), 10, getHeight() - 10 - ((lines.size() - i) * 20));
        }
    }

    private void renderGameObjects(Graphics g) {
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
            g.fillRect((int) topLeft.getX(), (int) topLeft.getY(), (int) (botRight.getX() - topLeft.getX()), (int) (botRight.getY() - topLeft.getY()));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressed[e.getKeyCode()] = true;

        char c = e.getKeyChar();

        if(typingMessage && this.player != null) {
           if (c == '\n') {
                String toAdd = "Player" + (this.player.getId() + 1) + ":" + messagingInput;
                this.messageHistory.add(toAdd);
                this.player.setCommunication(toAdd);
                messagingInput = "";
                typingMessage = false;
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

        mouseDragOffset.setXY((e.getX() - (double) getWidth() / 2), (e.getY() - (double) getHeight() / 2));
        mouseDragOffset.divide(zoomFactor);
        if(this.player.getGodMode()) return;
        if(mouseDragOffset.getX() > 1000) mouseDragOffset.setX(1000);
        if(mouseDragOffset.getX() < -1000) mouseDragOffset.setX(-1000);
        if(mouseDragOffset.getY() > 1000) mouseDragOffset.setY(600);
        if(mouseDragOffset.getY() < -1000) mouseDragOffset.setY(-600);
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
