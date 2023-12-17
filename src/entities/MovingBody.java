package src.entities;
import java.util.HashSet;

import src.manage.Clock;
import src.util.Util;

public class MovingBody extends Entity {

    private boolean movementEnabled = true;

    public final double BASE_SPEED;

    public double speedMultiplier = 1;

    /** vector on the unit circle */
    public double[] trajectory;
    HashSet<double[]> forces;

    public MovingBody(String spriteFilename, double x, double y, double baseSpeed) {
        super(spriteFilename, x, y);

        this.BASE_SPEED = baseSpeed;
        trajectory = new double[] {0,0};
        forces = new HashSet<>();
    }

    public void enableMovement() { 
        movementEnabled = true;
        unlockHeading();
    }
    public void disableMovement() { 
        movementEnabled = false; 
        lockHeading();
    }
    public boolean isMovementEnabled() { return movementEnabled; }

    final Object forcesLock = new Object();

    public Thread push(double destX, double destY, double force) {

        Thread pushThread = new Thread(new Runnable() {
            @Override
            public void run() {

                double f = force;
                double[] unitVector = getVectorTowards(destX, destY, 1);

                double[] vector = new double[2];

                synchronized (forcesLock) { forces.add(vector); }

                long lastTime = System.currentTimeMillis();
                
                while (f > 0) {
                    
                    long timeNow = System.currentTimeMillis();                    

                    vector[0] = f * unitVector[0];
                    vector[1] = f * unitVector[1];

                    // f is like velocity since its (distance moved) / time.   (0.1 * deltatime) is like its acceleration
                    f -= 0.1 * (timeNow - lastTime);

                    lastTime = timeNow;
                    Util.sleepTilInterrupt(5);

                }

                synchronized (forcesLock) { forces.remove(vector); }

            }
        }, "push thread for " + this);
        pushThread.start();
        return pushThread;
    }

    public Thread slowDownThread = new Thread();

    /** here to provide an interruptible slowdown effect (ex: dashing can stop starstep slow) */
    public void slowDown(double slowPercent, int time, Runnable endHook) {
        slowDownThread.interrupt();
        slowDownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                
                speedMultiplier *= slowPercent;

                Util.sleepTilInterrupt(time);

                speedMultiplier /= slowPercent;

                if (endHook != null) endHook.run();
            }
        }, "slowdown thread for " + this);
        slowDownThread.start();   
    }
    public void slowDown(double slowPercent, int time) {
        slowDown(slowPercent, time, null);
    }

    @Override
    public void update(long deltaTime) {
        if (!movementEnabled || (Clock.isPaused() && affectedByClock()) ) return;
        
        double[] totalForces = {0, 0};
        synchronized (forcesLock) {
            for (double[] force : forces) {
                totalForces[0] += force[0];
                totalForces[1] += force[1];
            }
        }
        // forces magnitude not affected by Clock.SPEED
        double finalSpeed = BASE_SPEED * speedMultiplier;
        transform(
            (trajectory[0] * finalSpeed) * deltaTime * Clock.getSpeed() + totalForces[0] * deltaTime, 
            (trajectory[1] * finalSpeed) * deltaTime * Clock.getSpeed() + totalForces[1] * deltaTime
        );
    }

    public void setTrajectoryFor(double destX, double destY) {

        trajectory = getVectorTowards(destX, destY, 1);

        if (trajectory[0] != 0) 
            setHeading(trajectory[0]);
    };
    
}
