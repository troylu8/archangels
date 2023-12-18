package src.entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;

import src.draw.Canvas;
import src.entities.ui.UI;
import src.manage.*;
import src.util.Util;
import src.shapes.RectShape;

import javax.imageio.ImageIO;

public class Entity {

    public static Group<Entity> allEntities = new Group<>();
    
    /**
     * <p> higher layers are painted on top
     * <p> 4 - interact tag
     * <p> 3 - obstacle
     * <p> 2 - default
     * <p> 1 - accessory
     * <p> 0 - gates
     * <p> -1 - tint
     */
    public static HashMap<Integer, Group<Entity>> entityLayers = new HashMap<>();

    public String spriteFilename;

    public Animation animation = null;
    /** do after animation finishes, before disabling */
    public Runnable endHook;

    private BufferedImage originalSprite;
    public BufferedImage sprite;
    
    /** latest FOVratio that this entity has been updated to; if localFOVratio != Canvas.FOVratio, must update drawnSprite */
    protected double localFOVratio;

    /** size of sprite before FOVratio (on the same level as Canvas.fov) */
    public RectShape spriteBounds;
    private double size;

    public boolean visible;
    public float opacity;

    /** where sprite is draw relative to pos, (0-1): 0.5 = centered */ 
    private double[] anchor;

    public double x;
    public double y;
    
    private double rotation;

    private int heading;
    private boolean headingLocked;

    public boolean disableOnRoomChange;

    /* if this entity is visible and included in groups */
    public boolean enabled = false;
    private int drawLayer = 2;

    /** clock speed/pausing only takes effect if Clock.pauseState >= clockAffectedLevel  */
    public int clockAffectedLevel;

    public Entity(String spriteFilename, double x, double y, double anchorX, double anchorY, double size) {
        this.spriteFilename = spriteFilename;
        spriteBounds = new RectShape(anchorX, anchorY);

        visible = true;
        opacity = 1;
        rotation = 0;
        heading = 1;
        headingLocked = false;
        localFOVratio = 1;
        disableOnRoomChange = true;

        clockAffectedLevel = Clock.NON_PLAYER_ENTITIES;

        setAnchor(anchorX, anchorY);
        this.x = x;
        this.y = y;
        
        this.size = size;

        if (spriteFilename.length() > 3 && spriteFilename.substring(spriteFilename.length() - 3).equals("gif")) {
            animation = new Animation(spriteFilename, this);
        } else {
            setSprite(spriteFilename);
            setSize(size);
        }
        
    }
    public Entity(String spriteFilename, double x, double y, double size) {
        this(spriteFilename, x, y, 0.5, 0.5, size);
    }
    public Entity(String spriteFilename, double x, double y) {
        this(spriteFilename, x, y, 0.5, 0.5, 1);
    }

    public static void clearEmptyEntityLayers() {
        int[] trash = new int[entityLayers.size()];
        int i = 0;

        synchronized(entityLayers) {
            for (Entry<Integer, Group<Entity>> layer : entityLayers.entrySet()) {
                if (layer.getValue().set.isEmpty()) 
                    trash[i++] = layer.getKey();
            }
            for (int j = 0; j < i; j++) 
                entityLayers.remove(trash[j]);
        }
    }

    public void setDrawLayer(int layer) {
        if (this.drawLayer == layer) return;
        removeFromLayerGroup();
        drawLayer = layer;
        if (enabled) addToLayerGroup();
    }
    protected void addToLayerGroup() {
        Group<Entity> layerGroup = entityLayers.getOrDefault(drawLayer, new Group<Entity>());
        layerGroup.queueToAdd(this);

        synchronized(entityLayers) { entityLayers.put(drawLayer, layerGroup); }
    } 
    protected void removeFromLayerGroup() {
        Group<Entity> layerGroup = entityLayers.get(drawLayer);
        if (layerGroup == null) return;
        layerGroup.queueToRemove(this);
    }  
    public int getDrawLayer() { return drawLayer; }

