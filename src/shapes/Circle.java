package src.shapes;

import java.awt.*;

import src.draw.Canvas;
import src.util.Util;

public class Circle extends Hitbox {
    public double x;
    public double y;
    public int radius;

    public Circle(double x, double y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public boolean collidesWithCircle(Circle other) {
        return Util.dist(this.x, this.y, other.x, other.y) <= other.radius + this.radius;
    }

    @Override
    public void transform(double x, double y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public void rotate(double originX, double originY, double radiansCCW) {
        double[] newCenter = Util.rotatePoint(x, y, originX, originY, radiansCCW);
        x = newCenter[0];
        y = newCenter[1];
    }

    @Override
    public void draw(Graphics g) {
        int[] cameraPos = Canvas.getDrawPosWorld( x - radius , y - radius);
        g.drawOval(cameraPos[0], cameraPos[1], (int) (radius * 2 * Canvas.FOVratio), (int) (radius * 2 * Canvas.FOVratio));
    }

    @Override
    public Hitbox deepCopy() { return new Circle(x, y, radius); }

    @Override
    public void forEachPoint(ModifyPoint modifyPoint) {
        double[] newP = modifyPoint.modify(x, y);
        x = newP[0];
        y = newP[1];
    }
}
