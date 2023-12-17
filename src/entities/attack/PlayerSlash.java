package src.entities.attack;

import src.entities.*;
import src.entities.accessory.SlashFX;
import src.input.PlayerControls;
import src.manage.Clock;
import src.shapes.*;
import src.util.Util;


public class PlayerSlash extends PlayerAttack {

    public static final int RANGE = 300;

    double[] facing;

    /** position relative to player */
    double[] gap;

    boolean doLunge;
    
    public PlayerSlash(double facingX, double facingY, boolean doLunge) {
        super("scythe slash.gif", Player.player.x, Player.player.y, 10);
        setSize(3.5);
        setAnchor(0.6, 0.5);
        clockAffectedLevel = Clock.INCLUDING_PLAYER;

        facing = new double[] {facingX, facingY};
        gap = new double[2];
        this.doLunge = doLunge;
    }
    public PlayerSlash() {
        this(-1, -1, false);
        facing = null;
    }
    

    @Override
    public void enable() {

        PlayerSlash thisSlash = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                
                if (facing != null) {
                    faceTowards(facing[0], facing[1]);
                    gap = getVectorTowards(facing[0], facing[1], 50);

                    
                    if (doLunge && Util.dist(Player.player.x, Player.player.y, facing[0], facing[1]) > 140) {
                        // push player towards enemy and wait until finished
                        try {
                            Player.player.push(facing[0], facing[1], Math.min(4, Util.dist(Player.player.x, Player.player.y, facing[0], facing[1]) / 80)).join();
                        } catch (InterruptedException e) {}
                    }
                } else {
                    setRotation(0); // rotation always 0, heading and gap changes
                    gap[0] = 50 * Player.player.getHeading();
                }
                
                thisSlash.setHeading(gap[0]);
                Player.player.setHeading(gap[0]);

                setPosition(Player.player.x, Player.player.y);
                // transform(gap[0], gap[1]);
                

                addToAllEntities();
                animation.play();

                new Thread(() -> {
                    Player.player.lockHeading();
                    Util.sleepTilInterrupt(300);
                    Player.player.unlockHeading();
                }, "lock heading when slashing thread").start();
                
                Polygon rect = new Polygon(
                    new double[] {x - 90, y + 70},
                    new double[] {x - 90, y - 70},
                    new double[] {x + 90, y - 70},
                    new double[] {x + 90, y + 70}
                );

                double rot = thisSlash.getRotation();
                if (thisSlash.getHeading() == -1) rot += Math.PI;
                rect.rotate(x, y, rot);

                hitboxes.add(rect);
                doPlayerAttackCollisions(thisSlash);
                hitboxes.clear();

            }
        }, "player slash enable thread").start();
        
        
    }

    @Override
    public void updateHitboxes() {}

    @Override 
    public void onCollideEnter(Collidable other) {
        Being enemy = (Being) other;
        Player player = Player.player;

        SlashFX.createSlash(enemy, 3.5, Util.directionToTheta(enemy.x - player.x, enemy.y - player.y), Clock.isPaused());
        new Thread(() -> {
            doHitlag(enemy, player);

            enemy.push(enemy.x + (enemy.x - player.x), enemy.y + (enemy.y - player.y), getKnockback());
        
            player.push(player.x + (player.x - enemy.x), player.y + (player.y - enemy.y), 1);

            player.slowDown(0.33, 300);
            enemy.slowDown(0.1, 300);

            // reset dash cd
            PlayerControls.DashAction.timeOfLastDash = 0;

        }, "hitlag then knockback after slash thread").start();
        
    }

    public void update(long deltaTime) {
        // since melee attack, shouldnt check for collisions every update
        setPosition(Player.player.x, Player.player.y);
        transform( gap[0], gap[1]);
    }

    @Override
    public double getKnockback() { return 2.5; }

} 
