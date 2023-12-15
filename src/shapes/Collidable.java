package src.shapes;

public interface Collidable {

    abstract HitboxList getHitboxes();
    
    abstract void updateHitboxes();

    abstract void onCollideEnter(Collidable other);
}

