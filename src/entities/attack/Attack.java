package src.entities.attack;

import src.entities.Being;
import src.entities.MovingBody;
import src.shapes.Collidable;
import src.shapes.HitboxList;
import src.util.Util;

public abstract class Attack extends MovingBody implements Collidable {

    public HitboxList hitboxes;

    public final int DMG;

    

    public Attack(String spriteFilename, double x, double y, int dmg, double speed) {
        super("attacks\\" + spriteFilename, x, y, speed);
        this.DMG = dmg;
        hitboxes = new HitboxList(this);
        setDrawLayer(ATTACK_LAYER);
    }

    @Override
    public HitboxList getHitboxes() {
        return hitboxes;
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        updateHitboxes();
    }

    public abstract double getKnockback();

    public void doHitlag(Being b1, Being b2, int duration) {
        if (animation != null) animation.pause();
        b1.disableMovement();
        b2.disableMovement();

        Util.sleepTilInterrupt(duration);

        if (animation != null) animation.play();
        b1.enableMovement();
        b2.enableMovement();
    }
    public void doHitlag(Being b1, Being b2) {
        doHitlag(b1, b2, 100);
    }
}
