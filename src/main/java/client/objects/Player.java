package client.objects;

import client.utility.Vector2D;

import java.awt.*;
import java.io.Serializable;

public class Player implements GameObject, Serializable {
    private Vector2D pos;
    private Vector2D vel, dim, spawnPoint, lastPos;
    private Color col;
    private float speed;
    private int id;
    private boolean grounded = false;
    private boolean god = false;
    private String communication;

    public Player(Vector2D position, Color color, Vector2D dimensions, int id) {
        this.pos = position;
        this.vel = new Vector2D(0, 0);
        this.dim = dimensions;
        this.speed = 0.5f;
        this.id = id;
        this.col = color;
        this.spawnPoint = new Vector2D(pos.getX(), pos.getY());
        this.god = false;
        this.communication = "";
    }

    @Override
    public void update(float dtMod, long currentTime) {
        if(god) return;
        this.pos.setXY(this.pos.getX() + (this.vel.getX() * dtMod), this.pos.getY() + (this.vel.getY() * dtMod));
        this.vel.scale(1f - (0.02f * dtMod));
        this.vel.addXY(0, 0.7f * dtMod);
        this.grounded = false;
    }
    @Override
    public boolean equals(Object p) {
        if(!(p instanceof Player)) return false;
        Player player = (Player) p;
        if(player.getId() == this.getId()) return true;
        return false;
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
    public void setColor(Color newCol) {
        this.col = newCol;
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
    public void setGodMode(boolean toSet) {this.god = toSet;}
    public boolean getGodMode() {return this.god;}
    public String getCommunication() {return communication;}
    public void setCommunication(String communication) {this.communication = communication;}
}