    //TODO: x and y getters setters
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        spriteBounds.centerSpriteAround(x, y);
    }

    public void transform(double x, double y) {
        setPosition(this.x + x, this.y + y);
    }

    public double getAnchorX() { return anchor[0]; }
    public double getAnchorY() { return anchor[1]; }
    public void setAnchor(double anchorX, double anchorY) { 
        anchor = new double[] {anchorX, anchorY};
        spriteBounds.setAnchor(anchorX, anchorY);
    }
    
    public boolean affectedByClock() { return Clock.getPauseState() >= clockAffectedLevel; }
    public void waitTilUnaffectedByClock() {
        Clock.waitTilUnaffected(clockAffectedLevel);
    }

    /** heading > 0 == right, heading < 0 == left */
    public void setHeading(double heading) {
        if (!headingLocked) 
            this.heading = (int) Math.signum(heading);
    }
    public void lockHeading() { headingLocked = true; }
    public void unlockHeading() { headingLocked = false; }
    public boolean getHeadingLocked() { return headingLocked; }
    
    public int getHeading() { return heading; }

    public void setRotation(double radians) { 
        rotation = radians % (2 * Math.PI); 
        spriteBounds.setRotation(radians);
    }
    public double getRotation() { return rotation; }
    
    public void rotate(double radiansCW) { setRotation(rotation + radiansCW); }

    /** if sprite == null, checks if pos is in fov */
    public boolean onCamera() {
        return visible && ((sprite != null)? spriteBounds.intersects(Canvas.fov) : Canvas.fov.contains(x, y));
    }
    

    public void setSprite(String spriteFilename) {
        this.spriteFilename = spriteFilename;
        
        // if last 4 letters are "none"
        int len = spriteFilename.length();
        if (len >= 4 && spriteFilename.substring(len-4, len).equals("none")) {
            this.originalSprite = null;
            return;
        }

        File f = new File("game files\\sprites\\" + spriteFilename); 

        if (!f.isFile()) 
            f = new File("game files\\sprites\\no img.png");
        else {
            try {
                setSprite(ImageIO.read(f));
            } 
            catch (IOException e) { e.printStackTrace(); }
        }
        
    }
    public void setSprite(BufferedImage sprite) {
        this.originalSprite = sprite;
        this.sprite = sprite;
        setSize(size);
    }
    public BufferedImage getSprite() { return sprite; }

    /** set sprite as a copy of originalSprite with a different size */
    public void setSize(double multiple) {
        if (sprite == null) {
            this.size = multiple;
            return;
        }
        if (multiple <= 0) return;

        int[] newBounds = {
            (int) Math.ceil(originalSprite.getWidth() * multiple * Canvas.FOVratio), 
            (int) Math.ceil(originalSprite.getHeight() * multiple * Canvas.FOVratio)
        };

        if (newBounds[0] <= 0 || newBounds[1] <= 0) {
            System.out.println("new size is <= 0");
            return;
        }

        this.size = multiple;

        spriteBounds.setSize(
            (int) (originalSprite.getWidth() * multiple), 
            (int) (originalSprite.getHeight() * multiple),
        x, y);
        
        Image newImg = originalSprite.getScaledInstance(newBounds[0], newBounds[1],  Image.SCALE_DEFAULT);
        localFOVratio = Canvas.FOVratio;
        
        sprite = new BufferedImage(newBounds[0], newBounds[1], BufferedImage.TYPE_INT_ARGB);

        Graphics g = sprite.getGraphics();
        g.drawImage(newImg, 0, 0, null);
        g.dispose();

    }
    public double getSize() { return size; }

    public void enable() {
        if (enabled) return;

        addToAllEntities();

        if (animation != null && animation.playOnEnable)
            animation.play();
    }
    public void disable() {
        if (!enabled) return;

        removeFromAllEntites();

        if (animation != null) 
            animation.dispose();
    }

    public void addToAllEntities() {
        enabled = true;
        allEntities.queueToAdd(this);
        addToLayerGroup();
    }
    public void removeFromAllEntites() {
        enabled = false;
        allEntities.queueToRemove(this);
        removeFromLayerGroup();
    }

    protected int[] getDrawPos() { return Canvas.getDrawPosWorld(x, y); }

    // paints sprite centered around its pos
    public void draw(Graphics2D g) {
        if (sprite == null) return;

        // if FOVratio outdated, generate sprite to the right size
        if (localFOVratio != Canvas.FOVratio) 
            setSize(size);

        int[] posOnCamera = getDrawPos();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        AffineTransform backup = g.getTransform();

        // move paper so that upper left corner is center of sprite
        g.translate(posOnCamera[0], posOnCamera[1]);
        
        g.rotate(rotation); 

        // if cornerDrawPos[0] == -5, then img is drawn 5px left of paper's upper left corner
        int[] cornerDrawPos = {(int)( -sprite.getWidth() * ((heading > 0)? anchor[0] : 1 - anchor[0]) * heading), (int) ( -sprite.getHeight() * anchor[1])};
        g.drawImage(sprite,  cornerDrawPos[0], cornerDrawPos[1],  sprite.getWidth() * heading, sprite.getHeight(), null);

        g.setTransform(backup);
        
        // draw sprite bounds
        if (Main.drawSpriteBounds) {               
            g.setColor(Color.ORANGE);
            g.fillOval(posOnCamera[0] - 5, posOnCamera[1] - 5, 10, 10);
                  
            g.setColor((this instanceof UI)? Color.GREEN : Color.RED);
            spriteBounds.draw(g, this instanceof UI);
        }

    }

    public void faceTowards(double destX, double destY) {
        if (destX == x) {
            if (destY > y)  setRotation(Math.PI / 2);
            else            setRotation(3 * Math.PI / 2);
            return;
        }
        setRotation(Math.atan( (destY - y) / (destX - x) ));
        setHeading(destX - x);
    }

    public double[] getVectorTowards(double destX, double destY, int length) {
        double[] res = {0,0};

        double[] displacement = { destX - this.x, destY - this.y };

        double theta = Util.directionToTheta(displacement[0], displacement[1]);
        
        res[0] = Math.abs(Math.cos(theta)) * Math.signum(displacement[0]) * length;
        res[1] = Math.abs(Math.sin(theta)) * Math.signum(displacement[1]) * length;

        return res;
    }

    /** deltaTime is in milliseconds */
    public void update(long deltaTime) {}

    @Override
    public String toString() {
        return this.getClass() + " " + spriteFilename + " ( " + super.toString().split("@")[1] + " )\n";
    }

}