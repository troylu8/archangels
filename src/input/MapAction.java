package src.input;

import src.entities.ui.Map;

public class MapAction extends KeyPressAction {
    @Override
    public void onKeyPress() { Map.map.enable(); }

    @Override
    public void onKeyRelease() { Map.map.disable();}
}
