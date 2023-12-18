package src.input;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;

import src.draw.Canvas;
import src.entities.*;

public class PlayerControls {
    
    /** always active regardless of current activeControls*/
    public static HashMap<KeyStroke, String> constantControls = new HashMap<>();
    public static HashMap<KeyStroke, String> defaultControls = new HashMap<>();

    public static HashMap<KeyStroke, String> activeControls;

    public PlayerControls(Player player){

        PlayerMovementControls.enable();

        new DashAction(KeyEvent.VK_K).addToControls(defaultControls, "dash");
        new SlashAction(KeyEvent.VK_J).addToControls(defaultControls, "slash");
        new StarStepAction(KeyEvent.VK_I).addToControls(defaultControls, "starstep");

        new InteractAction(KeyEvent.VK_E).addToControls(defaultControls, "interact");

        new MapAction(KeyEvent.VK_TAB).addToControls(defaultControls, "map");
        new MapAction(KeyEvent.VK_T).addToControls(defaultControls, "map");


        setActiveControls(defaultControls);
    }

    /** disables all but constantControls */
    public static void disableControls() { setActiveControls(new HashMap<>()); }

    public static void addKeybind(HashMap<KeyStroke, String> controlsList, int keyCode, Action action, boolean onKeyPress, String actionCode){
        KeyStroke keystroke = KeyStroke.getKeyStroke(keyCode, 0, !onKeyPress);
        if (!onKeyPress) actionCode += " released";

        ActionMap actionMap = Canvas.panel.getActionMap();

        controlsList.put(keystroke, actionCode);
        actionMap.put(actionCode, action);
    }

    /** adjust inputmap */
    public static void setActiveControls(HashMap<KeyStroke, String> controlsList){
        
        // if switching away from default controls, stop the player's movement
        if (activeControls != null && defaultControls != null && activeControls.equals(defaultControls))
            PlayerMovementControls.stopMoving();
        
        InputMap inputMap = Canvas.panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.clear();

        for (KeyStroke key : controlsList.keySet())
            inputMap.put(key, controlsList.get(key));
        
        for (KeyStroke key : constantControls.keySet())
            inputMap.put(key, constantControls.get(key));

        activeControls = controlsList;
    }

}
