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

    Player player;

    public static PlayerMovementControls playerMovementControls;

    public PlayerControls(Player player){

        this.player = player;

        playerMovementControls = new PlayerMovementControls(player);

        new DashAction(KeyEvent.VK_K).addToControls(defaultControls);
        new SlashAction(KeyEvent.VK_J).addToControls(defaultControls);
        new StarStepAction(KeyEvent.VK_I).addToControls(defaultControls);

        new InteractAction(KeyEvent.VK_E).addToControls(defaultControls);

        new MapAction(KeyEvent.VK_TAB).addToControls(defaultControls);
        new MapAction(KeyEvent.VK_T).addToControls(defaultControls);


        setActiveControls(defaultControls);
    }

    /** disables all but constantControls */
    public static void disableControls() { setActiveControls(new HashMap<>()); }

    public static void addKeybind(HashMap<KeyStroke, String> controlsList, int keyCode, Action action, boolean onKeyPress){
        KeyStroke keystroke = KeyStroke.getKeyStroke(keyCode, 0, !onKeyPress);
        String actionMapKey = String.valueOf(keyCode);
        if (!onKeyPress) actionMapKey += " released";

        ActionMap actionMap = Canvas.panel.getActionMap();

        controlsList.put(keystroke, actionMapKey);
        actionMap.put(actionMapKey, action);
    }

    /** adjust inputmap */
    public static void setActiveControls(HashMap<KeyStroke, String> controlsList){
        
        // if switching away from default controls, stop the player's movement
        if (activeControls != null && defaultControls != null && activeControls.equals(defaultControls))
            playerMovementControls.stopMoving();
        
        InputMap inputMap = Canvas.panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.clear();

        for (KeyStroke key : controlsList.keySet())
            inputMap.put(key, controlsList.get(key));
        
        for (KeyStroke key : constantControls.keySet())
            inputMap.put(key, constantControls.get(key));

        activeControls = controlsList;
    }

}
