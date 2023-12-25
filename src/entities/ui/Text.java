package src.entities.ui;

import java.awt.*;

import src.draw.Canvas;
import src.manage.Main;

public class Text {

    public static final String FONT_NAME = "arial"; 
    public static final double LEFT_ALIGNMENT = 0;
    public static final double CENTER_ALIGNMENT = 0.5;
    public static final double RIGHT_ALIGNMENT = 1;

    public UI parent = null;
    
    private Font originalFont;
    public Font font;
    private double localFOVratio = 1;
    public Color color;

    public String text;

    public double x;
    public double y;
    public double alignment;
    
    public Text(String text, double x, double y, int size, double alignment, Color color) {
        this.text = text;

        originalFont = new Font(FONT_NAME, Font.PLAIN, size);
        font = originalFont.deriveFont((float) size); // deep copy of originalFont
        this.color = color;

        this.x = x;
        this.y = y;
        
        this.alignment = alignment;
        
    }

    public void draw(Graphics2D g) {
        // if (localFOVratio != Canvas.FOVratio) {
        //     font = originalFont.deriveFont((float) (originalFont.getSize() * Canvas.FOVratio));
        //     localFOVratio = Canvas.FOVratio;
        // }
        g.setColor(color);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics(originalFont);

        double[] origin = {0, 0};
        if (parent != null) 
            origin = new double[] {parent.x, parent.y};

        int[] drawPos = Canvas.getDrawPosUI(origin[0] + (x - alignment * fm.stringWidth(text)) * Canvas.FOVratio, 
                                            origin[1] + y * Canvas.FOVratio);

        g.drawString(text, drawPos[0], drawPos[1]);

        if (Main.drawSpriteBounds) {
            g.setColor(Color.ORANGE);
            g.fillOval((int) (origin[0] + x * Canvas.FOVratio) - 5, (int) (origin[1] + y * Canvas.FOVratio) - 5, 10, 10);
        }
    }

    
}
