package src.input;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import src.draw.Canvas;
import src.entities.*;
import src.entities.attack.*;
import src.entities.fx.AfterimageFX;
import src.entities.fx.TintFX;
import src.manage.Clock;
import src.manage.Main;
import src.shapes.HitboxList;
import src.util.Util;
import src.entities.ui.Map;


public class PlayerControls {
    
    /** always active regardless of current activeControls*/
    public static HashMap<KeyStroke, String> constantControls = new HashMap<>();

    public static HashMap<KeyStroke, String> defaultControls = new HashMap<>();

    public static HashMap<KeyStroke, String> activeControls;

    Player player;

    public static PlayerMovementControls playerMovementControls;

    public PlayerControls(Player player){

        this.player = player;

        playerMovementControls = new PlayerMovementControls(player);

        new DashAction(KeyEvent.VK_K).addToControls(defaultControls);
        new SlashAction(KeyEvent.VK_J).addToControls(defaultControls);
        new StarStepAction(KeyEvent.VK_I).addToControls(defaultControls);

        new InteractAction(KeyEvent.VK_E).addToControls(defaultControls);

        new MapAction(KeyEvent.VK_TAB).addToControls(defaultControls);
        new MapAction(KeyEvent.VK_T).addToControls(defaultControls);


        setActiveControls(defaultControls);
    }

    /** disables all but constantControls */
    public static void disableControls() { setActiveControls(new HashMap<>()); }

    public static void addKeybind(HashMap<KeyStroke, String> controlsList, int keyCode, Action action, boolean onKeyPress){
        KeyStroke keystroke = KeyStroke.getKeyStroke(keyCode, 0, !onKeyPress);
        String actionMapKey = String.valueOf(keyCode);
        if (!onKeyPress) actionMapKey += " released";

        ActionMap actionMap = Canvas.panel.getActionMap();

        controlsList.put(keystroke, actionMapKey);
        actionMap.put(actionMapKey, action);
    }

    /** adjust inputmap */
    public static void setActiveControls(HashMap<KeyStroke, String> controlsList){
        
        // if switching away from default controls, stop the player's movement
        if (activeControls != null && defaultControls != null && activeControls.equals(defaultControls))
            playerMovementControls.stopMoving();
        
        InputMap inputMap = Canvas.panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.clear();

        for (KeyStroke key : controlsList.keySet())
            inputMap.put(key, controlsList.get(key));
        
        for (KeyStroke key : constantControls.keySet())
            inputMap.put(key, constantControls.get(key));

        activeControls = controlsList;
    }

     /** bundles key down and key up action together, 
     * ignores multiple keydown inputs when holding down key */
    static abstract class KeyPress {

        private int keyCode;

        private boolean keyPressed;

        private KeyPress(int keyCode) {
            this.keyCode = keyCode;
            keyPressed = false;
        }

        public void addToControls(HashMap<KeyStroke, String> controls) {
            
            ActionMap actionMap = Canvas.panel.getActionMap();

            controls.put(KeyStroke.getKeyStroke(keyCode, 0, false), "" + keyCode);
            actionMap.put("" + keyCode, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (keyPressed) return;
                    keyPressed = true;

                    onKeyPress();
                }
            } );

