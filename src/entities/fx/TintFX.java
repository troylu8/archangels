package src.entities.fx;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import src.draw.Canvas;
import src.entities.Entity;
import src.entities.Player;
import src.manage.Clock;
import src.util.Util;

public class TintFX extends Entity {

    int timeBeforeFading;

    public static final Color COLOR = new Color(34, 32, 52);

    public TintFX(int timeBeforeFading) {
        super("none", Player.player.x, Player.player.y);
        opacity = 0.5f;
        setDrawLayer(TINT_LAYER);
        this.timeBeforeFading = timeBeforeFading;
    }

    Thread fadeThread = new Thread();

    @Override
    public void enable() {
        super.enable();

        fadeThread = new Thread(() -> {
            Util.sleepTilInterrupt(timeBeforeFading);

            for (; opacity > 0; opacity = Math.max(0, opacity - 0.05f) ) 
                Util.sleepTilInterrupt(Clock.adjustForClockSpeed(30));
            
            super.disable();
        }, "fade out then disable tint thread");
        fadeThread.start();

    }

    @Override
    public void disable() { fadeThread.interrupt(); }

    @Override
    public void update(long deltaTime) {
        setPosition(Player.player.x, Player.player.y);
        setSize(getSize() + 5 * deltaTime);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(COLOR);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        int[] drawPos = getDrawPos();
        g.fillOval((int)(drawPos[0] - getSize()/2), (int) (drawPos[1] - getSize()/2), (int) getSize(), (int) getSize());
    }
    
}
