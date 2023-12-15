package src.entities.attack;

import src.entities.MovingBody;
import src.shapes.Collidable;
import src.shapes.HitboxList;

public abstract class Attack extends MovingBody implements Collidable {

    public HitboxList hitboxes;

    public final int DMG;

    public Attack(String spriteFilename, double x, double y, int dmg, double speed) {
        super("attacks\\" + spriteFilename, x, y, speed);
        this.DMG = dmg;
        hitboxes = new HitboxList(this);
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
}
