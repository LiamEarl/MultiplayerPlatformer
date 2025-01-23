package client.model;

import java.awt.*;

public class Obstacle implements GameObject {
    private Vector2D pos;
    private Vector2D dim;
    private Color col;
    public Obstacle(float x, float y, float width, float height, Color color) {
        this.pos = new Vector2D(x, y);
        this.dim = new Vector2D(width, height);
        this.col = color;
    }

    public Color getColor() {
        return this.col;
    }

    @Override
    public Vector2D getDim() {
        return dim;
    }

    @Override
    public Vector2D getPos() {
        return pos;
    }

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }

}
