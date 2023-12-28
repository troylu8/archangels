package src.entities.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

import src.draw.Canvas;
import src.entities.*;
import src.manage.Clock;
import src.util.Util;

public class UI extends Entity {

    public static final Font BASE_FONT = new Font("arial", Font.PLAIN, 20); 
    public static Font font = BASE_FONT.deriveFont(1f);

    public static Group<UI> allUI = new Group<>("allUI");

    /** {0.5, 0.5} means always at the center of the screen */
    double[] ref;

    /** if ref == {0.5, 1} (centered right edge), and offset == {-10, 0}, 
     * this ui will be drawn with a 10px gap between its right side and the edge of the camera.
     * (offset is shift in position after ui is positioned according to ref) */
    double[] offset;


    private double localCAMratio = 1;
    
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
        newX += offset[0] * Canvas.CAMratio;

        double newY = Canvas.camera.height * ref[1];
        newY = Util.clamp(newY, 0 + spriteBounds.getHeight() * getAnchorY(), Canvas.camera.height - (spriteBounds.getHeight() * (1 - getAnchorY())) );
        newY += offset[1] * Canvas.CAMratio;

        setPosition(newX, newY);
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
    public void setSize(double multiple) {
        if (sprite == null) {
            this.size = multiple;
            return;
        }
        if (multiple <= 0) return;

        int[] newBounds = {
            (int) Math.ceil(originalSprite.getWidth() * multiple * Canvas.CAMratio), 
            (int) Math.ceil(originalSprite.getHeight() * multiple * Canvas.CAMratio)
        };

        if (newBounds[0] <= 0 || newBounds[1] <= 0) {
            System.out.println("new size is <= 0");
            return;
        }

        this.size = multiple;

        spriteBounds.setSizeAndCenter(
            (int) (originalSprite.getWidth() * multiple), 
            (int) (originalSprite.getHeight() * multiple),
        x, y);
        
        Image newImg = originalSprite.getScaledInstance(newBounds[0], newBounds[1],  Image.SCALE_DEFAULT);
        localCAMratio = Canvas.CAMratio;
        
        sprite = new BufferedImage(newBounds[0], newBounds[1], BufferedImage.TYPE_INT_ARGB);

        Graphics g = sprite.getGraphics();
        g.drawImage(newImg, 0, 0, null);
        g.dispose();

        /** for ui, spritebounds is directly equal to sprite size */
        if (sprite != null) spriteBounds.setSizeAndCenter(sprite.getWidth(), sprite.getHeight(), x, y);
    }
    
    @Override
    public void draw(Graphics2D g) {

        if (localCAMratio != Canvas.CAMratio) 
            setSize(size);
        
        localFOVratio = Canvas.FOVratio;
        super.draw(g);
    }
}
