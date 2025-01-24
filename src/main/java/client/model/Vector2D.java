package client.model;

import java.io.Serializable;

public class Vector2D implements Serializable {
    private float x, y;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

    public void add(Vector2D other) {
        this.x += other.getX();
        this.y += other.getY();
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public void normalize() {
        float len = length();
        if (len != 0) {
            this.x /= len;
            this.y /= len;
        }
    }

    public void scale(float scale) {
        this.x *= scale;
        this.y *= scale;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void addXY(float addX, float addY) {
        this.x += addX;
        this.y += addY;
    }

    public void set(Vector2D toSet) {
        this.x = toSet.getX();
        this.y = toSet.getY();
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

