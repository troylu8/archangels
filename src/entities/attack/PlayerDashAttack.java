package src.entities.attack;

import src.entities.Enemy;
import src.entities.Player;
import src.entities.accessory.SlashFX;
import src.input.PlayerMovementControls;
import src.shapes.Circle;
import src.shapes.Collidable;
import src.util.Util;

public class PlayerDashAttack extends PlayerAttack {

    public PlayerDashAttack() {
        super("none", Player.player.x, Player.player.y, 10);
        hitboxes.add(new Circle(x, y, 50));
    }

    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        setPosition(Player.player.x, Player.player.y);
    }

    @Override
    public void updateHitboxes() {
        Circle c = (Circle) hitboxes.get(0);
        c.x = this.x;
        c.y = this.y;
    }

    @Override
    public void onCollideEnter(Collidable other) {
        
        Enemy enemy = (Enemy) other;

        double dashDir = Util.directionToTheta(Player.player.trajectory[0], Player.player.trajectory[1]) + Math.PI / 2;
        
        new SlashFX(enemy, 5, dashDir + Util.rand(-Math.PI/6, Math.PI/6) ).enable();

        // push in player heading direction if non-moving dash 
        // kb is done here instead of using getKnockback() in victim.onCollideEnter() as its in a special direction (not just away) 
        if (PlayerMovementControls.offset[0] == 0 && PlayerMovementControls.offset[1] == 0)
            enemy.push(enemy.x + Player.player.getHeading(), enemy.y, 4);
        else
            enemy.push(enemy.x + PlayerMovementControls.offset[0], enemy.y + PlayerMovementControls.offset[1], 4);
    }

    @Override
    public double getKnockback() { return 0; }

}