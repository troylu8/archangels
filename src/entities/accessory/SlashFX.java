package src.entities.accessory;

import src.entities.Being;
import src.manage.Clock;

public class SlashFX extends Accessory {

    public static SlashFX createSlash(Being following, double size, double rotation, boolean accented) {
        SlashFX slash;
        if (accented)   {
            slash = new SlashFX("red slash accented.gif", following, size, rotation);
            slash.clockAffectedLevel = Clock.INCLUDING_PLAYER;

            
            slash.animation.addFrameHook(6, () -> { 
                slash.animation.pause(); 
                slash.clockAffectedLevel = Clock.NON_PLAYER_ENTITIES;
                slash.waitTilUnaffectedByClock();
                slash.animation.frame++;
                slash.animation.play();
            });
        }
        else slash = new SlashFX("red slash no startup.gif", following, size, rotation);
        
        slash.enable();
        return slash;
    }

    private SlashFX(String spriteFilename, Being following, double size, double rotation) {
        super(spriteFilename, following, 0.0, false);
        setSize(size);
        setRotation(rotation);
    }
    
}
