package src.entities.fx;

import src.entities.Entity;
import src.entities.Player;
import src.manage.Clock;
import src.util.Util;

public class AfterimageFX extends Entity {
    
    /** one afterimage appears every (DELAY_BETWEEN) ms  */
    static final int DELAY_BETWEEN = 16;

    static private Thread afterImageMaker = new Thread();

    public AfterimageFX(double x, double y) {
        super("demon afterimage.png", x, y, 4);
        setHeading(Player.player.getHeading());

        disableOnRoomChange = false;
        clockAffectedLevel = Clock.INCLUDING_PLAYER;
    }

    public static Thread setActive() {
        return setActiveFor(Integer.MAX_VALUE);
    }

    public static Thread setActiveFor(int ms) {
        afterImageMaker.interrupt();

        afterImageMaker = new Thread(() -> {
            try {
                for (int i = 0; i < ms; i += DELAY_BETWEEN) {
                    new AfterimageFX(Player.player.x, Player.player.y).enable();
                    Thread.sleep(Clock.adjustForClockSpeed(DELAY_BETWEEN));
                }
            } 
            catch (InterruptedException ie ) {}
        }, "afterimage maker thread");
        
        afterImageMaker.start();
        return afterImageMaker;
    }
    public static void stop() {
        afterImageMaker.interrupt();
    }


    @Override
    public void enable() {
        super.enable();
        AfterimageFX thisAfterimg = this;
        new Thread(() -> {
            for (; opacity > 0; opacity = Math.max(0, opacity - 0.05f) ) {
                thisAfterimg.waitTilUnaffectedByClock();
                Util.sleepTilInterrupt(Clock.adjustForClockSpeed(5));
            }

            disable();
        }, "afterimage fading thread").start();
    }
    
}
