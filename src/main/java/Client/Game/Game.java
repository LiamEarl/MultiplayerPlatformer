package Client.Game;
import Client.PhysicalObjects.Player;
import Client.PhysicalObjects.Renderable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Game extends JPanel implements KeyListener {
    Player player;
    boolean[] keyPressed = new boolean[256];
    ArrayList<Renderable> toRender;

    Game(Player player, ArrayList<Renderable> toRender) {
        this.player = player;
        this.toRender = toRender;

        JFrame frame = new JFrame("Liam's Platformer Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
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
        if(this.keyPressed[KeyEvent.VK_UP]) {
            this.player.getVel().addXY(0, -player.getSpeed());
        }
        if(this.keyPressed[KeyEvent.VK_DOWN]) {
            this.player.getVel().addXY(0, player.getSpeed());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        for(Renderable r : this.toRender) {
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
