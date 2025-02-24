package client.objects;

import client.utility.OperationEvaluator;
import client.utility.Vector2D;

import java.awt.*;

public class Box implements GameObject {
    private final Vector2D pos;
    private final Vector2D originalPos;
    private final Vector2D lastPos;
    private final Vector2D dim;
    private final Color col;
    private final String[] equations;
    private final BoxType type;

    public Box(Vector2D position, Vector2D dimensions, Color color, BoxType type, String equations) {
        this.pos = position.copy();
        this.originalPos = position.copy();
        this.lastPos = position.copy();
        this.dim = dimensions.copy();
        this.col = color;
        this.type = type;
        this.equations = equations.split("~");
    }

    public Vector2D getVelocity() {
        if(this.equations[0].equals("#") && this.equations[1].equals("#")) return new Vector2D(0, 0);
        Vector2D velocity = new Vector2D(this.pos.getX(), this.pos.getY());
        velocity.subtract(this.lastPos);
        return velocity;
    }
    public Color getColor() {return this.col; }
    public BoxType getType() {return this.type; }
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
        this.lastPos.setXY(this.pos.getX(), this.pos.getY());
        this.pos.setX(OperationEvaluator.performCalculation(this.originalPos.getX(), this.equations[0], currentTime));
        this.pos.setY(OperationEvaluator.performCalculation(this.originalPos.getY(), this.equations[1], currentTime));
    }
}
