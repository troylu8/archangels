package src.input;

import src.util.Util;
import src.entities.*;

public interface Interactable {
    
    static final int INTERACT_RANGE = 125;

    static final Group<Interactable> allInteractables = new Group<>();

    public abstract void onInteract();

    public abstract String getInteractTagFilename();

    public static double[] getPos(Interactable interactable) {
        Entity entityInfo = (Entity) interactable;
        return new double[] {entityInfo.x, entityInfo.y};
    }

    public static boolean same(Interactable a, Interactable b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    };

    public static void updateFocusedInteractable() {
        Interactable prev = Player.focusedInteractable;

        double closestDist = INTERACT_RANGE;

        synchronized (allInteractables) {

            for (Interactable i : allInteractables) {

                double[] pos = getPos(i);

                double dist = Util.dist(pos[0], pos[1], Player.player.x, Player.player.y);
                
                if ( dist < closestDist )  {
                    Player.focusedInteractable = i;
                    closestDist = dist;
                }
            }
        }

        if (closestDist == INTERACT_RANGE)
            Player.focusedInteractable = null;   

        if (same(prev, Player.focusedInteractable)) return;

        InteractTag.moveTagTo(Player.focusedInteractable);
        

    }

}




