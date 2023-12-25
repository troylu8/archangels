package src.draw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

import src.entities.*;
import src.entities.attack.*;
import src.entities.ui.UI;
import src.manage.Main;
import src.shapes.*;
import src.world.*;
import src.util.Util;


public class Canvas extends JPanel {

    public static Canvas panel;
    
    /** how far the player can see */
    public static Rectangle fov = new Rectangle(0, 0, 1200, 675);

    /** actual view size on the panel, excluding black bars */
    public static Rectangle camera = new Rectangle(0, 0, 800, 450);

    /** black bars when screen size isnt proportional to fov */
    public static Rectangle bar1 = new Rectangle();
    public static Rectangle bar2 = new Rectangle();

    /** camera.width / fov.width  
     * - actual view size (aka camera) will always stay the same aspect ratio as fov, black bars fill excess window area */
    public static double FOVratio = 1;

    public Canvas() {
        addComponentListener(new ResizeListener());
        this.setFocusTraversalKeysEnabled(false);
    }
    
    @Override
    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        // draw bg
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // draw tiles
        if (RoomNode.getCurrentRoom() != null) 
            RoomNode.getCurrentRoom().region.landTiles.draw((Graphics2D) g);
        
        Integer[] layers = new Integer[Entity.entityLayers.keySet().size()];
        Entity.entityLayers.keySet().toArray(layers);
        Arrays.sort(layers);

        // draw each layer
        for (int l : layers) {

            Entity.entityLayers.get(l).forEachSynced( (Entity e) -> {
                if (e.onCamera()) {
                    e.draw(g2d);
                    
                    if (Main.drawHitboxes && e instanceof Collidable) {
                        g2d.setColor(Color.BLUE);
                        ((Collidable) e).getHitboxes().draw(g2d);
                    }

                }
            });

        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        // draw bars
        g2d.setColor(Color.BLACK);
        g2d.fillRect(bar1.x, bar1.y, bar1.width, bar1.height);
        g2d.fillRect(bar2.x, bar2.y, bar2.width, bar2.height);
        
        // draw premonitions
        g2d.setColor(Color.MAGENTA);
        EnemyAttack.allPremonitions.forEachSynced((HitboxList p) -> { p.draw(g2d); });

        // draw ui
        UI.allUI.forEachSynced((UI ui) -> { ui.draw(g2d); });

    }

    public static double[] targetFOVcenter = {0,0};
    private static double[] screenshakeForces = {0,0};
    public static int[] targetFOVsize = {fov.width, fov.height}; 

    public static void setFOVsizeByWidth(int width) {
        targetFOVsize[0] = width;
        targetFOVsize[1] = (int) (width * ((double)fov.height / fov.width));
        
    }

    public static void updateFOV() {

        double[] deltaSize = {(targetFOVsize[0] - fov.width) * 0.2, (targetFOVsize[1] - fov.height) * 0.2};
        fov.width += deltaSize[0];
        fov.height += deltaSize[1];
        fov.x -= deltaSize[0]/2;
        fov.y -= deltaSize[1]/2;

        int targetX = (int) targetFOVcenter[0] - fov.width / 2;
        int targetY = (int) targetFOVcenter[1] - fov.height / 2;

        fov.x += (targetX - fov.x) * 0.2 + screenshakeForces[0];
        fov.y += (targetY - fov.y) * 0.2 + screenshakeForces[1];

        if (deltaSize[0] != 0 || deltaSize[1] != 0)
            ResizeListener.doOnResize();
    }

    private static Thread screenshakeThread = new Thread();

    public static void screenshake(int duration, double strength, int delay) {
        screenshakeThread.interrupt();

        screenshakeThread = new Thread(() -> {
            for (int i = 0; i < duration; i += 50) {
                
                double[] dir = Util.thetaToUnitVector(Math.random() * 2 * Math.PI);

                screenshakeForces[0] = dir[0] * strength;
                screenshakeForces[1] = dir[1] * strength;

                Util.sleepTilInterrupt(delay);
            }
            screenshakeForces[0] = 0;
            screenshakeForces[1] = 0;
        }, "screenshake thread");
        
        screenshakeThread.start();
    }

    private static void updateCameraAndBars() {
        
        int panelWidth = Canvas.panel.getWidth();
        int panelHeight = Canvas.panel.getHeight();

        // top-bottom black bars
        if ((double) panelWidth / fov.width < (double) panelHeight / fov.height) {
            camera.width = panelWidth;
            camera.height = (int) (double) panelWidth * fov.height / fov.width;

            camera.y = (panelHeight / 2) - (camera.height / 2);
            camera.x = 0;

            bar1.setBounds(0, 0, panelWidth, camera.y);
            bar2.setBounds(0, (panelHeight / 2) + (camera.height / 2), panelWidth, camera.y + 1);
        } 
        // left-right black bars
        else {
            camera.height = panelHeight;
            camera.width = (int) (double) panelHeight * fov.width / fov.height;

            camera.x = (panelWidth / 2) - (camera.width / 2);
            camera.y = 0;

            bar1.setBounds(0, 0, camera.x, panelHeight);
            bar2.setBounds( (panelWidth / 2) + (camera.width / 2), 0, camera.x + 1, panelHeight);
        }

    }

    /** given an absolute point, find where on the panel it would be (taking into account pos relative to fov, FOVratio, and black bars) */
    public static int[] getDrawPosWorld(double x, double y) {
        return new int[] { 
            (int) ((x - fov.x) * FOVratio + camera.x),
            (int) ((y - fov.y) * FOVratio + camera.y)
        };
    }

    public static int[] getDrawPosUI(double x, double y) {
        return new int[] { 
            (int) (x + Canvas.camera.x),
            (int) (y + Canvas.camera.y)
        };
    }

    static class ResizeListener extends ComponentAdapter {

        public static void doOnResize() {
           
            updateCameraAndBars();
           
            FOVratio = (double) camera.width / fov.width;

            UI.allUI.forEachSynced((UI ui) -> { ui.updatePos(); });
        }
        
        @Override
        public void componentResized(ComponentEvent e) { doOnResize(); }
    }
}