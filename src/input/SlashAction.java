package src.input;

import src.entities.*;
import src.entities.attack.*;
import src.shapes.HitboxList;
import src.util.Util;

public class SlashAction extends KeyPress {
    
    private static long timeOfLastSlash;
    final int COOLDOWN = 400;

    public SlashAction(int keyCode) { super(keyCode); }

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