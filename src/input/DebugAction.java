package src.input;

import javax.swing.*;

import src.draw.Canvas;

public class DebugAction extends KeyPress {

    Runnable onKeyPress;
    Runnable onKeyRelease;

    public DebugAction(int keyCode, Runnable onKeyPress, Runnable onKeyRelease) { 
        super(keyCode); 
        this.onKeyPress = onKeyPress;
        this.onKeyRelease = onKeyRelease;

        this.addToControls(PlayerControls.constantControls, "debug" + keyCode);

        InputMap inputMap = Canvas.panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        for (KeyStroke key : PlayerControls.constantControls.keySet())
            inputMap.put(key, PlayerControls.constantControls.get(key));

    }
    public DebugAction(int keyCode, Runnable onKeyPress) { 
        this(keyCode, onKeyPress, null);
    } 

    @Override
    public void onKeyPress() { onKeyPress.run(); }
    
    @Override
    public void onKeyRelease() { if (onKeyRelease != null) onKeyRelease.run(); }

}
