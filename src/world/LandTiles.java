package src.world;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;

import src.draw.Canvas;

public class LandTiles {

    public static final int TILE_SIZE = 64;
    public static int[] tileDrawSize = {64, 64};
    private static double localFOVratio = 1; 


    // center, side, corner, corner inverted
    static final int CENTER = '.';
    static final int SIDE = 'S';
    static final int CORNER = 'c';
    static final int CORNER_INVERTED = 'C';

    // rotationMap[y][x][1] == # of times to rotate 90 degrees cw
    static int[][][] rotationMap;

    Image center;
    Image side;
    Image corner;
    Image corner_inverted;

    public LandTiles(String landTilesFolder) {
        landTilesFolder = "game files\\sprites\\tiles\\" + landTilesFolder;
        center = getTileImage(landTilesFolder + "\\center.png");
        side = getTileImage(landTilesFolder + "\\side.png");
        corner = getTileImage(landTilesFolder + "\\corner.png");
        corner_inverted = getTileImage(landTilesFolder + "\\corner inverted.png");
    }

    private Image getTileImage(String filepath) {
       return new ImageIcon(filepath).getImage().getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_DEFAULT);
    }

    public static void draw(Graphics2D g2d) {

        // if FOVScale outdated, generate sprite to the right size
        if (localFOVratio != Canvas.FOVratio) {
            tileDrawSize[0] = (int) (TILE_SIZE * Canvas.FOVratio);
            tileDrawSize[1] = (int) (TILE_SIZE * Canvas.FOVratio);
        }
             
        
        int[] corner1 = getTilePos(Canvas.fov.x, Canvas.fov.y);
        int[] corner2 = getTilePos(Canvas.fov.x + Canvas.fov.width, Canvas.fov.y + Canvas.fov.height);

        for (int tileY = corner1[1]; tileY < corner2[1] + 2; tileY++) {
            for (int tileX = corner1[0]; tileX < corner2[0] + 2; tileX++) {

                if (!RoomNode.isLandOrWall(tileX, tileY)) continue;

                int[] pos = getTruePos(tileX, tileY);
                int[] cameraPos = Canvas.getDrawPosWorld(pos[0], pos[1]);
                
                AffineTransform backup = g2d.getTransform();

                g2d.translate(cameraPos[0] + tileDrawSize[0]/2, cameraPos[1] + tileDrawSize[1]/2);
                
                g2d.rotate(Math.PI / 2 * rotationMap[tileY][tileX][1]);

                Image img = null;
                Region region = RoomNode.getCurrentRoom().region;
                
                switch (rotationMap[tileY][tileX][0]) {
                    case CENTER:
                        img = region.landTiles.center;
                        break;
                    case SIDE:
                        img = region.landTiles.side;
                        break;
                    case CORNER:
                        img = region.landTiles.corner;
                        break;
                    case CORNER_INVERTED:
                        img = region.landTiles.corner_inverted;
                        break;
                }

                g2d.drawImage(img, -tileDrawSize[0]/2, -tileDrawSize[1]/2, tileDrawSize[0], tileDrawSize[1], null);

                g2d.setTransform(backup);
            }
        }
    }

    public static void calculateRotationMap() {
        rotationMap = new int[RoomNode.map.length][RoomNode.map[0].length][2];

        for (int y = 0; y < rotationMap.length; y++) {
            for (int x = 0; x < rotationMap[0].length; x++) {

                if (RoomNode.isLandOrWall(x, y)) {

                    final int adjacentWaterDir = getSideDirHere(x, y);

                    if (adjacentWaterDir == -1) {
                        rotationMap[y][x][0] = CENTER;
                        
                        int cornerWaterDir = getInvertedDirHere(x, y);
                        if (cornerWaterDir != -1) {
                            rotationMap[y][x][0] = CORNER_INVERTED;
                            rotationMap[y][x][1] = cornerWaterDir;
                        }
                        
                    }
                    else {
                        rotationMap[y][x][0] = SIDE; 
                        rotationMap[y][x][1] = adjacentWaterDir; 

                        int doubleAdjacentWaterDir = getCornerDirHere(x, y);
                        if (doubleAdjacentWaterDir != -1) {
                            rotationMap[y][x][0] = CORNER;
                            rotationMap[y][x][1] = doubleAdjacentWaterDir;
                        }
                        
                    }
                    
                }

            }
        }


    }
    private static int getInvertedDirHere(int x, int y) {
        final int[][] directions = {{1, -1}, {1, 1}, {-1, 1}, {-1, -1}};
        for (int rot = 0; rot < 4; rot++) {

            if (!RoomNode.isLandOrWall(x + directions[rot][0], y +  + directions[rot][1])) {
                return rot;
            }
        }
        return -1;
    }
    private static int getCornerDirHere(int x, int y) {
        final int[][] directions = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}, {0, 1}};

        for (int rot = 0; rot < 4; rot++) {
            if (!RoomNode.isLandOrWall(x + directions[rot][0], y +  + directions[rot][1]) && 
                !RoomNode.isLandOrWall(x + directions[rot + 1][0], y +  + directions[rot + 1][1])) {
                
                return rot;

            }
        }
        return -1;
    }

    private static int getSideDirHere(int x, int y) {
        
        final int[][] directions = {{1,0}, {0,1}, {-1,0}, {0,-1}};

        for (int i = 0; i < 4; i++) {
            if (!RoomNode.isLandOrWall(x + directions[i][0], y + directions[i][1])) 
                return i;
        }

        return -1;
    }
    
    public static int getTrueVal(int tileVal) { return tileVal * TILE_SIZE; }
    public static int getTileVal(double trueVal) { return (int) (trueVal / TILE_SIZE); }

    public static int[] getTilePos(double trueX, double trueY) {
        return new int[] {getTileVal(trueX), getTileVal(trueY)};
    }
    public static int[] getTruePos(int tileX, int tileY) {
        return new int[] {getTrueVal(tileX), getTrueVal(tileY)};
    }

    public static void print() {
        System.out.println("=type======");
        for (int i = 0; i < rotationMap.length; i++) {
            for (int j = 0; j < rotationMap[0].length; j++) {
                System.out.print(((rotationMap[i][j][0] == 0)? ' ' : (char) rotationMap[i][j][0] ) + " ");
            }
            System.out.println("|");
        }
        System.out.println("=rotation=======");
        for (int i = 0; i < rotationMap.length; i++) {
            for (int j = 0; j < rotationMap[0].length; j++) {

                if (RoomNode.isLandOrWall(j, i)) System.out.print(rotationMap[i][j][1] + " ");
                else                    System.out.print("  ");
            }
            System.out.println("|");
        }
    }
    
}
