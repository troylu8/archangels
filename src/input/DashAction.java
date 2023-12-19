package src.input;

import src.draw.Canvas;
import src.entities.Player;
import src.entities.attack.*;
import src.entities.fx.*;
import src.manage.Clock;
import src.util.Util;

public class DashAction extends KeyPressAction {

    public static final int DISTORTION_TIME = 2500;

    static final double DASH_MULTIPLIER = 4;
    public static final int DASH_TIME = 75;
    public static boolean dashing;

    public static final int COOLDOWN = 500;
    public static long timeOfLastDash = 0;

    public static TintFX tint;

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
                        Util.sleepTilInterrupt(DASH_TIME / 2);
                        Player.player.speedMultiplier /= 5;
                        Util.sleepTilInterrupt(200);
                        Player.player.speedMultiplier *= 5;
                    }, "slow player on premonition thread").start();

                    Clock.pause(Clock.NON_PLAYER_ENTITIES);
                    Canvas.setFOVsizeByWidth(1500);

                    tint = new TintFX(DISTORTION_TIME);
                    tint.enable();

                    Util.sleepTilInterrupt(DISTORTION_TIME);
                    
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