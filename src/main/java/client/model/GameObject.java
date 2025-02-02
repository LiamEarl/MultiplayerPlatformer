package client.model;

import java.awt.*;

public interface GameObject {
    Vector2D getDim();
    Vector2D getPos();
    Color getColor();
    Vector2D getVelocity();
    void update(float dtMod);
}
