package src.manage;

import src.world.RoomNode;

import java.awt.event.KeyEvent;

import src.draw.*;
import src.entities.*;
import src.entities.attack.PlayerSlash;
import src.entities.fx.TintFX;
import src.input.CounterAction;
import src.input.DebugAction;
import src.input.KeyBindManager;
import src.input.PlayerControls;
import src.input.PlayerMovementControls;

public class Main { // out of bounds seed 1702397158781

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

        new DebugAction(KeyEvent.VK_1, () -> {
            if (Clock.isPaused())   Clock.unpause();
            else                    Clock.pause(100);
        });
        new DebugAction(KeyEvent.VK_2, () -> { Main.drawSpriteBounds = !Main.drawSpriteBounds; });
        new DebugAction(KeyEvent.VK_3, () -> { Main.drawHitboxes = !Main.drawHitboxes; });


        new DebugAction(KeyEvent.VK_4, () -> { Canvas.setFOVsizeByWidth(Canvas.fov.width - 200); });
        new DebugAction(KeyEvent.VK_5, () -> { Canvas.setFOVsizeByWidth(Canvas.fov.width + 200); });

        new DebugAction(KeyEvent.VK_6, () -> {  });
        
        SwordAngel sa = new SwordAngel(Player.player.x, Player.player.y);
        sa.enable();
        CounterAction.enable(sa);
        Player.player.visible = true;
    }

}
