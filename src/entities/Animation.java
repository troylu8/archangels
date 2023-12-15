package src.entities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import src.manage.Clock;


public class Animation {

    Entity entity;

    File gif;
    ImageInputStream inputStream;
    ImageReader imageReader;

    PlayThread playThread = new PlayThread(entity);

    public int[] frameDurations;
    private HashMap<Integer, ArrayList<Runnable>> frameHooks;
    public int totalFrames;
    int totalTime;

    public int frame;

    boolean playOnEnable;
    boolean disableAfterAnimation;

    public Animation(String filename, Entity entity) {
        this.entity = entity;
        entity.visible = false;
        frame = 0;
        playOnEnable = true;
        disableAfterAnimation = true;
        frameHooks = new HashMap<>();

        try {
            gif = new File("game files\\sprites\\" + filename);
            inputStream = ImageIO.createImageInputStream(gif);
            imageReader = ImageIO.getImageReadersByFormatName("gif").next();
            imageReader.setInput(inputStream);

            entity.setSprite(imageReader.read(0));
            
            totalFrames = imageReader.getNumImages(true);
            frameDurations = new int[totalFrames];

            // fill in frameDurations
            totalTime = 0;
            for (int f = 0; f < totalFrames; f++) {                        
                IIOMetadataNode root = (IIOMetadataNode) imageReader.getImageMetadata(f).getAsTree("javax_imageio_gif_image_1.0");
                IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
                frameDurations[f] = Integer.parseInt(graphicsControlExtensionNode.getAttributes().getNamedItem("delayTime").getNodeValue()) * 10;
                totalTime += frameDurations[f];
            }

        } 
        catch (IOException e) {}
        
    }

    /** hooks run at the end of the frame duration */
    public void addFrameHook(int frame, Runnable hook) {
        ArrayList<Runnable> hooks = frameHooks.getOrDefault(frame, new ArrayList<>());
        hooks.add(hook);
        frameHooks.put(frame, hooks);
    }
    private void runHooks(int frame) {
        if (frameHooks.get(frame) != null) {
            for (Runnable hook : frameHooks.get(frame)) 
                hook.run();
        }
    }

    /** time left in this frame */
    long unfinishedFrameTime = 0;

    class PlayThread extends Thread {
        Entity entity;
        public PlayThread(Entity entity) {
            this.entity = entity;
            setName("play thread for " + entity);
        }
        @Override
        public void run() {
            /** the time this frame WOULD end if animation wasn't interrupted */
            long frameEndTime = -1;

            try {
                
                int trueFrameTime;
                frameEndTime = System.currentTimeMillis() + unfinishedFrameTime;
                Thread.sleep( Math.max(unfinishedFrameTime, 0) );

                while (frame < totalFrames - 1) {  

                    entity.waitTilUnaffectedByClock();

                    trueFrameTime = (entity.affectedByClock())? Clock.adjustForClockSpeed(frameDurations[frame]) : frameDurations[frame];
                    frameEndTime = System.currentTimeMillis() + trueFrameTime;

                    Thread.sleep(trueFrameTime);
                    runHooks(frame); // run hooks just before starting next frame

                    frame++;

                    entity.setSprite(imageReader.read(frame));
                }
                runHooks(frame);
                trueFrameTime = (entity.affectedByClock())? Clock.adjustForClockSpeed(frameDurations[frame]) : frameDurations[frame];
                Thread.sleep(trueFrameTime);

                if (entity.endHook != null) entity.endHook.run();
                if (disableAfterAnimation)  entity.disable();
            }
            catch (InterruptedException ie) {
                if (frameEndTime == -1) {
                    System.out.println("investigate this! idk how this happened");
                    unfinishedFrameTime = 0;
                }
                else unfinishedFrameTime = frameEndTime - System.currentTimeMillis();
            }
            catch (IOException e) {}
        }
    }

    /** return play thread */
    public Thread play() {
        entity.visible = true;
        playThread = new PlayThread(entity);        
        playThread.start();
        return playThread;
    }

    public void pause() {
        playThread.interrupt();
    }

    public void restart() {
        pause();
        frame = 0;
        unfinishedFrameTime = 0;
        play();
    }

    public void dispose() {
        try {
            playThread.interrupt();
            inputStream.close();
            imageReader.dispose();
        } 
        catch (IOException e) {}
    }

    private static IIOMetadataNode getNode( IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        return null;
    }


}
