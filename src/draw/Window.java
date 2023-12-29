package src.draw;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;

import src.input.PlayerMovementControls;
import src.util.Util;

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

        addWindowStateListener((WindowEvent e) -> { 
            new Thread(() -> {
                Util.sleepTilInterrupt(40);
                Canvas.ResizeListener.doOnResize(); 
            }).start();
            
            System.out.println("a"); });
        addWindowFocusListener(new StopAfterFocusLost());

        setVisible(true);

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