package src.draw;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;

import src.input.PlayerMovementControls;

public class Window extends JFrame {

    public static Window window;

    public Window(Canvas canvas) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("title");

        getContentPane().setLayout(new BorderLayout());

        canvas.setPreferredSize(new Dimension(800, 450));
        add(canvas);
        pack();
        
        setLocationRelativeTo(null);

        addWindowStateListener(new CenterWhenWindowed());
        addWindowFocusListener(new StopAfterFocusLost());

        setVisible(true);

    }

    
}

class CenterWhenWindowed implements WindowStateListener {
    @Override
    public void windowStateChanged(WindowEvent e) {        
        if (e.getNewState() == 0)
            ((Window) e.getSource()).setLocationRelativeTo(null);       
        
        
        Canvas.ResizeListener.doOnResize();
        
    }
}

class StopAfterFocusLost implements WindowFocusListener {

    @Override
    public void windowGainedFocus(WindowEvent e) {}

    @Override
    public void windowLostFocus(WindowEvent e) {
        PlayerMovementControls.stopMoving();
    }
}