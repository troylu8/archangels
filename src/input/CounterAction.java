package src.input;

import src.entities.*;

public class CounterAction extends KeyPress {

    private static Enemy target = null;

    private CounterAction(int keyCode) { super(keyCode); }

    public static void enable(Enemy target) {
        // set keybind to counter
        CounterAction.target = target;
        // disable focus changing
        Focus.setFocus(target);

    }

    @Override
    public void onKeyPress() {
        Player p = Player.player;

        //disable movement controls
        p.setTrajectoryFor(target.x, target.y);
        p.speedMultiplier *= 5;
        // stop when there
    }

    @Override
    public void onKeyRelease() {}
}
