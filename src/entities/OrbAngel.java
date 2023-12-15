package src.entities;

import src.entities.attack.EnemyAttack;
import src.entities.attack.Projectile;
import src.shapes.Circle;

public class OrbAngel extends Enemy {

    public OrbAngel(int x, int y) {
        super("innocence angel.png", x, y, 800, 0.15, 
                3000, 1000, 400);
        setSize(0.2);
    }

    @Override
    EnemyAttack createAttackMove() { return new OrbShot(this); }

    class OrbShot extends Projectile {

        public OrbShot(Enemy caster) {
            super("projectile.png", caster, caster.x, caster.y, 1, 0.2, 5000);
            setSize(3);

            hitboxes.add(new Circle(x, y, 10));
            premonition.add(new Circle(x, y, 40));
        }
        @Override
        public double getKnockback() { return 2; }

        @Override
        public void enable() {
            super.enable();
            setPosition(caster.x, caster.y);
            setTrajectoryFor(Player.player.x, Player.player.y);
        }

        @Override
        public void updateHitboxes() {
            Circle c = (Circle) hitboxes.get(0);
            c.x = this.x;
            c.y = this.y;

            Circle prem = (Circle) premonition.get(0);
            prem.x = this.x + trajectory[0] * 50;
            prem.y = this.y + trajectory[1] * 50;
        }


    }

}
