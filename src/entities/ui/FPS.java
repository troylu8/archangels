package src.entities.ui;

import java.awt.*;
import src.draw.Canvas;

public class FPS extends TextUI {

    Text display;

    public double fps = 0;

    public FPS() {
        super("none", 1, 1, 1, -20, -20);
        setAnchor(1, 1);
        
        display = new Text("- fps", 0, 0, 20, Text.RIGHT_ALIGNMENT, Color.BLUE);
        addText(display);
    }

    private final long INTERVAL = 300;
    private long lastIntervalStart = 0;
    private long totalFramesThisInterval = 0;

    @Override
    public void update(long deltatime) {
        long timeNow = System.currentTimeMillis();

        if (timeNow >= lastIntervalStart + INTERVAL) {
            fps = totalFramesThisInterval / ((timeNow - lastIntervalStart) / 1000.0);

            totalFramesThisInterval = 0;
            lastIntervalStart = timeNow;

            display.text = String.format("%.2f", fps) + " fps";
            
            FontMetrics fm = Canvas.panel.getFontMetrics(display.font);
            spriteBounds.setSize(fm.stringWidth(display.text), fm.getHeight());
            updatePos();
        } 
        else totalFramesThisInterval++;
            
    }
    
}
