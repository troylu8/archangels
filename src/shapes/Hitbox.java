package src.shapes;

import java.awt.Graphics;

import src.util.Util;

public abstract class Hitbox {
    public abstract boolean collidesWithCircle(Circle circle);
    
    public abstract void draw(Graphics g);

    public abstract void transform(double x, double y);
    public abstract void rotate(double originX, double originY, double radiansCCW);

    /** return true if x is between a and b, inclusive */
    public static boolean between(double a, double b, double x) {
        return Math.min(a,b) <= x && x <= Math.max(a,b);
    }

    public static boolean lineTouchesCircle(double x1, double y1, double x2, double y2, Circle c) {
        double slope;

        if (x1 == x2) slope = Double.POSITIVE_INFINITY;
        else          slope = (y2 - y1) / (x2 - x1);
        
        // check if ends are in circle
        if (Util.dist(x1, y1, c.x, c.y) <= c.radius || Util.dist(x2, y2, c.x, c.y) <= c.radius) 
            return true;

        // check if vertical line crosses circle in case ends are not inside
        if (slope == Double.POSITIVE_INFINITY)
            return  (Math.abs(c.x - x1) <= c.radius) && between(y1, y2, c.y);

        // (closestX, closestY) is the point on line closest to circle center
        double closestX = ( (-(slope * slope) * x1) + (slope * y1) - (slope * c.y) - c.x) / (-(slope * slope) - 1);

        // if closestX isnt in the line segment, return false
        if (!between(x1, x2, closestX)) 
            return false;

        double closestY = slope * (closestX - x1) + y1;

        // return true if (closestX, closestY) is close enough to circle center
        return Util.dist(c.x, c.y, closestX, closestY) <= c.radius;
    }

    abstract Hitbox deepCopy();
    abstract void forEachPoint(ModifyPoint modifyPoint);
}