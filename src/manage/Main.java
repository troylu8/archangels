package src.manage;

import src.world.RoomNode;

import java.awt.event.KeyEvent;

import src.draw.*;
import src.entities.*;
import src.entities.attack.PlayerSlash;
import src.entities.fx.TintFX;
import src.entities.ui.FPS;
import src.input.*;

public class Main { // TODO: out of bounds seed 1702397158781

    public static boolean drawGrid = false;
    public static boolean drawSpriteBounds = false;
    public static boolean drawHitboxes = false;

    public static void main(String[] args) {

        Canvas.panel = new Canvas();
        Window.window = new Window(Canvas.panel);
        
        Player.player = new Player(1500, 1500);        
        Player.player.enable();

        PlayerControls.init();

        RoomNode.generateWorld(System.currentTimeMillis());
        
        Clock.start(); 
        
        Gate.open();

        new DebugAction(KeyEvent.VK_1, () -> { Main.drawGrid = !Main.drawGrid; });
        new DebugAction(KeyEvent.VK_2, () -> { Main.drawSpriteBounds = !Main.drawSpriteBounds; });
        new DebugAction(KeyEvent.VK_3, () -> { Main.drawHitboxes = !Main.drawHitboxes; });

        new DebugAction(KeyEvent.VK_4, () -> { Canvas.setFOVsizeByWidth(Canvas.fov.getWidth() - 200); });
        new DebugAction(KeyEvent.VK_5, () -> { Canvas.setFOVsizeByWidth(Canvas.fov.getWidth() + 200); });
        
        new DebugAction(KeyEvent.VK_6, () -> { Canvas.ResizeListener.doOnResize(); });

        new DebugAction(KeyEvent.VK_G, () -> { 
            new Entity("tiles\\cloud tiles\\corner.png", Player.player.x, Player.player.y).enable();
        });
        
        Player.player.visible = true;

        new FPS().enable();
    }

}
