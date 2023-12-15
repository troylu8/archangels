package src.entities.ui;

import java.awt.Graphics2D;

import src.entities.Group;

public class TextUI extends UI {
    public Group<Text> texts = new Group<>(); 

    public TextUI(String spriteFilename, double size, double refX, double refY, double offsetX, double offsetY, Text... text) {
        super(spriteFilename, 1, refX, refY, offsetX, offsetY);
        
        for (Text t : text) 
            addText(t);
        
    }

    public void addText(Text t) {
        texts.queueToAdd(t);
        t.parent = this;
    }
    public void removeText(Text t) { texts.queueToRemove(t); }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        texts.forEachSynced((Text t) -> { t.draw(g); });
    }
}
