package client.game;
import client.model.Obstacle;
import client.model.Player;
import client.model.GameObject;
import client.model.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
                this.player.getVel().addXY(-player.getSpeed(), 0);
            }
            if (this.keyPressed[KeyEvent.VK_RIGHT]) {
                this.player.getVel().addXY(player.getSpeed(), 0);
            }
            if (this.keyPressed[KeyEvent.VK_UP] && player.getGrounded()) {
                this.player.getVel().setY(-17);
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

    void checkPlayerCollisions() {
        if(godMode) return;

        if(player.getPos().getY() > 3000) {
            player.getPos().setXY(100, 700);
            player.getVel().setXY(0, 0);
        }

        for(GameObject obj : this.gameObjects) {
            if(obj != null && obj instanceof Obstacle) {
                handlePlayerCollision(this.player, (Obstacle) obj);
            }
        }
    }

    private void handlePlayerCollision(Player player, Obstacle toCollide) {
        Vector2D pPos = player.getPos();
        Vector2D pDim = player.getDim();
        Vector2D pVel = player.getVel();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();

        boolean isBoundingBoxColliding = pPos.getX() + pVel.getX() < oPos.getX() + oDim.getX() &&
                pPos.getX() + pDim.getX() + pVel.getX() > oPos.getX() &&
                pPos.getY() + pVel.getY() < oPos.getY() + oDim.getY() &&
                pPos.getY() + pDim.getY() + pVel.getY() > oPos.getY();

        if(!isBoundingBoxColliding) return;

        float overlapX = Math.min(pPos.getX() + pDim.getX(), oPos.getX() + oDim.getX()) - Math.max(pPos.getX(), oPos.getX());
        float overlapY = Math.min(pPos.getY() + pDim.getY(), oPos.getY() + oDim.getY()) - Math.max(pPos.getY(), oPos.getY());

        if (overlapX < overlapY) {
            if (pPos.getX() < oPos.getX()) {
                pPos.addXY(-overlapX, 0);
                if (pPos.getX() + pDim.getX() <= oPos.getX()) {
                    player.getVel().setXY(0, player.getVel().getY());
                }

            } else {
                pPos.addXY(overlapX, 0);
                if (pPos.getX() >= oPos.getX() + oDim.getX()) {
                    player.getVel().setXY(0, player.getVel().getY());
                }
            }

        } else {
            if (pPos.getY() < oPos.getY()) {
                pPos.addXY(0, -overlapY);
                player.setGrounded(true);
                player.getVel().setXY(player.getVel().getX() * 0.97f, 0);
            } else {
                pPos.addXY(0, overlapY);
                //if(pPos.getY() > oPos.getY()+oDim.getY()) {
                    player.getVel().setXY(player.getVel().getX(), 0f);
                //}
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Vector2D cameraOffset = new Vector2D((getWidth() / 2 - this.player.getPos().getX() - (this.player.getDim().getX() / 2)), (getHeight() / 2 - this.player.getPos().getY() - (this.player.getDim().getY() / 2)));
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
