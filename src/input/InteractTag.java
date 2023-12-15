package src.input;

import src.entities.Entity;
import src.entities.Player;

public class InteractTag extends Entity {

    /** only 1 active interactTag allowed */
    static InteractTag activeTag;

    private InteractTag(String spriteFilename) {
        super(spriteFilename, -1, -1);
    }

    public static void moveTagTo(Interactable interactable) {
        if (activeTag != null)
            activeTag.disable();
        
        if (interactable == null) activeTag = null;
        else {
            activeTag = new InteractTag(interactable.getInteractTagFilename());
            activeTag.enable();
        }
    }

    @Override
    public void enable() {
        super.enable();

        double[] pos = Interactable.getPos(Player.focusedInteractable);
        setPosition(pos[0], pos[1] - 100);
    }

}
