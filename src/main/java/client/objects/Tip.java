package client.objects;
import client.utility.Vector2D;
import java.awt.*;

public class Tip implements GameObject {
    private Vector2D pos, dim;
    private String tipContent;
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
