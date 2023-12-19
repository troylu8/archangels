package src.input;

import src.draw.Canvas;
import src.entities.attack.PlayerStarStep;
import src.manage.Clock;

public class StarStepAction extends KeyPressAction {

    @Override
    public void onKeyPress() {
        // if clock paused, star step is free but unpauses clock
        if (Clock.isPaused()) {
            new PlayerStarStep().enable();
            
            Clock.unpause();
            if (DashAction.tint != null) DashAction.tint.disable();

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