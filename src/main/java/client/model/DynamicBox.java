package client.model;

import java.awt.*;
import java.io.Serializable;

public class DynamicBox implements GameObject, Serializable {
    private Vector2D pos,vel, dim;
    private Color col;
    private int id;
    public DynamicBox(Vector2D position, Color color, Vector2D dimensions, int id) {
        this.pos = position;
        this.vel = new Vector2D(0, 0);
        this.dim = dimensions;
        this.col = color;
    }

    @Override
    public void update(float dtMod, long currentTime) {
        this.pos.setXY(this.pos.getX() + (this.vel.getX() * dtMod), this.pos.getY() + (this.vel.getY() * dtMod));
        this.vel.scale(1f - (0.02f * dtMod));
        this.vel.addXY(0, 0.7f * dtMod);
    }
    @Override
    public Vector2D getVelocity() {
        return vel;
    }
    @Override
    public Vector2D getDim() {
        return dim;
    }
    @Override
    public Vector2D getPos() {
        return this.pos;
    }
    public Color getColor() {
        return this.col;
    }
    public void setColor(Color newCol) {
        this.col = newCol;
    }
    public void setVel(Vector2D vel) {
        this.vel.setXY(vel.getX(), vel.getY());
    }
    public void setPos(Vector2D pos) {this.pos.set(pos); }
}
