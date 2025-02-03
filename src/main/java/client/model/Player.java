package client.model;

import java.awt.*;
import java.io.Serializable;

public class Player implements GameObject, Serializable {
    private Vector2D pos;
    private Vector2D vel, dim, spawnPoint, lastPos;
    private Color col;
    private float speed;
    private int id;
    private boolean grounded = false;

    public Player(Vector2D position, Color color, Vector2D dimensions, int id) {
        this.pos = position;
        this.vel = new Vector2D(0, 0);
        this.dim = dimensions;
        this.speed = 0.5f;
        this.id = id;
        this.col = color;
        this.spawnPoint = new Vector2D(pos.getX(), pos.getY());
    }

    @Override
    public void update(float dtMod) {
        this.pos.setXY(this.pos.getX() + (this.vel.getX() * dtMod), this.pos.getY() + (this.vel.getY() * dtMod));
        this.vel.scale(1f - (0.02f * dtMod) );
        this.vel.addXY(0, 0.7f * dtMod);
        this.grounded = false;
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
    public int getId() {return this.id;}
    public Color getColor() {
        return this.col;
    }
    public void setVel(Vector2D vel) {
        this.vel.setXY(vel.getX(), vel.getY());
    }
    public void setPos(Vector2D pos) {this.pos.set(pos); }
    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    public void setGrounded(boolean state) {
        this.grounded = state;
    }
    public boolean getGrounded() {
        return this.grounded;
    }
    public void respawn() {this.setPos(this.spawnPoint); this.getVelocity().setXY(0, 0);}
    public Vector2D getSpawnPoint() {return this.spawnPoint; }
    public void setSpawnPoint(Vector2D spawnPoint) {this.spawnPoint = spawnPoint;}
}
