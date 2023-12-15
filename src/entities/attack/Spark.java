package src.entities.attack;

import src.entities.Enemy;
import src.entities.Entity;

public class Spark extends Entity {

    public static final int ANIMATION_TIME = 1050;

    private Enemy caster;

    public Spark(Enemy caster) {
        super("spark.gif", caster.x, caster.y);
        this.caster = caster;
        setSize(3);
    }

    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        setPosition(caster.x, caster.spriteBounds.getY());
    }
}
