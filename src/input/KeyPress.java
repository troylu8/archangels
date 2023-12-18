package src.input;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.*;

import src.draw.Canvas;

/** bundles key down and key up action together, 
     * ignores multiple keydown inputs when holding down key */
public abstract class KeyPress {

    private int keyCode;

    private boolean keyPressed;

    public KeyPress(int keyCode) {
        this.keyCode = keyCode;
        keyPressed = false;
    }

    public void addToControls(HashMap<KeyStroke, String> controls) {
        
        ActionMap actionMap = Canvas.panel.getActionMap();

        controls.put(KeyStroke.getKeyStroke(keyCode, 0, false), "" + keyCode);
        actionMap.put("" + keyCode, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (keyPressed) return;
                keyPressed = true;

                onKeyPress();
            }
        } );

        controls.put(KeyStroke.getKeyStroke(keyCode, 0, true), keyCode + " released");
        actionMap.put(keyCode + " released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!keyPressed) return;
                keyPressed = false;

                onKeyRelease();
            }
        } );
    }

    public abstract void onKeyPress();
    public abstract void onKeyRelease();
}