package src.entities.accessory;

import src.entities.Being;

public class SlashFX extends Accessory {

    public SlashFX(Being following, double size, double rotation) {
        super("red slash.gif", following, 0.0, false);
        setSize(size);
        setRotation(rotation);
    }
    
}
