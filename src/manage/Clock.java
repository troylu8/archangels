package src.manage;

import java.util.ArrayList;
import java.util.Map.Entry;

import src.draw.Canvas;
import src.entities.*;
import src.input.Interactable;
import src.input.PlayerControls;
import src.util.Util;

public class Clock {

    public static final int INCLUDING_UI = 3;
    public static final int INCLUDING_PLAYER = 2;
    public static final int NON_PLAYER_ENTITIES = 1;
    public static final int NONE = 0;
    
    public static int pauseState;

    public static final Object timePausedLock = new Object();
    public static final Object timeUnpausedLock = new Object();

    private static ArrayList<Thread> interruptUponPause = new ArrayList<>();

    private static double speed = 1;
    private static boolean paused = false;

    public static void setSpeed(double speed) { Clock.speed = Math.max(Double.MIN_VALUE, speed); }
    public static double getSpeed() { return speed; }


    public static void pause(int pauseState) { 
        if (Clock.pauseState == pauseState) return;
        if (pauseState == NONE) {
            unpause();
            return;
        }
        Clock.pauseState = pauseState;
        paused = true;

        if (pauseState >= Clock.INCLUDING_PLAYER) {
            Player.player.lockHeading();
        }
            

        synchronized (timePausedLock) { timePausedLock.notifyAll(); }

        synchronized (Clock.class) {
            for (Thread t : interruptUponPause) 
                t.interrupt();
        }
    }
    public static void unpause() { 
        if (Clock.pauseState == NONE) return;
        Clock.pauseState = NONE;
        paused = false;

        Player.player.unlockHeading();

        // DO ALL FORCES AFTER UNPAUSE
        // for (Entry<Enemy, ArrayList<double[]>> push : Enemy.queuedPush.entrySet()) {
        //     Enemy e = push.getKey();
        //     for (double[] pushDetails : push.getValue())
        //         e.push(pushDetails[0], pushDetails[1], pushDetails[2]);
        // }
        Enemy.queuedPush.clear();

        synchronized (timeUnpausedLock) { timeUnpausedLock.notifyAll(); }

    }
    public static int getPauseState() { return pauseState; }
    public static boolean isPaused() { return paused; }

    public static void waitTilUnaffected(int clockAffectedLevel) {
        synchronized (timeUnpausedLock) { 
            while (pauseState >= clockAffectedLevel) { 
                try { timeUnpausedLock.wait(); } 
                catch (InterruptedException e) {} 
            } 
        }
    }

    public static synchronized void interruptUponPause(Thread t) { interruptUponPause.add(t); }

    /** 10 seconds is actually 5 seconds when clock speed is 2x */
    public static double adjustForClockSpeed(double duration) { return         duration / speed; }
    /** 10 seconds is actually 5 seconds when clock speed is 2x */
    public static long   adjustForClockSpeed(long   duration) { return (long) (duration / speed); }
    /** 10 seconds is actually 5 seconds when clock speed is 2x */
    public static int    adjustForClockSpeed(int    duration) { return (int)  (duration / speed); }
    
    public static void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                long lastTime = System.currentTimeMillis();

                while (true) {
                    long timeNow = System.currentTimeMillis();
                    long deltaTime = timeNow - lastTime;
                    lastTime = timeNow;

                    Entity.allEntities.forEachSynced((Entity e) -> {
                        e.update(deltaTime);
                    });
                    
                    Canvas.updateFOV();

                    Interactable.updateFocusedInteractable();
                    Focus.updateFocusedEnemy();
                    
                    Group.updateAll();
                    Entity.clearEmptyEntityLayers();

                    Canvas.panel.repaint();

                    Util.sleepTilInterrupt(5);
                }
            }
        }, "clock").start();
    }
}
