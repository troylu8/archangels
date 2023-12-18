package src.entities.accessory;

import src.entities.Player;

public class ScytheAccessory extends Accessory {

    public ScytheAccessory() {
        super("scythe.png", Player.player, 0.1, true);
        
        setSize(3);
    }

    @Override
    public void setHeading(double heading) {
        heading = -Math.signum(heading);
        super.setHeading(heading);
        setRotation(Math.toRadians(heading * 30));
        setAnchor(0.5 + -heading * 0.3, 0.5);
    }

}
