package client.objects;

import client.utility.Vector2D;

import java.awt.*;

/**
 * Interface establishing the requirements for a GameObject
 * Useful for storing objects of the same general type and looping through them
 */
public interface GameObject {
    Vector2D getDim();
    Vector2D getPos();
    Color getColor();
    Vector2D getVelocity();
    void update(float dtMod, long currentTime);
}
