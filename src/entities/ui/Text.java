package src.entities.ui;

import java.awt.*;

import src.draw.Canvas;
import src.manage.Main;
import src.util.Util;

public class Text {

    public static final String FONT_NAME = "arial"; 
    public static final double LEFT_ALIGNMENT = 0;
    public static final double CENTER_ALIGNMENT = 0.5;
    public static final double RIGHT_ALIGNMENT = 1;

    public UI parent = null;
    
    private Font originalFont;
    public Font font;
    public Color color;

    private double localCAMratio = 1;

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
        if (localCAMratio != Canvas.CAMratio) {
            font = originalFont.deriveFont((float) (originalFont.getSize() * Canvas.CAMratio));
            localCAMratio = Canvas.CAMratio;
        }
        g.setColor(color);
        g.setFont(font);

        FontMetrics fm = g.getFontMetrics(originalFont);


        double[] origin = {0, 0};
        if (parent != null) 
            origin = new double[] {parent.x, parent.y};

        int[] drawPos = Canvas.getDrawPosUI(origin[0] + (x - alignment * fm.stringWidth(text)) * Canvas.CAMratio, 
                                            origin[1] + y * Canvas.CAMratio);

        g.drawString(text, drawPos[0], drawPos[1]);

        if (Main.drawSpriteBounds) {
            g.setColor(Color.ORANGE);
            System.out.println(Util.arrStr(origin));
            g.fillOval((int) (origin[0] + x * Canvas.CAMratio) - 5, (int) (origin[1] + y * Canvas.CAMratio) - 5, 10, 10);
        }
    }

    
}
