package src.world;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import src.draw.Canvas;
import src.manage.Main;

public class LandTiles {

    public static final int TILE_SIZE = 64;
    public int tileDrawSize = 64;
    private double localFOVratio = 1;

    // center, side, corner, corner inverted
    static final int CENTER = '.';
    static final int SIDE = 'S';
    static final int CORNER = 'c';
    static final int CORNER_INVERTED = 'C';

    // rotationMap[y][x][1] == # of times to rotate 90 degrees cw
    static int[][][] rotationMap;

    BufferedImage center_original;
    BufferedImage side_original;
    BufferedImage corner_original;
    BufferedImage corner_inverted_original;

    BufferedImage center;
    BufferedImage side;
    BufferedImage corner;
    BufferedImage corner_inverted;

    public LandTiles(String landTilesFolder) {
        landTilesFolder = "game files\\sprites\\tiles\\" + landTilesFolder;

        try {
            center_original = ImageIO.read(new File(landTilesFolder + "\\center.png"));
            side_original = ImageIO.read(new File(landTilesFolder + "\\side.png"));
            corner_original = ImageIO.read(new File(landTilesFolder + "\\corner.png"));
            corner_inverted_original = ImageIO.read(new File(landTilesFolder + "\\corner inverted.png"));
        
            center = center_original;
            side = side_original;
            corner = corner_original;
            corner_inverted = corner_inverted_original;
        } 
        catch (IOException e) {e.printStackTrace();}
        
        updateTileSizes(64);
    }

    private void updateTileSizes(int tileDrawSize) {
        this.tileDrawSize = tileDrawSize; 
        center = toBufferedImage(center_original.getScaledInstance(tileDrawSize, tileDrawSize, Image.SCALE_FAST));
        side = toBufferedImage(side_original.getScaledInstance(tileDrawSize, tileDrawSize, Image.SCALE_FAST));
        corner = toBufferedImage(corner_original.getScaledInstance(tileDrawSize, tileDrawSize, Image.SCALE_FAST));
        corner_inverted = toBufferedImage(corner_inverted_original.getScaledInstance(tileDrawSize, tileDrawSize, Image.SCALE_FAST));
    }

    private BufferedImage toBufferedImage(Image img) {
        BufferedImage res = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = res.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return res;
    }
    
    static int c = 0;
    public void draw(Graphics2D g2d) {

        // if FOVScale outdated, generate sprite to the right size
        
        if (localFOVratio != Canvas.FOVratio) {
            updateTileSizes( (int) (TILE_SIZE * Canvas.FOVratio));
            localFOVratio = Canvas.FOVratio;
        }
        
        int[] corner1 = getTilePos(Canvas.fov.getX(), Canvas.fov.getY());
        int[] corner2 = getTilePos(Canvas.fov.getX() + Canvas.fov.getWidth(), Canvas.fov.getY() + Canvas.fov.getHeight());
        
        for (int tileY = corner1[1]; tileY < corner2[1] + 1; tileY++) {
            
            for (int tileX = corner1[0]; tileX < corner2[0] + 1; tileX++) {
                
                if (!RoomNode.isLandOrWall(tileX, tileY)) continue;

                int[] pos = getTruePos(tileX, tileY);
                int[] cameraPos = Canvas.getDrawPosWorld(pos[0], pos[1]);
                
                AffineTransform backup = g2d.getTransform();

                g2d.translate(cameraPos[0] + tileDrawSize/2, cameraPos[1] + tileDrawSize/2);
                
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

                g2d.drawImage(img, -tileDrawSize/2, -tileDrawSize/2, null);
                // g2d.drawImage(img, -tileDrawSize/2, -tileDrawSize/2, tileDrawSize, tileDrawSize, null);
                
                g2d.setTransform(backup);
                
                
            }
        }
        

        if (Main.drawGrid) {

            for (int y = corner1[1]; y < corner2[1] + 1; y++) {
                g2d.setColor( (y % RoomNode.OBSTACLE_GRID_SIZE == 0)? Color.RED : Color.GRAY );
                int drawPos = Canvas.getDrawPosWorld(0, LandTiles.getTrueVal(y))[1];
                g2d.drawLine( 0, drawPos, Canvas.panel.getWidth(), drawPos);
            }

            for (int x = corner1[0]; x < corner2[0] + 1; x++) {
                g2d.setColor( (x % RoomNode.OBSTACLE_GRID_SIZE == 0)? Color.RED : Color.GRAY );
                int drawPos = Canvas.getDrawPosWorld(LandTiles.getTrueVal(x), 0)[0];
                g2d.drawLine( drawPos, 0, drawPos, Canvas.panel.getHeight());
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
