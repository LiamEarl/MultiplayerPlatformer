package client.model;

import java.awt.*;

public class Ice extends Box implements GameObject {
    public Ice(float x, float y, float width, float height, Color color, String equations) {
        super(x, y, width, height, color, equations);
    }
    @Override
    public Vector2D getVelocity() {return new Vector2D(0, 0);}
    @Override
    public Vector2D getDim() {
        return this.dim;
    }
    @Override
    public void update(float dtMod, long currentTime) {super.update(dtMod, currentTime);};
    @Override
    public Vector2D getPos() {
        return this.pos;
    }
}