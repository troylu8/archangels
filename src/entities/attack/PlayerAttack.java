package src.entities.attack;

import java.util.HashSet;

import src.entities.Enemy;
import src.entities.Player;
import src.shapes.Circle;
import src.shapes.Collidable;

/**  a sibling of EnemyAttack under Attack  */
public abstract class PlayerAttack extends Attack {

    /** set of collidables that i've hit; i can only hit each unique enemy once
     * different than collidingWith bc these are all enemies ive hit in my life */
    HashSet<Collidable> collided;

    public PlayerAttack(String spriteFilename, double x, double y, int dmg) {
        super(spriteFilename, x, y, dmg, -1);
        collided = new HashSet<>();
    }

    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        doPlayerAttackCollisions(this);
    }

    public static void doPlayerAttackCollisions(PlayerAttack attack) {
        // System.out.println("starting atk collisions"); sync
        Enemy.allEnemies.forEachSynced( (Enemy enemy) -> {
            if (attack.hitboxes.collidesWithCircle((Circle) enemy.hitboxes.get(0))) {
                if (attack.collided.add(enemy)) {
                    attack.onCollideEnter(enemy);
                    enemy.onCollideEnter(attack);
                }
            }
        });
        
        // System.out.println("finished atk collisions"); sync

    }
    
}

