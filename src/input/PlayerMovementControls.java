package src.input;

import java.awt.event.*;
import java.util.*;

import src.entities.Player;

public class PlayerMovementControls {
    
    private static int[] keyCodes = { KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D };

    private static final Integer[][] DIRECTIONS = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };

    private static final String[] ACTION_CODES = { "up", "down", "left", "right" };
    
    static {
        // create keybind managers
        for (int i = 0; i < 4; i++) 
            new KeyBindManager(ACTION_CODES[i], keyCodes[i]);
    }

    public static void addMovementControls(HashMap<String, KeyPressAction> controlsList) {
        for (int i = 0; i < 4; i++) 
            controlsList.put(ACTION_CODES[i], new MovementAction(DIRECTIONS[i]));
    }

    public static void stopMoving(){
        directions.clear();
        Player.player.trajectory[0] = 0;
        Player.player.trajectory[1] = 0;
        offset[0] = 0;
        offset[1] = 0;
    }

    private static Set<Integer[]> directions = new HashSet<>();
    
    /** player trajectory without accounting for diagonal length, used to determine if player trajectory needs diagonal shortening or not */ 
    public static int[] offset = new int[2]; 

    static class OffsetManager {

        Integer[] direction;
        boolean addDirection;

        public OffsetManager(Integer[] direction, boolean addDirection){
            this.direction = direction;
            this.addDirection = addDirection;
        }

        public void changeOffset() {

            boolean somethingChanged = false;
            
            if (addDirection)   somethingChanged = directions.add(direction); 
            else                somethingChanged = directions.remove(direction);

            if (!somethingChanged) return;

            offset[0] += direction[0] * (addDirection ? 1 : -1);
            offset[1] += direction[1] * (addDirection ? 1 : -1);

            Player.player.trajectory[0] = offset[0];
            Player.player.trajectory[1] = offset[1];

            if (offset[0] != 0 && offset[1] != 0) {
                Player.player.trajectory[0] *= 0.707;
                Player.player.trajectory[1] *= 0.707;
            }
            
            if (Player.player.trajectory[0] != 0) 
                Player.player.setHeading(Player.player.trajectory[0]);
            
        }
    }

    static class MovementAction extends KeyPressAction {

        OffsetManager offsetManagerPress;
        OffsetManager offsetManagerRelease;

        public MovementAction(Integer[] direction) {
            offsetManagerPress = new OffsetManager(direction, true);
            offsetManagerRelease = new OffsetManager(direction, false);
        }

        @Override
        public void onKeyPress() { offsetManagerPress.changeOffset(); }

        @Override
        public void onKeyRelease() { offsetManagerRelease.changeOffset(); }

    }

}