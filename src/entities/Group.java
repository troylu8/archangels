package src.entities;

import java.util.*;
import java.util.function.Consumer;

public class Group<Type> implements Iterable<Type> {

    private static ArrayList<Group> allGroups = new ArrayList<>();
    
    public HashSet<Type> set;
    
    /** next frame, add/remove these items */ 
    public HashSet<Type> factory;
    public HashSet<Type> trash;

    public Group() {
        set = new HashSet<>();
        factory = new HashSet<>();
        trash = new HashSet<>();
        
        synchronized (allGroups) { allGroups.add(this); }
    }

    public void queueToAdd(Type item) {
        synchronized (factory) {
            factory.add(item);
        }
    }
    public void queueToRemove(Type item) {
        synchronized (trash) {
            trash.add(item);
        }
    }

    /** empties factory and trash */
    private void update() {
        synchronized (this) {
            synchronized (factory) {
                set.addAll(factory);
                factory.clear();
            }

            synchronized (trash) {
                set.removeAll(trash);
                trash.clear();
            }
        }
        
        
    }

    public static void updateAll() {
        synchronized (allGroups) {
            allGroups.forEach((Group g) -> { g.update(); });
        }
    }

    public void forEachSynced(Consumer<? super Type> func) {
        synchronized (this) { set.forEach(func); }
    }

    @Override
    public Iterator<Type> iterator() {
        return set.iterator();
    }

    @Override
    public String toString() {
        return "-----\n" + set.toString() + "\nsize " + set.size() + "\nfactory " + factory.size() + "\ntrash " + trash.size() + "\n-----";
    }
    
}
