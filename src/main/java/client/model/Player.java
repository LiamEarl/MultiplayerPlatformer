package client.model;

import java.awt.*;

public class Player implements GameObject {
    private EntityData pData;
    private Vector2D vel, dim;
    private Color col;
    private float speed;
    private boolean grounded = false;


    public Player(EntityData entityData, Color color, Vector2D dimensions) {
        this.pData = entityData;
        this.vel = new Vector2D(0, 0);
        this.dim = dimensions;
        this.speed = 0.5f;
        this.col = color;
    }

    public void update() {
        this.pData.getPos().setXY(this.pData.getPos().getX() + (this.vel.getX()), this.pData.getPos().getY() + (this.vel.getY()));
        this.vel.scale(1f - (0.02f));
        this.vel.addXY(0, 0.7f);
        this.grounded = false;
    }

    @Override
    public Vector2D getDim() {
        return dim;
    }
    @Override
    public Vector2D getPos() {
        return this.pData.getPos();
    }
    public EntityData getPlayerData() {return this.pData; }
    public void setPlayerData(EntityData newData) {this.pData = newData; }
    public Color getColor() {
        return this.col;
    }
    public Vector2D getVel() {
        return vel;
    }
    public void setVel(Vector2D vel) {
        this.vel = vel;
    }
    public void setPos(Vector2D pos) {this.pData.getPos().set(pos); }
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
}
