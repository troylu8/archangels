package src.input;

import javax.swing.ActionMap;

import src.draw.Canvas;
import src.entities.*;

public class CounterAction extends KeyPressAction {

    private static boolean enabled = false;

    private static Enemy target = null;

    public static void enable() {
        if (enabled) return;
        enabled = true;

        System.out.println("enabled");

        KeyBindManager.allKeyBindManagers.get("slash").setKeyPressAction(new CounterAction());

        CounterAction.target = target;

        // disable focus changing
        Focus.setFocus(target);

    }

    public static void disable() {
        if (!enabled) return;
        enabled = false;

        KeyBindManager.allKeyBindManagers.get("slash").setKeyPressAction(new SlashAction());
    }

    public static boolean isEnabled() { return enabled; }
    @Override
    public void onKeyPress() {
        System.out.println("counter");
        // Player p = Player.player;

        // p.disableMovement();
        // p.setTrajectoryFor(target.x, target.y);
        // p.speedMultiplier *= 5;
        // // stop when there
    }

    @Override
    public void onKeyRelease() {}
}
