package src.input;

import javax.swing.ActionMap;

import src.draw.Canvas;
import src.entities.*;
import src.entities.accessory.SlashFX;
import src.entities.attack.PlayerSlash;
import src.entities.fx.AfterimageFX;
import src.entities.fx.TintFX;
import src.shapes.Collidable;
import src.util.Util;

public class CounterAction extends KeyPressAction {

    public static final int DURATION = 5000;

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

    }

    public static void disable() {
        if (!enabled) return;
        enabled = false;

        tint.disable();

        KeyBindManager.allKeyBindManagers.get("slash").setKeyPressAction(new SlashAction());
    }

    public static boolean isEnabled() { return enabled; }
    @Override
    public void onKeyPress() {
        System.out.println("counter");

        AfterimageFX.setActive();

        Player p = Player.player;

        p.disableMovement();
        
        /** pos relative to enemy */
        double[] gap = p.getVectorTowards(target.x, target.y, 90);
        p.setPosition(target.x + gap[0], target.y + gap[1]);

        new Thread(() -> {
            Util.sleepTilInterrupt(1000);

            new PlayerSlash(target.x, target.y, false) {
                @Override 
                public void onCollideEnter(Collidable other) {
                    double dir = Util.directionToTheta(target.x - p.x, target.y - p.y);
                    SlashFX.createSlash(target, 4, dir, true);
                    SlashFX.createSlash(target, 4, dir + Math.toRadians(30), true);
                    SlashFX.createSlash(target, 4, dir - Math.toRadians(30), true);
                }
            }.enable();

            AfterimageFX.stop();
            p.enableMovement();
            disable();
        }, "counter pause then slash thread").start();
        
    }

    @Override
    public void onKeyRelease() {}
}
