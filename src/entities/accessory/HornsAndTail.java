package src.entities.accessory;

import src.entities.Player;

public class HornsAndTail extends Accessory {

    public HornsAndTail() {
        super("horns and tail 1.png", Player.player, 0, true);
        visible = false;
        setSize(4);

        disableOnRoomChange = false;
    }

    @Override
    public void enable() {
        super.enable();
        System.out.println(visible);
    }

    public void setStage(int stage) {
        if (stage == 0) 
            visible = false;
        else {
            visible = true;
            setSprite("horns and tail " + stage + ".png");
        }
    }
    

}
