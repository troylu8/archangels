package src.shapes;

import java.awt.Graphics;
import java.awt.Rectangle;

import src.draw.Canvas;
import src.util.Util;

/** 
 * a rectangle that can rotate, expressed using both java.awt.Rectangle and a Polygon
 * <p>
 * faster than a polygon with 4 vertexes since it takes advantage of Rectangle.intersects() when possible
 * <p> 
 * mostly used for spriteBounds
*/
public class RectShape extends Polygon {

    private Rectangle rect;

    private double rotation;

    private double[] anchor;

    public RectShape(int x, int y, int width, int height, double anchorX, double anchorY) {
        rect = new Rectangle(x, y, width, height);
        rotation = 0;
        anchor = new double[] {anchorX, anchorY}; 
        
        vertexes = null;
    }
    public RectShape(double anchorX, double anchorY) {
        this(0,0,0,0, anchorX, anchorY);
    }

    public double getRotation() { return rotation; }
    public double[] getAnchor() { return anchor; }
    public int getX() { return rect.x; }
    public int getY() { return rect.y; }
    public int getWidth() { return rect.width; }
    public int getHeight() { return rect.height; }

    public void setAnchor(double anchorX, double anchorY) { 
        anchor = new double[] {anchorX, anchorY};
    }

    public void setSize(int width, int height) { 
        rect.width = width;
        rect.height = height;
        if (vertexes != null) updatePoly();
    }
    /** set size, then center around point */
    public void setSize(int width, int height, double centerX, double centerY) {
        rect.width = width;
        rect.height = height;
        centerSpriteAround(centerX, centerY);
        if (vertexes != null) updatePoly();
    }
    /** sets the corner position of rect to a point <p>
     * uses transform() to avoid rotating points again */
    public void setPosition(int x, int y) { 
        transform(x - rect.x, y - rect.y);
    }
    /** centers rect around point according to anchor */
    public void centerSpriteAround(double x, double y) {
        setPosition(
            (int) (x - rect.width * anchor[0]), 
            (int) (y - rect.height * anchor[1])
        );
        if (vertexes != null) updatePoly();
    }

    /** builds vertexes[][] using rect, rotation, and anchor */
    private void updatePoly() {
        vertexes = getPolyFromRect(rect);
        
        final double[] rotateOrigin = new double[] {
            rect.x + rect.width * anchor[0],
            rect.y + rect.height * anchor[1]
        };
        super.rotate(rotateOrigin[0], rotateOrigin[1], rotation);
    }

    @Override
    public void transform(double x, double y) {
        if (vertexes != null) super.transform(x, y);
        
        rect.x += x;
        rect.y += y;
    }

    @Override
    public boolean collidesWithCircle(Circle c) {
        if (vertexes != null)
            return super.collidesWithCircle(c);

        if (rect.contains(c.x, c.y))
            return true;

        

        if (c.x >= rect.x && c.x <= rect.x + rect.width) 
            return Math.abs(c.y - (rect.y + rect.height/2.0)) <= rect.height/2.0 + c.radius;
        
        if (c.y >= rect.y && c.y <= rect.y + rect.height) 
            return Math.abs(c.x - (rect.x + rect.width/2.0)) <= rect.width/2.0 + c.radius;


        for (double[] corner : RectShape.getPolyFromRect(rect)) {
            if (Util.dist(corner[0], corner[1], c.x, c.y) <= c.radius)
                return true;
        }

        return false;
    }

    @Override
    public void rotate(double originX, double originY, double radiansCW) {
        throw new UnsupportedOperationException("rectshapes can only rotate around their anchor");
    }

    public void setRotation(double radians) {
        rotation = radians % (2 * Math.PI);

        // upright or upside down
        if (rotation == 0 || rotation == Math.PI) vertexes = null;
        else               updatePoly();
    }

    public void rotate(double radiansCW) { setRotation(rotation += radiansCW); }

    /** returned vertexes start at upper left corner and go clockwise */
    private static double[][] getPolyFromRect(Rectangle r) {
        return new double[][] {
            {r.x, r.y},
            {r.x + r.width, r.y},
            {r.x + r.width, r.y + r.height },
            {r.x, r.y + r.height}
        };
    }
    /** assumes vertexes start at upper left corner and go clockwise */
    private static Rectangle getRectFromPoly(double[][] vertexes) {
        return new Rectangle(
            (int) vertexes[0][0], 
            (int) vertexes[0][1], 
            (int) (vertexes[1][0] - vertexes[0][0]), 
            (int) (vertexes[3][1] - vertexes[0][1])
        );
    }

    public boolean intersects(Rectangle other) {
        if (vertexes == null)
            return rect.intersects(other);
        
        for (double[] v : vertexes) {
            if (other.contains((int) v[0], (int) v[1])) 
                return true;
        }

        for (int i = 0; i < vertexes.length-1; i++) {
            if (other.intersectsLine(vertexes[i][0], vertexes[i][1], vertexes[i+1][0], vertexes[i+1][1]))
                return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return  "rect: " + rect.toString() + '\n' + 
                "poly: " + ((vertexes != null)? super.toString() : "null");
                
    }

    @Override
    public void draw(Graphics g) {
        draw(g, false);
    }
    public void draw(Graphics g, boolean ui) {
        int[] pos = (ui)? Canvas.getDrawPosUI(rect.x, rect.y) : Canvas.getDrawPosWorld(rect.x, rect.y);
        double useFOVratio = (ui)? 1 : Canvas.FOVratio;

        g.drawRect(pos[0], pos[1], 
        (int) (rect.width * useFOVratio), 
        (int) (rect.height * useFOVratio));

        if (vertexes != null) {
            g.setColor(g.getColor().darker().darker());
            super.draw(g);
        }
    }

    @Override
    public Hitbox deepCopy() {
        RectShape r = new RectShape(rect.x, rect.y, rect.width, rect.height, anchor[0], anchor[1]);
        r.setRotation(this.rotation);
        return r;
    }

    @Override
    public void forEachPoint(ModifyPoint modifyPoint) {
        throw new UnsupportedOperationException("RectShape doesnt support forEachPoint()");
    }

}
