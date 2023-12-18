package src.input;

/** bundles key down and key up action together*/
public abstract class KeyPressAction {
    public abstract void onKeyPress();
    public abstract void onKeyRelease();
}