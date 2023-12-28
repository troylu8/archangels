package src.entities.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

import src.world.RoomNode;
import src.draw.Canvas;

public class Map extends UI {

    public static final Color EDGE_COLOR = Color.RED;
    public static final Color CLEARED_COLOR = Color.WHITE; 
    public static final Color UNCLEARED_COLOR = Color.GRAY;
    public static final Color CURRENT_COLOR = Color.ORANGE;

    public static final int NODE_SIZE = 10;
    private static int NODE_DIST = 25;

    public static Map map = new Map("map bg.png", 7, -1);
    public static Map minimap;
    static {
        minimap = new Map("minimap bg.png", 6, 3);
        minimap.setAnchor(0.7, 0.5);
        minimap.ref = new double[] {1, 0};
        minimap.setSize(6);

        minimap.enable();

    }

    private int depth = 0;

    public Map(String bgFilepath, int size, int depth) {
        super(bgFilepath, 1, 0.5, 0.5, 0, 0);
        visible = false;
        opacity = 0;
        this.depth = depth;
        setSize(size);
    }

    Thread fadeIn  = new Thread();
    Thread fadeOut = new Thread();

    @Override
    public void enable() {
        super.enable();
        visible = true;

        fadeIn = new Thread(() -> {
            fadeOut.interrupt();

            try {
                for (; opacity < 1; opacity = Math.min(opacity + 0.1f, 1) )
                    Thread.sleep(5);
            } 
            catch (InterruptedException ie) {}
            
        }, "map fade in thread");
        fadeIn.start();
        
    }

    @Override
    public void disable() {

        fadeOut = new Thread(() -> {
            fadeIn.interrupt();

            try {
                for (; opacity > 0; opacity = Math.max(opacity - 0.1f, 0) )
                    Thread.sleep(5);
                
                visible = false;
                super.disable();
            } 
            catch (InterruptedException ie) {}
            
            
        }, "map fade out thread");
        fadeOut.start();
        
    }

    @Override
    public void setSize(double multiple) {
        super.setSize(multiple);
        drawMap();
    }

    /* clear map markings then drawMap() */
    public void clearAndDrawMap() {
        setSize(getSize()); // applies originalsprite (that doesnt have map markings on it) as the sprite 
        drawMap();
    }
    /** draws map on top of bg img */
    public void drawMap() {
        BufferedImage sprite = getSprite();

        Graphics g = sprite.getGraphics();
        
        int centerX = (int) (sprite.getWidth(null) * getAnchorX());
        int centerY = (int) (sprite.getHeight(null) * getAnchorY());

        drawMapBFS(g, centerX, centerY);

        g.dispose();
    }

    private void drawMapBFS(Graphics g, int centerX, int centerY) {
        boolean[][][] drawn = new boolean[RoomNode.WORLD_SIZE][RoomNode.WORLD_SIZE][RoomNode.WORLD_SIZE];
        drawn[RoomNode.playerQ][RoomNode.playerR][RoomNode.playerS] = true;

        int currDepth = 0;

        ArrayDeque<int[]> queue = new ArrayDeque<>(); // {q, r, s, drawX, drawY}
        queue.add(new int[] {RoomNode.playerQ, RoomNode.playerR, RoomNode.playerS, centerX, centerY});

        while (!queue.isEmpty() && currDepth != depth) {
            int sz = queue.size();

            for (int i = 0; i < sz; i++) {
                int[] pos = queue.removeFirst();
                RoomNode room = RoomNode.allRooms[pos[0]][pos[1]][pos[2]];
                int x = pos[3], y = pos[4];

                g.setColor(Color.RED);
                g.setFont(new Font(g.getFont().getName(), Font.BOLD, 20));
                g.drawString("" + room.getRadius(), x, y);
                
                if (RoomNode.playerQ == room.getQ() && RoomNode.playerR == room.getR() && RoomNode.playerS == room.getS())
                    g.setColor(CURRENT_COLOR);
                else if (room.isCleared())
                    g.setColor(CLEARED_COLOR);
                else if (room.getRadius() >= RoomNode.TOTAL_RADIUS - 1)
                    g.setColor(EDGE_COLOR);
                else
                    g.setColor(UNCLEARED_COLOR);

                final int drawnNodeSize = (int) (NODE_SIZE * Canvas.CAMratio);
                g.fillOval(x - drawnNodeSize / 2, y - drawnNodeSize / 2, drawnNodeSize, drawnNodeSize);

                for (int[] b : room.bridges) {
                    int[] transform2D = hexDirectionToVector(b, NODE_DIST * Canvas.CAMratio);

                    g.drawLine(x, y, x + transform2D[0], y + transform2D[1]);
                }

                for (int[] d : RoomNode.HEX_DIRECTIONS) {
                    
                    int[] transform2D = hexDirectionToVector(d, NODE_DIST * Canvas.CAMratio);
                    int[] newHexPos = new int[] {room.getQ() + d[0], room.getR() + d[1], room.getS() + d[2]};

                    if (RoomNode.allRooms[newHexPos[0]][newHexPos[1]][newHexPos[2]] != null && !drawn[newHexPos[0]][newHexPos[1]][newHexPos[2]]) {
                        queue.addLast(new int[] {newHexPos[0], newHexPos[1], newHexPos[2], x + transform2D[0], y + transform2D[1]});
                        drawn[newHexPos[0]][newHexPos[1]][newHexPos[2]] = true;
                    }
                }
            }

            currDepth++;
        }
    }
    public static int[] hexDirectionToVector(int[] hexDirection, double length) {
        int[] res = {
            (int) (((hexDirection[2] <= 0 && hexDirection[0] >= 0)? length : -length)),
            (int) (length * hexDirection[1])
        };

        // if moving vertically at all, horizontal distance halved
        if (res[1] != 0) res[0] /= 2;

        return res;
    }

}