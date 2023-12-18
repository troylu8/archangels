package src.input;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import src.draw.Canvas;
import src.entities.Player;

public class PlayerMovementControls {

    private static boolean enabled = false;
    
    private static int[] keyCodes = { KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D };

    private static final Integer[][] DIRECTIONS = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };

    private static final String[] ACTION_CODES = { "up", "down", "left", "right" };


    public static void enable() {
        if (enabled) return;
        enabled = true;
        for (int i = 0; i < 4; i++) 
            setDirectionalKey(keyCodes[i], DIRECTIONS[i], ACTION_CODES[i]);
        
    }

    public static void disable() {
        if (!enabled) return;
        enabled = false;
        ActionMap actionMap = Canvas.panel.getActionMap();
        for (int i = 0; i < 4; i++) 
            actionMap.remove(ACTION_CODES[i]);
    }

    public static boolean isEnabled() { return enabled; }

    public static void setDirectionalKey(int keyCode, Integer[] direction, String actionCode){
        PlayerControls.addKeybind(PlayerControls.defaultControls, keyCode, new OffsetManager(direction, true), true, actionCode);
        PlayerControls.addKeybind(PlayerControls.defaultControls, keyCode, new OffsetManager(direction, false), false, actionCode);
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

    static class OffsetManager extends AbstractAction {

        Integer[] direction;
        boolean addDirection;

        public OffsetManager(Integer[] direction, boolean addDirection){
            this.direction = direction;
            this.addDirection = addDirection;
        }

        @Override
        public void actionPerformed(ActionEvent e){

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

}