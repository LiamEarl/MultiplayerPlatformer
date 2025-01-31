package client.model;

import java.awt.*;

public class Box implements GameObject {
    private Vector2D pos;
    private Vector2D originalPos;
    private Vector2D dim;
    private Color col;
    private String[] equations;
    private Vector2D lastPos;

    public Box(float x, float y, float width, float height, Color color, String equations) {
        this.pos = new Vector2D(x, y);
        this.originalPos = new Vector2D(x, y);
        this.dim = new Vector2D(width, height);
        this.col = color;
        this.lastPos = new Vector2D(x, y);
        this.equations = equations.split("~");
    }
    public Vector2D getVelocity() {
        if(this.equations[0].equals("#") && this.equations[1].equals("#")) return new Vector2D(0, 0);
        Vector2D velocity = new Vector2D(this.pos.getX(), this.pos.getY());
        velocity.subtract(this.lastPos);
        return velocity;
    }
    public Color getColor() {return this.col; }
    @Override
    public Vector2D getDim() {
        return dim;
    }
    @Override
    public Vector2D getPos() {
        return pos;
    }
    public void setPos(Vector2D pos) {
        this.lastPos.setXY(this.pos.getX(), this.pos.getY());
        this.pos = pos;
    }
    @Override
    public void update() {
        this.lastPos.setXY(this.pos.getX(), this.pos.getY());
        //System.out.println(MathParser.performCalculation(this.originalPos.getX(), this.equations[0]));
        this.pos.setX(MathParser.performCalculation(this.originalPos.getX(), this.equations[0]));
        this.pos.setY(MathParser.performCalculation(this.originalPos.getY(), this.equations[1]));
    };
}
