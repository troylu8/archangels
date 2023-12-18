package src.entities.attack;

import src.shapes.*;
import src.entities.*;
import src.manage.Clock;
import src.util.Util;

/** a sibling of PlayerAttack under Attack */
public abstract class EnemyAttack extends Attack {
    
    /** if dash inside a premonition, trigger mirage */
    public static Group<HitboxList> allPremonitions = new Group<>();

    final int attackStartFrame;
    final int attackEndFrame;
    int timeBeforeAttackFrame;
    /** true when between attack start and end frames */
    boolean active;

    public Enemy caster;

    /** attackFrame = when spark should disappear, 0-indexed */
    public EnemyAttack(String spriteFilename, Enemy caster, double x, double y, int dmg, double speed, int attackStartFrame, int attackEndFrame) {
        super(spriteFilename, x, y, dmg, speed);
        this.caster = caster;
        this.attackStartFrame = attackStartFrame;
        this.attackEndFrame = attackEndFrame;
        active = false;
        visible = false;

        if (animation != null) {
            timeBeforeAttackFrame = 0;
            for (int i = 0; i < attackStartFrame; i++) 
                timeBeforeAttackFrame += animation.frameDurations[i]; 
            
            
            if (attackStartFrame == attackEndFrame) {
                animation.addFrameHook(attackStartFrame, new Runnable() {
                    @Override public void run() { 
                        removePremonition(); 

                        doEnemyAttackCollision();
                    }
                });
            } 
            else {
                animation.addFrameHook(attackStartFrame, new Runnable() {
                    @Override public void run() { 
                        removePremonition(); 
                        active = true;
                    }
                });
                animation.addFrameHook(attackEndFrame, new Runnable() {
                    @Override public void run() { active = false; }
                });
            }
        }
        
    }
    public EnemyAttack(String spriteFilename, Enemy caster, double x, double y, int dmg, int attackStartFrame, int attackEndFrame) {
        this(spriteFilename, caster, x, y, dmg, 1, attackStartFrame, attackEndFrame);
    }

    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        if (active) doEnemyAttackCollision(); 
    }

    public void doEnemyAttackCollision() {
        if (hitboxes.collidesWithCircle(Player.player.playerCircle)) {
            if (Player.player.collidingWith.add(this)) {
                Player.player.onCollideEnter(this);
                this.onCollideEnter(Player.player);
            }
        }
    }
    HitboxList getPremonition() { return hitboxes; }

    public static HitboxList isPlayerInPremonition() {
        if (Clock.isPaused()) return null;
        
        // not using Group.foreachSynced() to allow return keyword
        synchronized (allPremonitions) {
            for (HitboxList hitbox : allPremonitions) {
                if (hitbox.collidesWithCircle(Player.player.playerCircle)) {
                    return hitbox;
                }
            }
        }

        return null;

    }

    public void addPremonition() { allPremonitions.queueToAdd(getPremonition()); }
    public void removePremonition() { allPremonitions.queueToRemove(getPremonition()); }

    @Override
    public void disable() {
        super.disable();
        removePremonition();
    }


    public Thread sparkAndAttack() {
        
        Thread saa = new Thread( new Runnable() {
            public void run() {
                
                int gap = Clock.adjustForClockSpeed(Spark.ANIMATION_TIME - timeBeforeAttackFrame);

                // even tho attack is not visible, need to add to allEntities to run update()
                addToAllEntities();

                if (gap > 0) {
                    new Spark(caster).enable();
                    addPremonition();
                    Util.sleepTilInterrupt(gap);
                    enable();
                }
                else {
                    enable();
                    Util.sleepTilInterrupt(-gap);
                    new Spark(caster).enable();
                    addPremonition();
                }
            }
        }, "spark and attack thread for " + caster);
        saa.start();
        return saa;
    };

    @Override
    public void enable() { // doesnt call super.enable() bc already added to factory earlier
        visible = true;
        if (animation != null)  animation.play();
        else                    active = true;
    }

}



