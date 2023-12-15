package src.entities.accessory;

import src.entities.*;

public class Accessory extends Entity {

    public Being wearer;
    public double followDelay;
    public boolean headingLinked;

    public Accessory(String spriteFilename, Being wearer, double followDelay, boolean headingLinked) {
        super(spriteFilename, wearer.x, wearer.y);

        this.wearer = wearer;
        this.followDelay = followDelay;
        this.headingLinked = headingLinked;

        wearer.accessories.queueToAdd(this);
    }
    public Accessory(String spriteFilename, Being wearer) {
        this(spriteFilename, wearer, 0.1, false);
    }

    @Override
    public void update(long deltaTime) {
        if (followDelay == 0)   setPosition(wearer.x, wearer.y);
        else                    transform((wearer.x - x) * followDelay, (wearer.y - y) * followDelay);
    }
    
}
