package src.world;

import java.util.*;

import src.entities.*;
import src.entities.Enemy.*;

public class Region {

    public static Region TEST_REGION = new Region("cloud tiles", "torii.gif", 
        new ObstacleInfo[] {
            new ObstacleInfo("pagoda.png", 2, 1),
            new ObstacleInfo("pagoda 2.png", 4, 1, new boolean[][] {
                {true, false, true, true}
            })
        }, 
        new Spawner[]{
            new Spawner(1, () -> new SwordAngel(0, 0)),
            // new Spawner(1, () -> new   OrbAngel(0, 0)),
        }
    );
    

    public LandTiles landTiles;

    String gateGIF;

    ObstacleInfo[] obstacles;

    Integer[] sortedCosts;
    HashMap<Integer, ArrayList<SpawnFunction>> spawnFuncs; 

    private Region(String landTilesFolder, String gateGIF, ObstacleInfo[] obstacles, Spawner[] spawners) {
        landTiles = new LandTiles(landTilesFolder);
        this.gateGIF = gateGIF;        
        this.obstacles = obstacles;

        spawnFuncs = new HashMap<>();

        for (Spawner s : spawners) {
            ArrayList<SpawnFunction> value = spawnFuncs.getOrDefault(s.cost, new ArrayList<SpawnFunction>());
            value.add(s.spawnFunction);
            spawnFuncs.put(s.cost, value);
        }
        
        sortedCosts = new Integer[spawnFuncs.keySet().size()];

        spawnFuncs.keySet().toArray(sortedCosts);
        Arrays.sort(sortedCosts);
    }

    /** bsearch but returns the lower value if doesnt exist */
    int biggestSpawnerIndex(int enemyPoints, int lo, int hi) {
        if (hi < lo) return hi;

        int mid = (hi + lo) / 2;
        if (sortedCosts[mid] > enemyPoints) return biggestSpawnerIndex(enemyPoints, lo, mid-1);
        if (sortedCosts[mid] < enemyPoints) return biggestSpawnerIndex(enemyPoints, mid+1, hi);
        
        return mid;
    }
}