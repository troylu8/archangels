package src.shapes;

import java.awt.Rectangle;

public class DoubleRect extends Rectangle {
    private double x;
    private double y;
    private double width;
    private double height;

    public DoubleRect(double x, double y, double width, double height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }

    public double getX() { return x; }
    public void setX(double x) {
        this.x = x;
        super.x = (int) x;
    }

    public double getY() { return y; }
    public void setY(double y) {
        this.y = y;
        super.y = (int) y;
    }

    public double getWidth() { return width; }
    public void setWidth(double width) {
        this.width = width;
        super.width = (int) width;
    }

    public double getHeight() { return height; }
    public void setHeight(double height) {
        this.height = height;
        super.height = (int) height;
    }

    public void transform(double x, double y) {
        setX(this.x + x);
        setY(this.y + y);
    }
}