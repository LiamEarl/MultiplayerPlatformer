package client.utility;

import java.io.Serializable;

/**
 * Vector class that stores an x value and y value plus has some
 * methods to manipulate those two values. Pretty self-explanatory.
 */
public class Vector2D implements Serializable {
    private double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Vector2D(Vector2D v) {
        this.x = v.getX();
        this.y = v.getY();
    }

    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

    public void add(Vector2D other) {
        this.x += other.getX();
        this.y += other.getY();
    }
    public void subtract(Vector2D other) {
        this.x -= other.getX();
        this.y -= other.getY();
    }
    public void subtract(float sx, float sy) {
        this.x -= sx;
        this.y -= sy;
    }

    public double length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public void normalize() {
        double len = length();
        if (len != 0) {
            this.x /= len;
            this.y /= len;
        }
    }

    public void scale(double scale) {
        this.x *= scale;
        this.y *= scale;
    }

    public void divide(double toDivide) {
        this.x /= toDivide;
        this.y /= toDivide;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void addXY(double addX, double addY) {
        this.x += addX;
        this.y += addY;
    }

    public void set(Vector2D toSet) {
        this.x = toSet.getX();
        this.y = toSet.getY();
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

