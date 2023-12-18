package src.input;


public class DebugAction extends KeyPressAction {

    Runnable onKeyPress;
    Runnable onKeyRelease;

    public DebugAction(int keyCode, Runnable onKeyPress, Runnable onKeyRelease) { 
        this.onKeyPress = onKeyPress;
        this.onKeyRelease = onKeyRelease;

        new KeyBindManager("debug " + keyCode, keyCode).setKeyPressAction(this);
    }
    public DebugAction(int keyCode, Runnable onKeyPress) { 
        this(keyCode, onKeyPress, null);
    } 

    @Override
    public void onKeyPress() { onKeyPress.run(); }
    
    @Override
    public void onKeyRelease() { if (onKeyRelease != null) onKeyRelease.run(); }

}
