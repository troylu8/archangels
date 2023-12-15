package src.entities.attack;

import src.draw.Canvas;
import src.entities.Being;
import src.entities.Enemy;
import src.entities.Player;
import src.entities.fx.AfterimageFX;
import src.manage.Clock;
import src.util.Util;
import src.shapes.Circle;
import src.shapes.Collidable;

public class PlayerStarStep extends PlayerAttack {

    final int RADIUS = 140;

    /** star step requires the player to land 3 hits without taking dmg to cast */
    private static int charge = 0;

    public PlayerStarStep() {
        super("demon star.gif", Player.player.x, Player.player.y, 50);
        setSize(5);
        hitboxes.add(new Circle(x, y, RADIUS));

        clockAffectedLevel = Clock.INCLUDING_PLAYER;
    }

    public static int getCharge() { return charge; }
    public static void addCharge() { setCharge(charge + 1); }
    public static void setCharge(int charge) { 
        PlayerStarStep.charge = charge; 
        Player.player.hornsAndTail.setStage(Math.min(charge, 3));
    }

    @Override
    public void enable() {
        
        new Thread(new Runnable() {
            @Override
            public void run() {

                double[] starVector;
                double[] playerVector;

                if (Player.player.trajectory[0] != 0 || Player.player.trajectory[1] != 0) {

                    starVector = new double[] {
                        Player.player.trajectory[0] * RADIUS,
                        Player.player.trajectory[1] * RADIUS
                    };

                    playerVector = new double[] {
                        Player.player.trajectory[0] * 2 * RADIUS,
                        Player.player.trajectory[1] * 2 * RADIUS
                    };
                }
                else {
                    Being closestEnemy = Enemy.closestEnemyInRange(RADIUS * 2);
                    if (closestEnemy != null) {                        
                        starVector = Player.player.getVectorTowards(closestEnemy.x, closestEnemy.y, RADIUS);
                        playerVector = Player.player.getVectorTowards(closestEnemy.x, closestEnemy.y, 2 * RADIUS);
                        Player.player.setHeading(playerVector[0]);
                    } else {
                        starVector = new double[] {RADIUS * Player.player.getHeading(), 0};
                        playerVector = new double[] {2 * RADIUS * Player.player.getHeading(), 0};
                    }
                    
                    // unsure whether to use ayato-like slash if no directional key held
                    // (player doesnt move)
                }

                Player.player.visible = false;
                Player.player.invincible = true;
                Player.player.slowDown(0.15, 550);

                transform(starVector[0],  starVector[1]);

                Canvas.screenshake(250, 5, 50);

                addToAllEntities();
                animation.play();

                Player.player.transform(playerVector[0], playerVector[1]);

                Util.sleepTilInterrupt(250);

                Player.player.visible = true;
                Player.player.invincible = false;

                AfterimageFX.setActiveFor(250);
            }
            
        }, "player star step enable thread").start();

    }


    @Override
    public void updateHitboxes() {
        Circle c = (Circle) hitboxes.get(0);
        c.x = this.x;
        c.y = this.y;
    }

    @Override
    public void onCollideEnter(Collidable c) {
        Being enemy = (Being) c;
        enemy.slowDown(0.1, 800);
    }
    
    @Override
    public double getKnockback() { return 0; }
}

