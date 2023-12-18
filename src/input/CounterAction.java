package src.input;

import javax.swing.ActionMap;

import src.draw.Canvas;
import src.entities.*;

public class CounterAction extends KeyPress {

    static CounterAction counterAction;

    private static Enemy target = null;

    private CounterAction(int keyCode) { super(keyCode); counterAction = this; }

    public static void enable(Enemy target) {

        ActionMap actionMap = Canvas.panel.getActionMap();
        counterAction.addToControls(null, null);
        

        CounterAction.target = target;
        // disable focus changing
        Focus.setFocus(target);

    }

    @Override
    public void onKeyPress() {
        Player p = Player.player;

        p.disableMovement();
        p.setTrajectoryFor(target.x, target.y);
        p.speedMultiplier *= 5;
        // stop when there
    }

    @Override
    public void onKeyRelease() {}
}
