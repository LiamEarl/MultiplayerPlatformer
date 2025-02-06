package client.model;

import java.awt.*;

public class DeathBox implements GameObject {
    private Vector2D pos;
    private Vector2D dim;
    private Color col;
    private Vector2D originalPos;
    private String[] equations;

    public DeathBox(float x, float y, float width, float height, Color color, String equations) {
        this.pos = new Vector2D(x, y);
        this.dim = new Vector2D(width, height);
        this.originalPos = new Vector2D(x, y);
        this.equations = equations.split("~");
        this.col = color;
    }
    @Override
    public Vector2D getVelocity() {return new Vector2D(0, 0);}
    @Override
    public Vector2D getDim() {
        return dim;
    }
    @Override
    public Vector2D getPos() {
        return pos;
    }
    @Override
    public void update(float dtMod, long currentTime) {
        this.pos.setX(MathParser.performCalculation(this.originalPos.getX(), this.equations[0], currentTime));
        this.pos.setY(MathParser.performCalculation(this.originalPos.getY(), this.equations[1], currentTime));
    };
    public void setPos(Vector2D pos) {
        this.pos = pos;
    }
    public Color getColor() {
        return this.col;
    }
}