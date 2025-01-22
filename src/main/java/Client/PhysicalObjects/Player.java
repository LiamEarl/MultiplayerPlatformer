package Client.PhysicalObjects;

import java.awt.*;

public class Player implements Renderable {
    private Vector2D pos;
    private Vector2D vel;
    private Vector2D dim;
    private Color col;
    private float speed;

    public Player(float x, float y, Color color) {
        this.pos = new Vector2D(x, y);
        this.vel = new Vector2D(0, 0);
        this.dim = new Vector2D(20, 20);
        this.speed = 0.5f;
        this.col = color;
    }

    public void update() {
        this.pos.setXY(this.pos.getX() + this.vel.getX(), this.pos.getY() + this.vel.getY());
        this.vel.scale(0.95f);
    }
    public Color getColor() {
        return this.col;
    }

    @Override
    public Vector2D getDim() {
        return dim;
    }

    public Vector2D getVel() {
        return vel;
    }

    public void setVel(Vector2D vel) {
        this.vel = vel;
    }

    @Override
    public Vector2D getPos() {
        return pos;
    }

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
