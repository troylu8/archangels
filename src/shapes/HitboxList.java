package src.shapes;

import java.awt.Graphics;
import java.util.*;


public class HitboxList {
    public Collidable wearer;

    private List<Hitbox> hitboxes;

    public HitboxList(Collidable wearer, Hitbox... hitboxes) {
        this.hitboxes = new ArrayList<>();
        for (Hitbox h : hitboxes) {
            this.hitboxes.add(h);
        }
        this.wearer = wearer;
    }

    public void add(Hitbox h) {
        synchronized (hitboxes) { hitboxes.add(h); }
    }
    public Hitbox get(int i) {
        synchronized (hitboxes) {
            if (hitboxes.isEmpty()) return null;
            return hitboxes.get(i);
        }
    }

    public boolean collidesWithCircle(Circle circle) {
        synchronized (hitboxes) {
            for (Hitbox h : hitboxes) {
                if (h.collidesWithCircle(circle))
                    return true;
            }
            return false;
        }
    }

    public void translate(double x, double y) {
        synchronized (hitboxes) {
            for (Hitbox h : hitboxes) 
                h.transform(x, y);
        }
    }

    public void rotate(double originX, double originY, double radiansCCW) {
        synchronized (hitboxes) {
            for (Hitbox h : hitboxes) 
                h.rotate(originX, originY, radiansCCW);
        }
    }

    public void draw(Graphics g) {
        synchronized (hitboxes) {
            for (Hitbox h : hitboxes)
                h.draw(g);
        }
    }

    public void clear() {
        synchronized (hitboxes) { hitboxes.clear(); }
    }
    
    public HitboxList deepCopy() {
        synchronized (hitboxes) {
            HitboxList copy = new HitboxList(wearer);
            for (Hitbox h : hitboxes)
                copy.add(h.deepCopy());
            return copy;
        }
        
    }
}