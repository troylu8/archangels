package src.entities;

import java.util.ArrayList;

import src.input.Interactable;
import src.world.*;

public class Gate extends Entity implements Interactable {

    public static ArrayList<Gate> allGates = new ArrayList<>();

    private static boolean open;

    int[] dir;

    /** sprite must be a gif */
    public Gate(String spriteFilename, int tileX, int tileY, int[] dir) {
        super("gates\\" + spriteFilename, -1, -1, 0.5, 1, 3.5);

        this.dir = dir;

        open = false;

        animation.playOnEnable = false;
        animation.disableAfterAnimation = false;

        // center on tile
        setPosition(LandTiles.getTrueVal(tileX) + LandTiles.TILE_SIZE/2, LandTiles.getTrueVal(tileY + 1));
        setDrawLayer(0);
    }

    public static void open() {
        open = true;

        for (Gate g : allGates)
            g.animation.play();
    }
    public boolean isOpen() { return open; }

    @Override
    public void enable() {
        super.enable();
        Interactable.allInteractables.queueToAdd(this);
        allGates.add(this);

        visible = true;
    }

    @Override
    public void disable() {
        super.disable();
        Interactable.allInteractables.queueToRemove(this);
        allGates.remove(this);
    }

    @Override
    public void onInteract() {
        double temp = Player.player.speedMultiplier;
        Player.player.speedMultiplier = 0;
        // TODO: transition
        Player.player.speedMultiplier = temp;

        if (open) {

            RoomNode.enterNextRoom(dir[0], dir[1], dir[2]);

            int[] opposite = RoomNode.getOppositeHexDirection(dir);
            for (Gate g : allGates) {
                if (g.dir[0] == opposite[0] && g.dir[1] == opposite[1] && g.dir[2] == opposite[2]) {
                    Player.player.setPosition(g.x, g.y);
                }
            }
            

            Gate.open();
        
        }
        else        System.out.println("gate closed");
    }

    @Override
    public String getInteractTagFilename() { return "enter.png"; }
    
}
