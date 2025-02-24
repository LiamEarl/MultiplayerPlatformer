package client.objects;

import client.utility.Vector2D;

import java.awt.*;

public interface GameObject {
    Vector2D getDim();
    Vector2D getPos();
    Color getColor();
    Vector2D getVelocity();
    void update(float dtMod, long currentTime);
}
