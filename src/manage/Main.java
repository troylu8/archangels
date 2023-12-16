package src.manage;

import src.world.RoomNode;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.KeyStroke;

import src.draw.Canvas;
import src.draw.Window;
import src.entities.*;
import src.entities.ui.FPS;
import src.input.PlayerControls.DebugAction;
import src.util.Util;

public class Main { // out of bounds seed 1702397158781

    public static boolean drawSpriteBounds = false;
    public static boolean drawHitboxes = false;

    public static void main(String[] args) {

        Canvas.panel = new Canvas();
        Window.window = new Window(Canvas.panel);
        
        Player.player = new Player(1500, 1500);        
        Player.player.enable();
        System.out.println("f " + Entity.entityLayers);

        RoomNode.generateWorld(System.currentTimeMillis());
        
        Clock.start(); 
        
        Gate.open();

        new DebugAction(KeyEvent.VK_1, () -> {
            if (Clock.isPaused())   Clock.unpause();
            else                    Clock.pause(100);
        });
        new DebugAction(KeyEvent.VK_2, () -> { Main.drawSpriteBounds = !Main.drawSpriteBounds; });
        new DebugAction(KeyEvent.VK_3, () -> { Main.drawHitboxes = !Main.drawHitboxes; });


        new DebugAction(KeyEvent.VK_4, () -> { Canvas.setFOVsizeByWidth(Canvas.fov.width - 200); });
        new DebugAction(KeyEvent.VK_5, () -> { Canvas.setFOVsizeByWidth(Canvas.fov.width + 200); });
    
        new DebugAction(KeyEvent.VK_6, () -> { System.out.println(Player.player.hornsAndTail.getDrawLayer()); });
    }

}
