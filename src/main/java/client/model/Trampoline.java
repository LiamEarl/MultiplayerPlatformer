package client.model;

import java.awt.*;

public class Trampoline implements GameObject {
    private Vector2D pos;
    private Vector2D dim;
    private Color col;

    public Trampoline(float x, float y, float width, float height, Color color) {
        this.pos = new Vector2D(x, y);
        this.dim = new Vector2D(width, height);
        this.col = color;
    }
    @Override
    public Vector2D getVelocity() {return new Vector2D(0, 0);}
    @Override
    public Vector2D getDim() {
        return dim;
    }
    @Override
    public void update(float dtMod, long currentTime) {};
    @Override
    public Vector2D getPos() {
        return pos;
    }
    public Color getColor() {
        return this.col;
    }
    public void setPos(Vector2D pos) {
        this.pos = pos;
    }
}