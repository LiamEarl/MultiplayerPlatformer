package client.game;
import client.model.*;
import client.model.Box;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import static java.lang.Math.round;

public class Game extends JPanel implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {

    Player player;
    boolean[] keyPressed = new boolean[256];
    GameObject[] gameObjects;
    private final int WINDOW_WIDTH = 1300;
    private final int WINDOW_HEIGHT = 800;
    private double zoomFactor = 1f;
    private Vector2D mouseDragOffset = new Vector2D(0, 0);
    private float fps = 60;
    private boolean typing = false;
    private String currentInput = "";
    private ArrayList<String> messageHistory = new ArrayList<>();

    Game(Player player, GameObject[] gameObjects) {
        this.player = player;
        this.gameObjects = gameObjects;

        JFrame frame = new JFrame("Liam's Platformer Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(true);
        frame.addKeyListener(this);
        frame.addMouseWheelListener(this);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.add(this);

        createGameObjects();

        frame.setVisible(true);
    }

    void renderScene(float curFps) {
        fps = curFps;
        repaint();
    }

    void handleKeyInputs(float dtMod) {
        if(typing) return;

        if (this.keyPressed[KeyEvent.VK_BACK_SLASH] && this.keyPressed[KeyEvent.VK_SHIFT]) {
            this.player.setGodMode(true);
        }else if (this.keyPressed[KeyEvent.VK_SLASH]) {
            this.player.setGodMode(false);
        }

        if(!player.getGodMode()) {
            if (this.keyPressed[KeyEvent.VK_LEFT] || this.keyPressed[KeyEvent.VK_A]) {
                this.player.getVelocity().addXY(-player.getSpeed() * dtMod, 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT] || this.keyPressed[KeyEvent.VK_D]) {
                this.player.getVelocity().addXY(player.getSpeed() * dtMod, 0);
            }
            if ((this.keyPressed[KeyEvent.VK_UP] || this.keyPressed[KeyEvent.VK_W] || this.keyPressed[KeyEvent.VK_SPACE]) && player.getGrounded()) {
                this.player.getVelocity().addXY(0,-17);
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

    void createGameObjects() {
        ArrayList<GameObject> buffer = new ArrayList<>();

        Color drabWallColor = new Color(30, 20, 90);
        Color brightWallColor = new Color(255, 213, 0);
        Color checkpointColor = new Color(4, 126, 220);
        Color deathBoxColor = new Color(239, 26, 26);

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
        buffer.add(new DeathBox(3670, 900, 4140-3670, 4000, deathBoxColor, "#~#"));
        buffer.add(new Box(3925, 435, 50, 50, drabWallColor, "#~#"));
        buffer.add(new Box(3710, 290, 10, 10, drabWallColor, "#~#"));
        buffer.add(new Box(3950, 190, 20, 5, drabWallColor, "#~#"));
        buffer.add(new Box(3710, 45, 10, 10, drabWallColor, "#~#"));
        buffer.add(new Box(4100, 20, 40, 10, drabWallColor, "#~#"));
        buffer.add(new Box(2900, 600, 200, 50, drabWallColor, "+300picn0.125~#"));
        buffer.add(new Box(4141, -101, 3501, 5, drabWallColor, "#~#"));
        buffer.add(new Checkpoint(7400, -111, 100, 10, checkpointColor));
        buffer.add(new Box(4240, -700, 1400, 500, drabWallColor, "#~+100picn0.32"));
        buffer.add(new Box(4240+1600, -700, 1400, 500, drabWallColor, "#~+100picn0.32"));
        for(int i = 0; i < buffer.size(); i++) {
            this.gameObjects[i + 10] = buffer.get(i);
        }
    }

    void updateGameObjects(float dtMod, long currentTime) {
        for(GameObject gameObject : this.gameObjects) {
            if(gameObject == null) continue;
            if(gameObject instanceof Player) {
                Player playerObject = (Player) gameObject;
                playerObject.update(dtMod, currentTime);
                if(!playerObject.getCommunication().equals("") && playerObject.getId() != this.player.getId()) {
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
        for(GameObject obj : this.gameObjects) {
            if(obj != null && !(obj instanceof Player)) {
                for(int i = 0; i < 10; i++) {
                    if(!(this.gameObjects[i] instanceof Player)) continue;
                    Player currentPlayer = (Player) this.gameObjects[i];
                    if(currentPlayer == null) continue;
                    if(currentPlayer.getGodMode()) continue;
                    handlePlayerCollision(currentPlayer, obj, dtMod);
                    if(currentPlayer.getPos().getY() > 3000) currentPlayer.respawn();
                }
            }
        }
    }

    private void handlePlayerCollision(Player player, GameObject toCollide, float dtMod) {
        Vector2D pPos = player.getPos();
        Vector2D pDim = player.getDim();
        Vector2D pVel = player.getVelocity();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();
        Vector2D oVel = toCollide.getVelocity();

        if(toCollide instanceof DeathBox) {
            boolean isColliding = pPos.getX() < oPos.getX() + oDim.getX() &&
                                  pPos.getX() + pDim.getX() > oPos.getX() &&
                                  pPos.getY() < oPos.getY() + oDim.getY() &&
                                  pPos.getY() + pDim.getY() > oPos.getY();
            if(!isColliding) return;
            player.respawn();
            return;
        }

        boolean isBoundingBoxColliding = pPos.getX() + pVel.getX() < oPos.getX() + oDim.getX() + oVel.getX() &&
                                         pPos.getX() + pDim.getX() + pVel.getX() > oPos.getX() + oVel.getX() &&
                                         pPos.getY() + pVel.getY() < oPos.getY() + oDim.getY() + oVel.getY() &&
                                         pPos.getY() + pDim.getY() + pVel.getY() > oPos.getY() + oVel.getY();

        if(!isBoundingBoxColliding) return;

        double overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        double overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

        if(overlapX < overlapY) {
            if(pPos.getX() < oPos.getX()) {
                pPos.addXY(-overlapX, 0);
                if(pPos.getX() + pDim.getX() <= oPos.getX()) {
                    player.getVelocity().setXY(oVel.getX(), player.getVelocity().getY());
                }
            } else {
                pPos.addXY(overlapX, 0);
                if(pPos.getX() >= oPos.getX() + oDim.getX()) {
                    player.getVelocity().setXY(oVel.getX(), player.getVelocity().getY());
                }
            }
        } else {
            if(pPos.getY() < oPos.getY()) {
                pPos.addXY(0, -overlapY);
                player.setGrounded(true);
                player.getVelocity().setXY(player.getVelocity().getX() * (1 - 0.03f * dtMod), oVel.getY() * 0.9f);
            } else {
                pPos.addXY(0, overlapY);
                player.getVelocity().setXY(player.getVelocity().getX(), -player.getVelocity().getY());
            }
        }

        if(toCollide instanceof Checkpoint) player.setSpawnPoint(new Vector2D(oPos.getX() + (oDim.getX() / 2) - (pDim.getX() / 2), oPos.getY() - pDim.getY()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font font = new Font("Courier New", Font.PLAIN, 18);

        renderGameObjects(g);
        renderMessageSystem(g, font);

        g.setColor(Color.WHITE);
        g.drawString("FPS" + ((round(fps) > 144) ? ">144" : round(fps)), 10, 20);
    }

    private void renderMessageSystem(Graphics g, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        int windowWidth = 300;
        int windowHeight = 300;

        if(typing) {
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

        String ticker = (typing && (System.currentTimeMillis() % 1000 < 500) ? "_" : "");
        lines.add("");
        lines.add((this.player.getId() + 1) + "Send Message:" + (currentInput.isEmpty() ? ticker : ""));
        String typingText = currentInput + (currentInput.isEmpty() ? "" : ticker);

        for(int i = 0; i < typingText.length(); i += colsAvailable) {
            lines.add((this.player.getId() + 1) + typingText.substring(i, Math.min(i + colsAvailable, typingText.length())));
        }

        for(int i = lines.size() - 1; i >= Math.max(lines.size() - (1 + rowsAvailable), 0); i--) {
            if(lines.get(i).isEmpty()) continue;

            int id = Character.getNumericValue(lines.get(i).charAt(0));
            if(id != -1) g.setColor(this.gameObjects[id - 1].getColor());

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

        if(typing) {
           if (c == '\n') {
                String toAdd = "Player" + (this.player.getId() + 1) + ":" + currentInput;
                this.messageHistory.add(toAdd);
                this.player.setCommunication(toAdd);
                currentInput = "";
                typing = false;
            } else if (c == '\b' && !currentInput.isEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            } else if (isCharAllowed(c) && currentInput.length() < 500) {
                currentInput += c;
            }
        }else {
            if(c == '\n') typing = true;
        }
    }

    private boolean isCharAllowed(char toCheck) {
        if(Character.isLetterOrDigit(toCheck) || Character.isWhitespace(toCheck)) return true;
        String allowed = "!@#$%^&*()_+-=|}]{[':;'/>.<,~`?";
        for(char allowedChar : allowed.toCharArray()) {
            if(toCheck == allowedChar) return true;
        }
        return false;
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
        if(e.getX() < 200 && e.getY() > getHeight() - 100) {
            typing = true;
        }else {
            typing = false;
        }
        /*
        Vector2D cameraOffset = new Vector2D(
                -((double) getWidth() / 2 - this.player.getPos().getX() - (this.player.getDim().getX() / 2)) - 7.5,
                -((double) getHeight() / 2 - this.player.getPos().getY() - (this.player.getDim().getY() / 2)) - 30);
        System.out.println((e.getX() + cameraOffset.getX()) + ", " + (e.getY() + cameraOffset.getY()));
         */
    }
    @Override
    public void keyTyped(KeyEvent e) {}
}
