package client.objects;
import client.utility.Vector2D;
import java.awt.*;

/**
 * A Tip object kind of like a transparent Box, but the result of a collision with a player is that it
 * displays it's message to the window.
 */
public class Tip implements GameObject {
    private Vector2D pos, dim; // Position of the tip object
    private String tipContent; // String containing what the tip says

    /**
     * Creates a new Tip Object
     * @param pos position of the Tip
     * @param dim dimensions of the Tip
     * @param tipContent string content of the Tip
     */
    public Tip(Vector2D pos, Vector2D dim, String tipContent) {
        this.pos = pos;
        this.dim = dim;
        this.tipContent = tipContent;
    }
    public String getTipContent() {return this.tipContent;}
    @Override
    public Vector2D getDim() {
        return dim;
    }
    @Override
    public Vector2D getPos() {
        return pos;
    }
    @Override
    public Color getColor() {return new Color(0, 0, 0, 0);}
    @Override
    public Vector2D getVelocity() {return new Vector2D(0, 0);}
    @Override
    public void update(float dtMod, long currentTime) {};
}
