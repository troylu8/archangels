package src.input;

import src.entities.ui.Map;

public class MapAction extends KeyPress {

    public MapAction(int keyCode) { super(keyCode); }

    @Override
    public void onKeyPress() { Map.map.enable(); }

    @Override
    public void onKeyRelease() { Map.map.disable();}
}
