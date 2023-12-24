package src.entities;

import java.awt.*;
import java.util.*;

import src.draw.Canvas;
import src.entities.attack.*;
import src.manage.*;
import src.util.Util;
import src.shapes.*;

public abstract class Enemy extends Being {

    public static Group<Enemy> allEnemies = new Group<>("allEnemies");

    final int ATK_COOLDOWN;
    final int AGGRO_RANGE;
    final int ATK_RANGE;

    public Enemy(String spriteFilename, double x, double y, int maxHealth, double speed, int atkCooldown, int aggroRange, int atkRange) {
        super("characters\\" + spriteFilename, x, y, maxHealth, speed);

        ATK_COOLDOWN = atkCooldown;
        AGGRO_RANGE = aggroRange;
        ATK_RANGE = atkRange;

        hitboxes.add(new Circle(x, y, 35));

        atkCDthread = new AttackCooldownThread(this, ATK_COOLDOWN);
    }

    abstract EnemyAttack createAttackMove();

    @Override
    public void updateHitboxes() {
        Circle hitbox = (Circle) hitboxes.get(0);
        hitbox.x = this.x;
        hitbox.y = this.y;
    }

    @Override
    public boolean hurt(int dmg) {
        boolean wasHurt = super.hurt(dmg);
        
        // reduce speed for a short period after being hurt
        // if (wasHurt) {
        //     new Thread(new Runnable() {
        //         @Override
        //         public void run() {
        //             multiplySpeedBy(0.5);

        //             Util.sleepNoInterrupt(500);

        //             multiplySpeedBy(2);
        //         }
        //     }, " slow enemy after being hurt thread for " + this).start();
        // }

        return wasHurt;

    }


    @Override
    public void onCollideEnter(Collidable other) {

        if (!(other instanceof PlayerAttack)) return;
        
        PlayerAttack atk = (PlayerAttack) other;

        this.hurt(atk.DMG);
        
        if (!(other instanceof PlayerStarStep) )
            PlayerStarStep.addCharge();
        
    }

    AttackCooldownThread atkCDthread;

    Object inRangeLock = new Object();
    boolean inRange = false;
    
    /** always running, attacks on cooldown, pauses when clock pauses */
    class AttackCooldownThread extends Thread {

        Enemy enemy;
        
        boolean running = true;
        long timeRemaining;
        long endTime = 0;

        public AttackCooldownThread(Enemy e, long timeRemaining) {
            this.enemy = e;
            this.timeRemaining = timeRemaining;
            setName("attack cooldown thread");
            Clock.interruptUponPause(this);
        }

        @Override
        public void run() {
            
            try {
                Thread.sleep( Clock.adjustForClockSpeed(timeRemaining) );

                while (running) {
                    // wait until in range
                    synchronized (inRangeLock) { while (!inRange) { inRangeLock.wait(); } }

                    createAttackMove().sparkAndAttack();

                    final long TRUE_ATK_COOLDOWN = Clock.adjustForClockSpeed(ATK_COOLDOWN);
                    endTime = System.currentTimeMillis() + TRUE_ATK_COOLDOWN;
                    Thread.sleep(TRUE_ATK_COOLDOWN);
                }
                

            } catch (InterruptedException ie) {
                
                // calculate timeRemaining BEFORE waiting for unpause so that CD doesnt drain when time is stopped
                long nextTimeRemaining = Math.max(endTime - System.currentTimeMillis(), 0);

                enemy.waitTilUnaffectedByClock();
                
                atkCDthread = new AttackCooldownThread(enemy, nextTimeRemaining);
                atkCDthread.start();

            }
            
        }
    }
    

    private DriftThread driftThread = new DriftThread(this); 

    /** drift randomly within range */
    class DriftThread extends Thread {
        boolean running = true;
        Enemy enemy;
        public DriftThread(Enemy e) {
            this.enemy = e;
            setName("drift thread");
        }
        public void run() {
            try {
                while (running) {
                    enemy.waitTilUnaffectedByClock();

                    double[] dir = Util.thetaToUnitVector(Math.random() * 2 * Math.PI);
                    double[] pointonEdge = new double[] {
                        Player.player.x + dir[0] * ATK_RANGE, 
                        Player.player.y + dir[1] * ATK_RANGE
                    };
                    setTrajectoryFor( pointonEdge[0], pointonEdge[1]);
                    
                    Thread.sleep(Clock.adjustForClockSpeed(1000));
                }
            } 
            catch (InterruptedException ie) { }
            
        }
    }

    @Override
    public void enable() {
        super.enable();
        atkCDthread.start();

        allEnemies.queueToAdd(this);
    }

    @Override
    public void disable() {
        super.disable();
        atkCDthread.running = false;
        driftThread.running = false;

        allEnemies.queueToRemove(this);
    }

    boolean a = true;
    @Override
    public void update(long deltaTime) {
        if (Clock.isPaused() && affectedByClock()) return;
        
        super.update(deltaTime);

        double playerDist = Util.dist(Player.player.x, Player.player.y, this.x, this.y);
        
        if (playerDist > AGGRO_RANGE) {
            trajectory[0] = 0;
            trajectory[1] = 0;
            inRange = false;
        }
        else if (playerDist > ATK_RANGE) {
            setTrajectoryFor(Player.player.x, Player.player.y);
            driftThread.interrupt();
            inRange = false;
        }
        else {
            inRange = true;
            synchronized (inRangeLock) { inRangeLock.notifyAll(); } 

            if (!driftThread.isAlive()) {
                driftThread = new DriftThread(this);
                driftThread.start();
            }
        }
            
        
    }

    public static HashMap<Enemy, ArrayList<double[]>> queuedPush = new HashMap<>();

    @Override
    public Thread push(double towardsX, double towardsY, double force) {
        
        if (Clock.isPaused()) {
            ArrayList<double[]> q = queuedPush.getOrDefault(this, new ArrayList<>());
            q.add(new double[] {towardsX, towardsY, force});
            queuedPush.put(this, q);
            return null;
        }
        else {
            return super.push(towardsX, towardsY, force);
        }
        
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        // draw attack range
        if (Main.drawHitboxes) {
            g.setColor(Color.RED);
            int[] pos = Canvas.getDrawPosWorld(x - ATK_RANGE, y - ATK_RANGE);
            g.drawOval(pos[0], pos[1], (int)(2 * ATK_RANGE * Canvas.FOVratio), (int)(2 * ATK_RANGE * Canvas.FOVratio));
        }
    }


    public static Enemy closestEnemyInRange(int range) {
        Enemy res = null;
        double closestDist = range;

        double dist;
        synchronized (Enemy.allEnemies) {
            for (Enemy e : Enemy.allEnemies) {
                if ((dist = Util.dist(Player.player.x, Player.player.y, e.x, e.y)) < closestDist) {
                    closestDist = dist;
                    res = e;
                }
            }
        }

        return res;
    }


    /** bundles cost and spawnFunction together */
    public static class Spawner {
        public int cost;
        public SpawnFunction spawnFunction;
        public Spawner(int cost, SpawnFunction spawnFunction) {
            this.cost = cost;
            this.spawnFunction = spawnFunction;
        }
    }

    @FunctionalInterface
    public static interface SpawnFunction {
        public abstract Enemy spawn();
    }
}