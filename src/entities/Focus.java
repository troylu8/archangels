package src.entities;

import src.entities.attack.PlayerSlash;
import src.manage.Clock;

public class Focus extends Entity {

    /** toggles refocusing to closest enemy every frame */
    public static boolean updateFocus = true;

    final Enemy wearer;

    final Object focusLock = new Object(); 
    
    boolean startDisabling;

    public Focus(Enemy enemy) {
        super("focus.gif", enemy.x, enemy.spriteBounds.getY() + enemy.spriteBounds.getHeight(), 0.5, 0.5, 5);
        
        this.wearer = enemy;

        startDisabling = false;

        for (int frame = 0; frame < 5; frame++) {
            animation.addFrameHook(frame, () -> {
                if (startDisabling) 
                    animation.frame = 10 - animation.frame - 1;  // -1 since frame++ will run after hook
            });
        }

        animation.addFrameHook(5, () -> {
            
            synchronized (focusLock) {
                
                while (!startDisabling) {
                    try { focusLock.wait(); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        });
        
        endHook = () -> { super.disable(); };

        clockAffectedLevel = Clock.NON_PLAYER_ENTITIES;
    }

    @Override
    public void update(long deltaTime) {
        setPosition(wearer.x, wearer.spriteBounds.getY() + wearer.spriteBounds.getHeight());
    }

    @Override
    public void disable() { 
        startDisabling = true; 
        synchronized (focusLock) { focusLock.notifyAll(); }
    }

    public static Enemy focusedEnemy;
    static Focus activeFocus;

    public static void updateFocusedEnemy() {
        if (!updateFocus) return;

        Enemy prev = focusedEnemy;

        focusedEnemy = Enemy.closestEnemyInRange(PlayerSlash.RANGE);

        if (prev != null && focusedEnemy != null && focusedEnemy.equals(prev)) return;

        setFocus(focusedEnemy);
    }

    public static void setFocus(Enemy enemy) {
        if (activeFocus != null)
            activeFocus.disable();
        
        if (enemy == null) activeFocus = null;
        else {
            activeFocus = new Focus(enemy);
            activeFocus.enable();
        }
    }
    
}
