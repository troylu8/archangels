package src.input;

import javax.swing.ActionMap;

import src.draw.Canvas;
import src.entities.*;
import src.entities.accessory.SlashFX;
import src.entities.attack.PlayerSlash;
import src.entities.fx.AfterimageFX;
import src.entities.fx.TintFX;
import src.manage.Clock;
import src.shapes.Collidable;
import src.util.Util;

public class CounterAction extends KeyPressAction {

    public static final int DURATION = 1000;
    private static Thread disableAfter = new Thread();

    private static boolean enabled = false;

    private static Enemy target = null;

    static TintFX tint;

    public static void enable(Enemy target) {
        if (enabled) return;
        enabled = true;

        KeyBindManager.allKeyBindManagers.get("slash").setKeyPressAction(new CounterAction());

        CounterAction.target = target;

        Focus.updateFocus = false;
        Focus.setFocus(target);

        tint = new TintFX(DURATION);
        tint.enable();

        Clock.setSpeed(0.3);
        AfterimageFX.setActive();
        
        disableAfter = new Thread(() -> {
            try {
                Thread.sleep(DURATION);
                System.out.println("time disable");
                disable();
            } 
            catch (InterruptedException e) { System.out.println("interrupt");}
        }, "disable counterAction after time thread");
        disableAfter.start();

        System.out.println("enabled");
    }

    public static void disable() {
        if (!enabled) return;
        enabled = false;
        System.out.println("disabled");

        Clock.setSpeed(1);
        AfterimageFX.stop();

        tint.disable();

        KeyBindManager.allKeyBindManagers.get("slash").setKeyPressAction(new SlashAction());
    }

    public static boolean isEnabled() { return enabled; }

    @Override
    public void onKeyPress() {
        System.out.println("counter");

        // disableAfter.interrupt();

        Player p = Player.player;
        
        /** pos relative to enemy */
        double[] gap = p.getVectorTowards(target.x, target.y, 110);
        p.setPosition(target.x + gap[0], target.y + gap[1]);

        new Thread(() -> {
            Clock.pause(Clock.INCLUDING_PLAYER);
            Util.sleepTilInterrupt(300);
            Clock.unpause();

            new PlayerSlash(target.x, target.y, false) {
                @Override 
                public void onCollideEnter(Collidable other) {
                    double dir = Util.directionToTheta(target.x - p.x, target.y - p.y);
                    new Thread( () -> {
                        SlashFX.createSlash(target, 6, dir, true);
                        SlashFX.createSlash(target, 4, dir + Math.toRadians(Util.rand(15, 30)) *  ((Math.random() > 0.5)? 1 : -1)  , true);
                    } ).start();
                }
            }.enable();

            disable();
        }, "counter pause then slash thread").start();
        
    }

    @Override
    public void onKeyRelease() {}
}
