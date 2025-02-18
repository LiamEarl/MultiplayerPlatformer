package client.model;

import java.awt.*;

public class Trampoline extends Box implements GameObject {
    public Trampoline(float x, float y, float width, float height, Color color) {
        super(x, y, width, height, color, "#~#");
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
}