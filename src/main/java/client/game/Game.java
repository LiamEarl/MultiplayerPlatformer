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
    private final int WINDOW_WIDTH = 1200;
    private final int WINDOW_HEIGHT = 1000;


    Game(Player player, GameObject[] gameObjects) {
        this.player = player;
        this.gameObjects = gameObjects;

        JFrame frame = new JFrame("Liam's Platformer Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.addKeyListener(this);
        frame.add(this);
        frame.setVisible(true);
    }

    void renderScene() {
        repaint();
    }

    void handleKeyInputs() {
        if(this.keyPressed[KeyEvent.VK_LEFT]) {
            this.player.getVel().addXY(-player.getSpeed(), 0);
        }
        if(this.keyPressed[KeyEvent.VK_RIGHT]) {
            this.player.getVel().addXY(player.getSpeed(), 0);
        }
        if(this.keyPressed[KeyEvent.VK_UP] && player.getGrounded()) {
            this.player.getVel().setY(-50);
        }
        if(this.keyPressed[KeyEvent.VK_DOWN]) {
            this.player.getVel().addXY(0, player.getSpeed());
        }
    }

    void checkPlayerCollisions() {
        if(player.getPos().getY() > WINDOW_HEIGHT) {
            player.getPos().setXY(100, 700);
            player.getVel().setXY(0, 0);
        }

        for(GameObject obj : this.gameObjects) {
            if(obj != null && obj instanceof Obstacle) {
                handleEntityCollision(this.player, (Obstacle) obj);
            }
        }
    }

    private void handleEntityCollision(Player entity, Obstacle toCollide) {
        Vector2D ePos = entity.getPos();
        Vector2D eDim = entity.getDim();
        Vector2D oPos = toCollide.getPos();
        Vector2D oDim = toCollide.getDim();

        boolean isColliding = ePos.getX() < oPos.getX() + oDim.getX() &&
                ePos.getX() + eDim.getX() > oPos.getX() &&
                ePos.getY() < oPos.getY() + oDim.getY() &&
                ePos.getY() + eDim.getY() > oPos.getY();

        if(!isColliding) return;

        float overlapX = Math.min(ePos.getX() + eDim.getX(), oPos.getX() + oDim.getX()) - Math.max(ePos.getX(), oPos.getX());
        float overlapY = Math.min(ePos.getY() + eDim.getY(), oPos.getY() + oDim.getY()) - Math.max(ePos.getY(), oPos.getY());

        if (overlapX < overlapY) {
            if (ePos.getX() < oPos.getX()) {
                ePos.addXY(-overlapX, 0);
                if (ePos.getX() + eDim.getX() <= oPos.getX()) {
                    entity.getVel().setXY(0, entity.getVel().getY());
                }

            } else {
                ePos.addXY(overlapX, 0);
                if (ePos.getX() >= oPos.getX() + oDim.getX()) {
                    entity.getVel().setXY(0, entity.getVel().getY());
                }
            }

        } else {
            if (ePos.getY() < oPos.getY()) {
                ePos.addXY(0, -overlapY);
                entity.setGrounded(true);
                entity.getVel().setXY(entity.getVel().getX() * 0.85f, 0);
            } else {
                ePos.addXY(0, overlapY);
                if(ePos.getY() > oPos.getY()+oDim.getY()) {
                    entity.getVel().setXY(entity.getVel().getX(), 0);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for(GameObject r : this.gameObjects) {
            if(r == null) continue;
            // Draw the rectangle
            g.setColor(r.getColor());
            g.fillRect((int) r.getPos().getX(), (int) r.getPos().getY(), (int) r.getDim().getX(), (int) r.getDim().getY());
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
