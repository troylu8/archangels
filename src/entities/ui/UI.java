package src.entities.ui;

import java.awt.*;

import src.draw.Canvas;
import src.entities.*;
import src.manage.Clock;
import src.util.Util;

public class UI extends Entity {

    public static final Font BASE_FONT = new Font("arial", Font.PLAIN, 20); 
    public static Font font = BASE_FONT.deriveFont(1f);

    public static Group<UI> allUI = new Group<>();

    /** {0.5, 0.5} means always at the center of the screen */
    double[] ref;

    /** if ref == {0.5, 1} (centered right edge), and offset == {-10, 0}, 
     * this ui will be drawn with a 10px gap between its right side and the edge of the camera.
     * (offset is shift in position after ui is positioned according to ref) */
    double[] offset;
    
    public UI(String spriteFilename, double size, double refX, double refY, double offsetX, double offsetY) {
        super(spriteFilename, -1, -1,  size);
        ref = new double[] {refX, refY};
        offset = new double[] {offsetX, offsetY};

        disableOnRoomChange = false;
        clockAffectedLevel = Clock.INCLUDING_UI;
    }

    @Override
    public void setSprite(String spriteFilename) { super.setSprite("ui\\" + spriteFilename); }

    public void updatePos() {
        
        double newX = Canvas.camera.width * ref[0];
        newX = Util.clamp(newX, 0 + spriteBounds.getWidth() * getAnchorX(), Canvas.camera.width - (spriteBounds.getWidth() * (1 - getAnchorX())) );
        newX += offset[0] * Canvas.FOVratio;

        double newY = Canvas.camera.height * ref[1];
        newY = Util.clamp(newY, 0 + spriteBounds.getHeight() * getAnchorY(), Canvas.camera.height - (spriteBounds.getHeight() * (1 - getAnchorY())) );
        newY += offset[1] * Canvas.FOVratio;

        setPosition(newX, newY);
    }

    /** for ui, spritebounds is directly equal to sprite size */
    @Override
    public void setSize(double multiple) {
        super.setSize(multiple);
        if (sprite != null) spriteBounds.setSize(sprite.getWidth(), sprite.getHeight(), x, y);
    }

    @Override
    public void enable() {
        super.enable();
        allUI.queueToAdd(this);
        updatePos();
    }
    @Override
    public void disable() {
        super.disable();
        allUI.queueToRemove(this);
    }

    @Override
    protected int[] getDrawPos() { return Canvas.getDrawPosUI(x, y); }
    
    @Override
    public void draw(Graphics2D g) {
        localFOVratio = Canvas.FOVratio;
        super.draw(g);
    }
}