            controls.put(KeyStroke.getKeyStroke(keyCode, 0, true), keyCode + " released");
            actionMap.put(keyCode + " released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!keyPressed) return;
                    keyPressed = false;

                    onKeyRelease();
                }
            } );
        }

        public abstract void onKeyPress();
        public abstract void onKeyRelease();
    }

    public static class DebugAction extends KeyPress {

        Runnable onKeyPress;
        Runnable onKeyRelease;

        public DebugAction(int keyCode, Runnable onKeyPress, Runnable onKeyRelease) { 
            super(keyCode); 
            this.onKeyPress = onKeyPress;
            this.onKeyRelease = onKeyRelease;

            this.addToControls(constantControls);

            InputMap inputMap = Canvas.panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            
            for (KeyStroke key : constantControls.keySet())
                inputMap.put(key, constantControls.get(key));

        }
        public DebugAction(int keyCode, Runnable onKeyPress) { 
            this(keyCode, onKeyPress, null);
        } 

        @Override
        public void onKeyPress() { onKeyPress.run(); }
        
        @Override
        public void onKeyRelease() { if (onKeyRelease != null) onKeyRelease.run(); }

    }


    public static class InteractAction extends KeyPress {
    
        private InteractAction(int keyCode) {
            super(keyCode);
        }

        @Override
        public void onKeyPress() {
            if (Player.focusedInteractable != null)
                Player.focusedInteractable.onInteract();
        }

        @Override
        public void onKeyRelease() {}
    }
    
    public static class DashAction extends KeyPress {
    
        static final double DASH_MULTIPLIER = 4;
        public static final int DASH_TIME = 75;
        public static boolean dashing;

        public static final int COOLDOWN = 500;
        public static long timeOfLastDash = 0;

    
        private DashAction(int keyCode) { super(keyCode); }

        @Override
        public void onKeyPress() {
            if (System.currentTimeMillis() - timeOfLastDash < COOLDOWN) return;
            timeOfLastDash = System.currentTimeMillis();

            new Thread( () -> {

                dashing = true;

                PlayerDashAttack dashAttack = new PlayerDashAttack();
                dashAttack.enable();

                if (EnemyAttack.isPlayerInPremonition() != null) {
                    new Thread( () -> {

                        new Thread( () -> {
                            Util.sleepTilInterrupt(PlayerControls.DashAction.DASH_TIME / 2);
                            Player.player.speedMultiplier /= 5;
                            Util.sleepTilInterrupt(200);
                            Player.player.speedMultiplier *= 5;
                        }, "slow player on premonition thread").start();

                        Clock.pause(Clock.NON_PLAYER_ENTITIES);
                        Canvas.setFOVsizeByWidth(1500);
                        new TintFX().enable();
                        Util.sleepTilInterrupt(1000);
                        Canvas.setFOVsizeByWidth(1200);
                        Clock.unpause();

                    }, "pause time on premonition thread").start();
                }
                    
                Player.player.slowDownThread.interrupt();
                

                if (Player.player.trajectory[0] == 0 && Player.player.trajectory[1] == 0) {
                    Player.player.lockHeading();
                    Player.player.push(Player.player.x + Player.player.getHeading(), Player.player.y, 5);
                }

                Player.player.speedMultiplier *= DASH_MULTIPLIER;
                Player.player.makeInvincible(DASH_TIME + 100);

                // afterimage time lasts a little after dash ends
                AfterimageFX.setActiveFor(DASH_TIME + 50);
                Util.sleepTilInterrupt(DASH_TIME);

                Player.player.speedMultiplier /= DASH_MULTIPLIER;

                Player.player.unlockHeading();

                dashAttack.disable();

                dashing = false;

            }, "player dashing thread").start();
                
        }

        @Override
        public void onKeyRelease() {}
    
    }
    
    public static class SlashAction extends KeyPress {
    
        private static long timeOfLastSlash;
        final int COOLDOWN = 400;
    
        private SlashAction(int keyCode) { super(keyCode); }

        @Override
        public void onKeyPress() {
            long timeNow = System.currentTimeMillis();
            if (timeNow - timeOfLastSlash < COOLDOWN) return;
            
            timeOfLastSlash = timeNow;

            HitboxList prem = EnemyAttack.isPlayerInPremonition();
            if (prem != null) {
                new Thread(() -> {
                    Player.player.parryBodyActive = true;
                    Util.sleepTilInterrupt(500);
                    Player.player.parryBodyActive = false;
                }, "enable parryBody for a short time thread").start();
   
            }

            if (Focus.focusedEnemy != null) {
                new PlayerSlash(Focus.focusedEnemy.x, Focus.focusedEnemy.y, true).enable();
            } 
            else if (prem != null) {
                EnemyAttack atk = (EnemyAttack) prem.wearer;
                new PlayerSlash(atk.x, atk.y, false).enable();
            }
            else new PlayerSlash().enable();
        }

        @Override
        public void onKeyRelease() {}
    
    }
    
    public static class StarStepAction extends KeyPress {
    
        private StarStepAction(int keyCode) { super(keyCode); }

        @Override
        public void onKeyPress() {
            // if clock paused, star step is free but unpauses clock
            if (Clock.isPaused()) {
                new PlayerStarStep().enable();
                Clock.unpause();
                Canvas.setFOVsizeByWidth(1200);
            }
            else {
                if (PlayerStarStep.getCharge() >= 3) {
                    new PlayerStarStep().enable();
                    PlayerStarStep.setCharge(0);
                }
            }
        }

        @Override
        public void onKeyRelease() {}
    
    }

    public static class MapAction extends KeyPress {

        private MapAction(int keyCode) { super(keyCode); }

        @Override
        public void onKeyPress() { Map.map.enable(); }

        @Override
        public void onKeyRelease() { Map.map.disable();}
    }

}
