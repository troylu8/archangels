package src.input;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.*;

import src.draw.Canvas;

public class KeyBindManager {

    public static HashMap<String, KeyBindManager> allKeyBindManagers = new HashMap<>();

    public final String ACTION_CODE;
    private int keyCode;
    private KeyPressAction keyPressAction;

    private boolean keyPressed;

    public KeyBindManager(String actionCode, int keyCode) {
        ACTION_CODE = actionCode;

        keyPressed = false;

        this.keyCode = keyCode;
        setKeyCode(keyCode);

        ActionMap actionMap = Canvas.panel.getActionMap();

        actionMap.put(actionCode, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (keyPressed || keyPressAction == null) return;
                keyPressed = true;

                keyPressAction.onKeyPress();
            }
        } );
        actionMap.put(actionCode + " released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!keyPressed || keyPressAction == null) return;
                keyPressed = false;

                keyPressAction.onKeyRelease();
            }
        } );

        allKeyBindManagers.put(actionCode, this);
    }

    public void setKeyCode(int keyCode) {
        InputMap inputMap = Canvas.panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.remove(KeyStroke.getKeyStroke(this.keyCode, 0, false));
        inputMap.remove(KeyStroke.getKeyStroke(this.keyCode, 0, true));

        this.keyCode = keyCode;

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), ACTION_CODE);
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), ACTION_CODE + " released");
    }
    public int getKeyCode() { return keyCode; }

    public void setKeyPressAction(KeyPressAction keyPressAction) {
        this.keyPressAction = keyPressAction;
    }
    public KeyPressAction getKeyPressAction() { return keyPressAction; }
}
