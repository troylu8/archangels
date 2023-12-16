package src.entities;
import java.util.HashSet;

import src.draw.Canvas;
import src.entities.accessory.Accessory;
import src.entities.accessory.HornsAndTail;
import src.entities.attack.*;
import src.input.*;
import src.manage.Clock;
import src.shapes.*;
import src.util.Util;

public class Player extends Being {

    public static Player player;

    public static Interactable focusedInteractable;

    PlayerControls playerControls;

    /** set of collidables that i am currently in collision with
     * different than collided bc these are only things im colliding with RIGHT NOW */
    public HashSet<Collidable> collidingWith = new HashSet<>();
    public Circle playerCircle;

    public HornsAndTail hornsAndTail;
    public Accessory scytheAccessory;

    public boolean parryBodyActive;
    
    public Player(int x, int y) {
        super("characters\\demon.png", x, y, 3, 1.1);
        setSize(4);
        
        playerCircle = new Circle(x, y, 25);
        hitboxes.add(playerCircle);

        playerControls = new PlayerControls(this);
        
        disableOnRoomChange = false;

        parryBodyActive = false;

        clockAffectedLevel = Clock.INCLUDING_PLAYER;
    }

    @Override
    public void enable() {

        hornsAndTail = new HornsAndTail();
        
        // scytheAccessory = new Accessory("scythe accessory.png", this, 0.1, true);
        // scytheAccessory.setSize(4);

        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
        hornsAndTail.disable();
    }
    
    @Override
    public boolean hurt(int dmg) {

        if (invincible) {
            System.out.println("player iframed attack");
            return false;
        }
        health -= dmg;
        health = Util.clamp(health, 0, MAX_HEALTH);

        PlayerStarStep.setCharge(0);

        makeInvincible(1500);

        return true;
    }

    @Override
    public void updateHitboxes() {
        Circle hitbox = (Circle) this.hitboxes.get(0);
        hitbox.x = this.x;
        hitbox.y = this.y;
    }

    @Override
    public void onCollideEnter(Collidable other) {

        if (!(other instanceof EnemyAttack)) return;

        EnemyAttack atk = (EnemyAttack) other;

        if (parryBodyActive) {

            Canvas.screenshake(80, 2, 15);
            
            new Thread(() -> {
                Clock.pause(Clock.INCLUDING_PLAYER);
                Util.sleepTilInterrupt(250);
                Clock.unpause();
            }, "freeze on parry thread").start();
            

            final int slashReach = 120;

            double[] parryPos = {atk.x, atk.y};
            if (Util.dist(this.x, this.y, atk.x, atk.y) > slashReach) {
                parryPos = getVectorTowards(atk.x, atk.y, slashReach);
                parryPos[0] += Player.player.x;
                parryPos[1] += Player.player.y;
            }
            
            new Entity("parry.gif", parryPos[0], parryPos[1], 10).enable();
        }
        else if (player.hurt(1)) 
            push(x + (x - atk.x), y + (y - atk.y), atk.getKnockback());
    }

    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);

        Canvas.targetFOVcenter[0] = x;
        Canvas.targetFOVcenter[1] = y;
    }
    
}
