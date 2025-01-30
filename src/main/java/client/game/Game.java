package client.game;
import client.model.*;
import client.model.Box;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.lang.Math.abs;

public class Game extends JPanel implements KeyListener {

    Player player;
    boolean[] keyPressed = new boolean[256];
    GameObject[] gameObjects;
    private final int WINDOW_WIDTH = 1300;
    private final int WINDOW_HEIGHT = 800;
    private boolean godMode = false;

    Game(Player player, GameObject[] gameObjects) {
        this.player = player;
        this.gameObjects = gameObjects;

        JFrame frame = new JFrame("Liam's Platformer Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(true);
        frame.addKeyListener(this);
        frame.add(this);
        frame.setVisible(true);

        createGameObjects();
    }

    void renderScene() {
        repaint();
    }

    void handleKeyInputs() {
        if (this.keyPressed[KeyEvent.VK_BACK_SLASH] && this.keyPressed[KeyEvent.VK_SHIFT]) {
            godMode = true;
        }else if (this.keyPressed[KeyEvent.VK_SLASH]) {
            godMode = false;
        }

        if(!godMode) {
            if (this.keyPressed[KeyEvent.VK_LEFT]) {
                this.player.getVelocity().addXY(-player.getSpeed(), 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT]) {
                this.player.getVelocity().addXY(player.getSpeed(), 0);
            }
            if (this.keyPressed[KeyEvent.VK_UP] && player.getGrounded()) {
                this.player.getVelocity().setY(-17);
            }
        } else {
            this.player.setVel(new Vector2D(0, 0));
            if (this.keyPressed[KeyEvent.VK_LEFT]) {
                this.player.getPos().addXY(-25, 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT]) {
                this.player.getPos().addXY(25, 0);
            }
            if (this.keyPressed[KeyEvent.VK_UP]) {
                this.player.getPos().addXY(0, -25);
            }
            if (this.keyPressed[KeyEvent.VK_DOWN]) {
                this.player.getPos().addXY(0, 25);
            }
        }
    }

    void createGameObjects() {
        Color drabWallColor = new Color(30, 20, 90);
        Color brightWallColor = new Color(255, 213, 0);
        Color checkpointColor = new Color(4, 126, 220);
        Color deathBoxColor = new Color(239, 26, 26);
        gameObjects[9]  = new Box(-100, 800, 600, 1000, drabWallColor, "#~#");
        gameObjects[10] = new Box(-1000, -500, 1050, 4000, drabWallColor, "#~#");
        gameObjects[11] = new Box(700, 700, 310, 4000, drabWallColor, "#~#");
        gameObjects[12] = new Box(1700, 700, 575, 4000, drabWallColor, "#~#");
        gameObjects[13] = new Box(1000, 400, 10, 4000, drabWallColor, "#~#");
        gameObjects[14] = new Box(990, 550, 10, 10, drabWallColor, "#~#");

        gameObjects[15] = new Box(3000, 600, 100, 50, drabWallColor, "+400picf0.125~#");
       // gameObjects[16] = new Box(3250, 524, 5, 5, brightWallColor, "#~#");
        gameObjects[17] = new Box(3600, 525, 375, 3000, drabWallColor, "#~#");
        gameObjects[18] = new Checkpoint(3750f, 515, 45, 10, checkpointColor);
        gameObjects[19] = new Box(4140, -700, 60, 3000, drabWallColor, "#~#");
        gameObjects[20] = new Box(3650, -700, 60, 1000, drabWallColor, "#~#");
        gameObjects[21] = new Box(3925, 425, 50, 100, drabWallColor, "#~#");
        gameObjects[22] = new Box(3710, 290, 10, 10, drabWallColor, "#~#");
        gameObjects[23] = new Box(3950, 175, 20, 5, drabWallColor, "#~#");
        gameObjects[24] = new Box(3710, 30, 50, 10, drabWallColor, "#~#");
        gameObjects[25] = new Box(4050, -20, 150, 10, drabWallColor, "#~#");
        gameObjects[26] = new Box(300, -1000, 500, 50, drabWallColor, "#~+4000picn0.125");
        //gameObjects[26] = new Box(4050, -20, 150, 10, drabWallColor, "#~#");
        //gameObjects[27] = new Box(200, 400, 500, 50, drabWallColor, "+500pic0.125~#");
        //gameObjects[27] = new DeathBox(200, 700, 50, 50, deathBoxColor);
    }

    void updateGameObjects() {
        this.player.update();
        for(GameObject gameObject : this.gameObjects) {
            if(gameObject instanceof Player || gameObject == null) continue;
            gameObject.update();
        }
    }

    void checkPlayerCollisions() {
        if(godMode) return;

        if(player.getPos().getY() > 3000) player.respawn();

        for(GameObject obj : this.gameObjects) {
            if(obj != null && !(obj instanceof Player)) {
                handlePlayerCollision(this.player, obj);
            }
        }
    }

    private void handlePlayerCollision(Player player, GameObject toCollide) {
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

        float overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        float overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

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
                player.getVelocity().setXY(player.getVelocity().getX() * 0.97f, oVel.getY());

                pPos.setX(pPos.getX() + oVel.getX());
            } else {
                pPos.addXY(0, overlapY);
                player.getVelocity().setXY(player.getVelocity().getX(), oVel.getY());

            }
        }

        if(toCollide instanceof Checkpoint) player.setSpawnPoint(new Vector2D(oPos.getX() + (oDim.getX() / 2) - (pDim.getX() / 2), oPos.getY() - pDim.getY()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Vector2D cameraOffset = new Vector2D(((float) getWidth() / 2 - this.player.getPos().getX() - (this.player.getDim().getX() / 2)), ((float) getHeight() / 2 - this.player.getPos().getY() - (this.player.getDim().getY() / 2)));
        // Draw the background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for(GameObject r : this.gameObjects) {

            if(r == null) continue;

            int objX = (int) (r.getPos().getX() + cameraOffset.getX());
            int objY = (int) (r.getPos().getY() + cameraOffset.getY());
            int objW = (int) r.getDim().getX();
            int objH = (int) r.getDim().getY();

            if(objX > getWidth() || objX + objW < 0 || objY > getHeight() || objY + objH < 0) continue;

            // Draw the rectangle
            g.setColor(r.getColor());
            g.fillRect(objX, objY, objW, objH);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressed[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyPressed[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) { }
}
