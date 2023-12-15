package src.shapes;

import java.awt.*;

import src.draw.Canvas;
import src.util.Util;

public class Line extends Hitbox {
    public double x1;
    public double y1;
    public double x2;
    public double y2;
    double slope;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public boolean collidesWithCircle(Circle circle) {
        return lineTouchesCircle(x1, y1, x2, y2, circle);
    }

    @Override
    public void transform(double x, double y) {
        this.x1 += x;
        this.x2 += x;
        this.y1 += y;
        this.y2 += y;
    }

    @Override
    public void rotate(double originX, double originY, double radiansCCW) {
        double[] newP1 = Util.rotatePoint(x1, y1, originX, originY, radiansCCW);
        double[] newP2 = Util.rotatePoint(x2, y2, originX, originY, radiansCCW);

        x1 = newP1[0];
        y1 = newP1[1];
        x2 = newP2[0];
        y2 = newP2[1];
    }

    @Override
    public void draw(Graphics g) {
        int[] cameraPos1 = Canvas.getDrawPosWorld(x1 , y1);
        int[] cameraPos2 = Canvas.getDrawPosWorld(x2 , y2);
        g.drawLine(cameraPos1[0], cameraPos1[1], cameraPos2[0], cameraPos2[1]);
    }

    @Override
    public Hitbox deepCopy() { return new Line(x1,y1,x2,y2); }

    @Override
    public void forEachPoint(ModifyPoint modifyPoint) {
        double[] new1 = modifyPoint.modify(x1, y1);
        double[] new2 = modifyPoint.modify(x2, y2);
        x1 = new1[0];
        y1 = new1[1];
        x2 = new2[0];
        y2 = new2[1];
    }
    
}
