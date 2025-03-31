package client.objects;

import client.utility.OperationEvaluator;
import client.utility.Vector2D;

import java.awt.*;

public class Box implements GameObject {
    private final Vector2D pos; // Position of the box
    private final Vector2D originalPos; // Original position of the box, used for calculating dynamic positions
    private final Vector2D lastPos; // Last position of the box, used for calculating the velocity
    private final Vector2D dim; // Dimensions of the box
    private final Color col; // Color of the box
    private final String[] equations; // Equation handling the box's movement.
    private final BoxType type; // Type of the box

    /**
     * Creates a box object that can be anything under the BoxType enum
     * @param position position of the box.
     * @param dimensions dimensions of the box.
     * @param color color of the box.
     * @param type type of the box.
     * @param equations Equation handling the box's movement.
     */
    public Box(Vector2D position, Vector2D dimensions, Color color, BoxType type, String equations) {
        this.pos = position.copy();
        this.originalPos = position.copy();
        this.lastPos = position.copy();
        this.dim = dimensions.copy();
        this.col = color;
        this.type = type;
        this.equations = equations.split("~"); // Get the x and y equation separated by "~"
    }

    public Vector2D getVelocity() { // Uses the lastPosition to get the current velocity
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
    public void update(float dtMod, long currentTime) { // Calculates the new position based on the box's equation
        this.lastPos.setXY(this.pos.getX(), this.pos.getY());
        this.pos.setX(OperationEvaluator.performCalculation(this.originalPos.getX(), this.equations[0], currentTime));
        this.pos.setY(OperationEvaluator.performCalculation(this.originalPos.getY(), this.equations[1], currentTime));
    }
}
