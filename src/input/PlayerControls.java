package src.input;

import java.util.*;
import java.awt.event.*;

public class PlayerControls {
    
    /** always active regardless of current activeControls*/
    public static HashMap<String, KeyPressAction> constantControls = new HashMap<>();
    public static HashMap<String, KeyPressAction> defaultControls = new HashMap<>();

    public static void init() {
        System.out.println("a");

        new KeyBindManager("dash", KeyEvent.VK_K);
        new KeyBindManager("slash", KeyEvent.VK_J);
        new KeyBindManager("starstep", KeyEvent.VK_I);
        new KeyBindManager("interact", KeyEvent.VK_E);
        new KeyBindManager("map", KeyEvent.VK_T);

        PlayerMovementControls.addMovementControls(defaultControls);
        defaultControls.put("dash", new DashAction());
        defaultControls.put("slash", new SlashAction());
        defaultControls.put("starstep", new StarStepAction());
        defaultControls.put("interact", new InteractAction());
        defaultControls.put("map", new MapAction());

        setKeyBinds(defaultControls);
    }

    /** disables all but constantControls */
    public static void disableControls() { setKeyBinds(new HashMap<>()); }

    public static void setKeyBinds(HashMap<String, KeyPressAction> controlsList){        

        for (String actionCode : KeyBindManager.allKeyBindManagers.keySet()) {

            KeyBindManager keyBindManager = KeyBindManager.allKeyBindManagers.get(actionCode);
            if (!keyBindManager.ACTION_CODE.contains("debug")) {
                keyBindManager.setKeyPressAction(
                    (controlsList.containsKey(actionCode))? controlsList.get(actionCode) : null
                );
            } else System.out.println(keyBindManager);
            
        }

    }

}
