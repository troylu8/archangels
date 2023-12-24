package src.entities;

import src.util.Util;
import src.entities.accessory.Accessory;
import src.shapes.Collidable;
import src.shapes.HitboxList;

public abstract class Being extends MovingBody implements Collidable {

    public Group<Accessory> accessories = new Group<>("accessories for " + this);

    public HitboxList hitboxes;

    final int MAX_HEALTH;
    int health;

    public boolean invincible;

    public Being(String spriteFilename, double x, double y, int maxHealth, double speed) {
        super(spriteFilename, x, y, speed);
        hitboxes = new HitboxList(this);

        this.MAX_HEALTH = maxHealth;
        health = MAX_HEALTH;
        
        invincible = false;
    }

    @Override
    public void setHeading(double heading) {
        
        if (!getHeadingLocked()) {
            super.setHeading(heading);

            accessories.forEachSynced((Accessory a) -> { 
                if (a.headingLinked) a.setHeading(heading);
            });
        }
    }

    @Override
    public void enable() {
        super.enable();

        accessories.forEachSynced((Accessory a) -> { 
            a.enable(); 
        });
    }

    @Override
    public void disable() {
        super.disable();
        accessories.forEachSynced((Accessory a) -> { a.disable(); });
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        updateHitboxes();
    }
    
    /** returns true if successfully hurt (not invincible) */
    public boolean hurt(int dmg) {
        if (invincible) return false;

        health -= dmg;

        health = Util.clamp(health, 0, MAX_HEALTH);

        if (health == 0) {
            disable();
        }

        return true;
    }

    @Override
    public HitboxList getHitboxes() {
        return hitboxes;
    }

    public void makeInvincible(int duration) {
        if (invincible) return;
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                invincible = true;

                Util.sleepTilInterrupt(duration);

                invincible = false;
            }
        }, "invincibility thread for " + this).start();
    }

    @Override
    public void unlockHeading() { 
        super.unlockHeading();
        if (trajectory[0] != 0)
            setHeading(trajectory[0]);
    }

}
