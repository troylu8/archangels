package src.entities;

import src.draw.Canvas;
import src.entities.attack.EnemyAttack;
import src.shapes.Collidable;
import src.shapes.Polygon;
import src.util.Util;

public class SwordAngel extends Enemy {

    public SwordAngel(double x, double y) {
        super("oriental angel.png", x, y, 800, 0.25,
            2500, 1, 100
        );

        setSize(0.2);
    }

    @Override
    public EnemyAttack createAttackMove() { return new StaffSwing(this); }

    public static class StaffSwing extends EnemyAttack {

        public StaffSwing(Enemy caster) {
            super("staff swing.gif", caster, caster.x, caster.y, 1, 3, 3);
            setSize(5);

            endHook = new Runnable() { public void run() { 
                caster.speedMultiplier *= 10;
                caster.unlockHeading();
            } };

            hitboxes.add(new Polygon(4));

        }

        @Override
        public double getKnockback() { return 4.3; }

        boolean lockRotation = false;

        @Override
        public void enable() {
            caster.speedMultiplier *= 0.1;
            caster.setHeading(Player.player.x - caster.x);
            caster.lockHeading();

            setPosition(caster.x, caster.y);
            lockRotation = true;

            super.enable();
        }
        
        @Override
        public void update(long deltaTime) {
            super.update(deltaTime);
            setPosition(caster.x, caster.y);
            if (!lockRotation) 
                faceTowards(Player.player.x, Player.player.y);
        }

        @Override
        public void updateHitboxes() {
            Polygon rect = (Polygon) hitboxes.get(0);
            rect.vertexes[0] = new double[] {caster.x +  caster.ATK_RANGE, caster.y - 80};
            rect.vertexes[1] = new double[] {caster.x -                 0, caster.y - 80};
            rect.vertexes[2] = new double[] {caster.x -                 0, caster.y + 80};
            rect.vertexes[3] = new double[] {caster.x +  caster.ATK_RANGE, caster.y + 80};

            double rot = getRotation();
            if (getHeading() == -1) rot += Math.PI;
            rect.rotate(x, y, rot);
        }

        @Override
        public void onCollideEnter(Collidable other) {}
    }    
}
