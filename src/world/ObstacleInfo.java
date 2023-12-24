package src.world;

import java.awt.*;

import src.entities.Entity;
import src.manage.Main;
import src.draw.Canvas;

public class ObstacleInfo {
    String spriteFilename;

    /** this area must be on land for the obstacle to fit, not always same as tileHitboxSize */
    int[] landArea;

    boolean[][] tileHitbox = null; 

    public ObstacleInfo(String spriteFilename, int width, int height, boolean[][] tileHitbox) {
        this.spriteFilename = "obstacles\\" + spriteFilename;
        this.landArea = new int[] {width, height};
        this.tileHitbox = tileHitbox;
    }
    public ObstacleInfo(String spriteFilename, int width, int height) {
        this(spriteFilename, width, height, null);
    }

    /** lower left tile */
    public void placeObstacleAt(int tileX, int tileY) {

        if (tileHitbox != null) {
            for (int y = 0; y < tileHitbox.length; y++) {
                for (int x = 0; x < tileHitbox[0].length; x++) {

                    if (tileHitbox[y][x] && RoomNode.inBounds(tileX + x, tileY - y))
                        RoomNode.map[tileY - y][tileX + x] = RoomNode.WALL;

                }
            }
        }

        Entity obstacle = new Entity(spriteFilename,
        (LandTiles.getTrueVal(tileX) + LandTiles.getTrueVal(tileX + landArea[0])) / 2,
        LandTiles.getTrueVal(tileY + 1), 
        0.5, 1, 4);
        obstacle.setDrawLayer(Entity.OBSTACLE_LAYER);
        obstacle.enable();

        new ObstacleDebug(this, LandTiles.getTrueVal(tileX), LandTiles.getTrueVal(tileY) + LandTiles.TILE_SIZE).enable();

    }


    /** y and x are lower left corner 
     * if theres a hole inside the corners of the obstacle, will return true. the obstacle will cover the hole anyways 
    */
    public boolean fitsHere(int x, int y) {
        final int[][] corners = { 
            {y, x}, 
            {y - landArea[1], x}, 
            {y, x + landArea[0]}, 
            {y - landArea[1], x + landArea[0]}
        };

        for (int[] c : corners) {
            if (!RoomNode.inBounds(c[0], c[1]) || RoomNode.map[c[0]][c[1]] != RoomNode.LAND) {
                return false;
            }
        }
        return true;
    }

}

class ObstacleDebug extends Entity {

    ObstacleInfo ob;

    public ObstacleDebug(ObstacleInfo obstacleInfo, int x, int y) {
        super("none", x, y);
        this.ob = obstacleInfo;
    }

    @Override
    public void draw(Graphics2D g) {
        if (Main.drawHitboxes) {

            int[] posOnCamera = getDrawPos();

            g.setColor(Color.DARK_GRAY);
            g.drawRect(posOnCamera[0], (int) (posOnCamera[1] - LandTiles.getTrueVal(ob.landArea[1]) * Canvas.FOVratio), 
                (int) (LandTiles.getTrueVal(ob.landArea[0]) * Canvas.FOVratio), 
                (int) (LandTiles.getTrueVal(ob.landArea[1]) * Canvas.FOVratio)
            );


            g.setColor(Color.BLUE);

            if (ob.tileHitbox != null) {
                for (int height = 0; height < ob.tileHitbox.length; height++) {
                    for (int width = 0; width < ob.tileHitbox[0].length; width++) {

                        if (ob.tileHitbox[height][width]) {
                            g.drawRect(
                                posOnCamera[0] + (int) (LandTiles.getTrueVal(width) * Canvas.FOVratio), 
                                (int) (posOnCamera[1] + LandTiles.getTrueVal(height) - LandTiles.TILE_SIZE * Canvas.FOVratio), 
                                (int) (LandTiles.TILE_SIZE * Canvas.FOVratio), 
                                (int) (LandTiles.TILE_SIZE * Canvas.FOVratio)
                            );
                        }

                    }
                }
            }

        }
    }

}