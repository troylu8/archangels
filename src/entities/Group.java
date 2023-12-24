package src.entities;

import java.util.*;
import java.util.function.Consumer;

public class Group<Type> implements Iterable<Type> {

    //TODO: private
    public static ArrayList<Group> allGroups = new ArrayList<>();
    
    public HashSet<Type> set;
    
    /** next frame, add/remove these items */ 
    public HashSet<Type> factory;
    public HashSet<Type> trash;

    public String name;

    public Group(String name) {
        this.name = name; 

        set = new HashSet<>();
        factory = new HashSet<>();
        trash = new HashSet<>();
        
        synchronized (allGroups) { allGroups.add(this); }
    }

    public void disable() {
        synchronized (allGroups) { allGroups.remove(this); }
        // System.out.println("disabled - " + allGroups.contains(this)); sync
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
            allGroups.forEach((Group g) -> { 
                // System.out.println(g.name);
                g.update(); 
            });
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
        return name + "\tsize " + set.size() + "\tfactory " + factory.size() + "\ttrash " + trash.size();
    }
    
}
