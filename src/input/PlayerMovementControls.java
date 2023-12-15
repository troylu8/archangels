package src.input;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import src.draw.Canvas;
import src.entities.Player;

public class PlayerMovementControls {

    JPanel panel;
    Player player;
 
    public static int upKeyCode = KeyEvent.VK_W;
    public static int downKeyCode = KeyEvent.VK_S;
    public static int leftKeyCode = KeyEvent.VK_A;
    public static int rightKeyCode = KeyEvent.VK_D;

    private static final Integer[] UP = {0, -1};
    private static final Integer[] DOWN = {0, 1};
    private static final Integer[] LEFT = {-1, 0};
    private static final Integer[] RIGHT = {1, 0};

    public PlayerMovementControls(Player player){
        panel = Canvas.panel;

        this.player = player;

        setDirectionalKey(upKeyCode, UP);
        setDirectionalKey(downKeyCode, DOWN);
        setDirectionalKey(leftKeyCode, LEFT);
        setDirectionalKey(rightKeyCode, RIGHT);
        
    }

    public void setDirectionalKey(int keyCode, Integer[] direction){
        PlayerControls.addKeybind(PlayerControls.defaultControls, keyCode, new OffsetManager(direction, true), true);
        PlayerControls.addKeybind(PlayerControls.defaultControls, keyCode, new OffsetManager(direction, false), false);
    }

    public void stopMoving(){
        directions.clear();
        player.trajectory[0] = 0;
        player.trajectory[1] = 0;
        offset[0] = 0;
        offset[1] = 0;
    }

    static Set<Integer[]> directions = new HashSet<>();
    
    /** player trajectory without accounting for diagonal length, used to determine if player trajectory needs diagonal shortening or not */ 
    public static int[] offset = new int[2]; 

    class OffsetManager extends AbstractAction {

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

            player.trajectory[0] = offset[0];
            player.trajectory[1] = offset[1];

            if (offset[0] != 0 && offset[1] != 0) {
                player.trajectory[0] *= 0.707;
                player.trajectory[1] *= 0.707;
            }
            
            if (player.trajectory[0] != 0) 
                player.setHeading(player.trajectory[0]);
            
        }
    }

}