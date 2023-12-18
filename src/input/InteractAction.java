package src.input;

import src.entities.Player;

public class InteractAction extends KeyPressAction {

    @Override
    public void onKeyPress() {
        if (Player.focusedInteractable != null)
            Player.focusedInteractable.onInteract();
    }

    @Override
    public void onKeyRelease() {}
}