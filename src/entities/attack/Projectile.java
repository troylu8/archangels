package src.entities.attack;

import src.entities.Enemy;
import src.input.PlayerControls;
import src.manage.Clock;
import src.util.Util;
import src.shapes.Collidable;
import src.shapes.HitboxList;

public abstract class Projectile extends EnemyAttack {

    // premonitions are just hitboxes but bigger, follows hitbox around
    public HitboxList premonition;

    int maxDuration;

    public Projectile(String spriteFilename, Enemy caster, double x, double y, int dmg, double speed, int maxDuration) {
        super(spriteFilename, caster, x, y, dmg, speed, 0, -1); // when end frame == -1, endhook wont run so atk will always be active
        this.maxDuration = maxDuration;

        premonition = new HitboxList(this);

        if (animation != null) {
            animation.addFrameHook(animation.totalFrames-1, new Runnable() {
                @Override public void run() { animation.restart(); }
            });
        }
        
    }

    DissipateAfterDuration autoDissipate;
    

    class DissipateAfterDuration extends Thread {
        Projectile proj;

        boolean running = true;
        long timeRemaining;
        long endTime;

        public DissipateAfterDuration(Projectile proj, long timeRemaining) {
            this.proj = proj;
            this.timeRemaining = timeRemaining;
            setName("projectile dissipates after max duration thread");
            Clock.interruptUponPause(this);
        }
        @Override
        public void run() {
            try {
                while (running) {
                    proj.waitTilUnaffectedByClock();

                    endTime = System.currentTimeMillis() + timeRemaining;
                    Thread.sleep(timeRemaining);
                    dissipate();
                }
            } 
            catch (InterruptedException ie) { 
                long nextTimeRemaining = Math.max(endTime - System.currentTimeMillis(), 0);

                proj.waitTilUnaffectedByClock();

                autoDissipate = new DissipateAfterDuration(proj, nextTimeRemaining);
                autoDissipate.start();
             }
            
        }
    }

    @Override
    public void enable() {
        super.enable();
        
        autoDissipate = new DissipateAfterDuration(this, maxDuration);
        autoDissipate.start();
    }

    @Override
    public void disable() {
        super.disable();
        autoDissipate.running = false;
    }

    @Override
    HitboxList getPremonition() { return premonition; }

    @Override
    public Thread sparkAndAttack() {
        Thread saa = new Thread( new Runnable() {
            public void run() {                
                new Spark(caster).enable();
                Util.sleepTilInterrupt(Clock.adjustForClockSpeed(Spark.ANIMATION_TIME));
                addToAllEntities();
                enable();
                addPremonition();
            }
        }, "spark and attack thread for " + caster);
        saa.start();
        return saa;
    };

    public void dissipate() {
        Projectile thisProj = this;
        new Thread( new Runnable() {
            @Override
            public void run() {
                active = false;
                removePremonition();

                while (opacity > 0) {
                    thisProj.waitTilUnaffectedByClock();

                    setSize( getSize() + 0.3 );
                    opacity = Math.max(0, opacity - 0.05f);

                    
                    Util.sleepTilInterrupt(Clock.adjustForClockSpeed(10));
                }

                disable();            }
        }, "projectile dissipate thread").start();
    }

    @Override
    public void onCollideEnter(Collidable other) {
        if (!PlayerControls.DashAction.dashing)
            dissipate();
    }
    
}
