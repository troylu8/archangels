package src.input;

import src.entities.Player;

public class InteractAction extends KeyPress {
    
    public InteractAction(int keyCode) {
        super(keyCode);
    }

    @Override
    public void onKeyPress() {
        if (Player.focusedInteractable != null)
            Player.focusedInteractable.onInteract();
    }

    @Override
    public void onKeyRelease() {}
}