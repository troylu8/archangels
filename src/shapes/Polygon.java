package src.shapes;

import java.awt.Graphics;

import src.draw.Canvas;
import src.util.Util;

public class Polygon extends Hitbox {

    //TODO: make private
    public double[][] vertexes;

    public Polygon(double[]... vertexes) {
        this.vertexes = vertexes;
    }

    @Override
    public boolean collidesWithCircle(Circle circle) {
        // https://www.desmos.com/calculator/btdfol7lwm

        // counting # of edges touching ray that starts at circle center going left

        int edgesTouchingRay = 0;
        
        for (int i = 0; i < vertexes.length; i++) {
            
            double x1 = vertexes[i][0];
            double y1 = vertexes[i][1];
            double x2 = vertexes[(i+1) % vertexes.length][0];
            double y2 = vertexes[(i+1) % vertexes.length][1];

            // if circle intersects this edge return true
            if (lineTouchesCircle(x1, y1, x2, y2, circle))
                return true;

            // if ray isnt even between top and bottom points, continue
            if (!between(y1, y2, circle.y))
                continue;
            
            // if edge is vertical line AND ray is between y1 y2,  ray is touching edge
            if (x1 == x2) {
                if (x1 < circle.x) 
                    edgesTouchingRay++;
            } else {
                double slope = (y2 - y1) / (x2 - x1);
            
                double intersectX = (circle.y - y1 + slope * x1) / slope;
                
                if (intersectX < circle.x && between(x1, x2, intersectX)) 
                    edgesTouchingRay++;  
            }
            
        
        }

        // if ray touches odd # of edges, return true
        return edgesTouchingRay % 2 == 1;
    }

    @Override
    public void transform(double x, double y) {
        for (double[] vertex : vertexes) {
            vertex[0] += x;
            vertex[1] += y;
        }
    }

    @Override
    public void rotate(double originX, double originY, double radiansCW) {
        for (int i = 0; i < vertexes.length; i++) 
            vertexes[i] = Util.rotatePoint(vertexes[i][0], vertexes[i][1], originX, originY, radiansCW);
    }

    @Override
    public void draw(Graphics g) {
        for (int i = 0; i < vertexes.length; i++) {
            
            double x1 = vertexes[i][0];
            double y1 = vertexes[i][1];
            double x2 = vertexes[(i+1) % vertexes.length][0];
            double y2 = vertexes[(i+1) % vertexes.length][1];

            int[] cameraPos1 = Canvas.getDrawPosWorld(x1 , y1);
            int[] cameraPos2 = Canvas.getDrawPosWorld(x2 , y2);
            g.drawLine(cameraPos1[0], cameraPos1[1], cameraPos2[0], cameraPos2[1]);
        
        }
    }

    @Override
    public Hitbox deepCopy() { 
        double[][] vertexesCopy = new double[vertexes.length][2];
        for (int i = 0; i < vertexes.length; i++) 
            vertexesCopy[i] = new double[] {vertexes[i][0], vertexes[i][1]};
        
        return new Polygon(vertexesCopy);
    }

    @Override
    public void forEachPoint(ModifyPoint modifyPoint) {
        for (int i = 0; i < vertexes.length; i++) 
            vertexes[i] = modifyPoint.modify(vertexes[i][0], vertexes[i][1]);
    }

    @Override
    public String toString() {
        String res = "";
        for (double[] v : vertexes) 
            res += " > " + "[" + v[0] + "," + v[1] + "]";
        return res;
    }

}