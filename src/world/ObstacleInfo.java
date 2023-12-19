package src.world;

import src.entities.Entity;

public class ObstacleInfo {
    String spriteFilename;
    int[] tileHitboxSize;
    boolean[][] tileHitbox = null; 

    public ObstacleInfo(String spriteFilename, boolean[][] tileHitbox) {
        this.spriteFilename = "obstacles\\" + spriteFilename;
        this.tileHitbox = tileHitbox;
        tileHitboxSize = new int[] {tileHitbox[0].length, tileHitbox.length};
    }
    public ObstacleInfo(String spriteFilename, int width, int height) {
        this.spriteFilename = "obstacles\\" + spriteFilename;
        this.tileHitboxSize = new int[] {width, height};
    }

    /** lower left tile */
    public void placeObstacleAt(int tileX, int tileY) {

        for (int y = 0; y < tileHitboxSize[1]; y++) {
            for (int x = 0; x < tileHitboxSize[0]; x++) {

                boolean addWallHere = tileHitbox == null || tileHitbox[tileY - y][tileX + x];

                if (addWallHere)
                    RoomNode.map[tileY - y][tileX + x] = RoomNode.WALL;

            }
        }

        Entity obstacle = new Entity(spriteFilename,
        (LandTiles.getTrueVal(tileX) + LandTiles.getTrueVal(tileX + tileHitboxSize[0])) / 2,
        LandTiles.getTrueVal(tileY + 1), 
        0.5, 1, 4);
        obstacle.setDrawLayer(1);
        obstacle.enable();
    }
}
