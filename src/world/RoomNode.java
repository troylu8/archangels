package src.world;

import java.util.*;

import src.entities.*;
import src.entities.Enemy.SpawnFunction;
import src.entities.ui.Map;
import src.util.Util;

public class RoomNode {

    public static final int WORLD_SIZE = 20;
    public static RoomNode[][][] allRooms = new RoomNode[WORLD_SIZE][WORLD_SIZE][WORLD_SIZE];

    static final int ROOM_SIZE = 50;
    static final int HALF_SIZE = ROOM_SIZE / 2;
    static char[][] map = new char[ROOM_SIZE][ROOM_SIZE];

    static final char WATER = '.';
    static final char LAND = '#';
    static final char WALL = 'W';

    /** half the edge length of a hexagon inscribed in map[][] */
    static final double HALF_EDGE_LENGTH = (int) (HALF_SIZE / Math.sqrt(3));
    static final double HEXAGON_SLOPE = Math.sqrt(3);
    
    static long nextSeed = System.currentTimeMillis();
    long seed;
    
    public Region region = Region.TEST_REGION;

    /** shows hex directions of adjacent rooms */
    public ArrayList<int[]> bridges = new ArrayList<>();

    public static int playerQ = 10;
    public static int playerR = 10;
    public static int playerS = 10;

    private int q;
    private int r;
    private int s;

    /** room is locked until enemies cleared. 
     * enemy spawns / exit spawns / map hints are dependent on room cleared */
    private boolean cleared;

    public RoomNode(int q, int r, int s) {
        this.seed = nextSeed++;
        cleared = false;
        
        this.q = q;
        this.r = r;
        this.s = s;

        allRooms[q][r][s] = this;

    }

    public int getQ() { return q; }
    public int getR() { return r; }
    public int getS() { return s; }
    public boolean isCleared() { return cleared; }

    public static void buildBridge(RoomNode r1, RoomNode r2) {
        int[] dir = {r2.q - r1.q, r2.r - r1.r, r2.s - r1.s};

        r1.bridges.add(dir);
        r2.bridges.add(getOppositeHexDirection(dir));
    }

    public static boolean isLandOrWall(int x, int y) {
        return inBounds(x, y) && (map[y][x] == LAND || map[y][x] == WALL);
    }

    public static void generateWorld(long seed) {
        System.out.println("seed: " + seed);
        
        nextSeed = seed;
        RoomNode center = new RoomNode(10, 10, 10);
        center.addNeighborsDFS(new Random(seed-1));
        // addNeighborsBFS(new Random(seed-1), center);

        center.setAsMap();
    }

    private static int MAX_NEIGHBORS = 6;
    public static int TOTAL_RADIUS = 0;

    private static void addNeighborsBFS(Random random, RoomNode center) {

        ArrayDeque<RoomNode> queue = new ArrayDeque<>();
        queue.add(center);

        while (!queue.isEmpty()) {
            int sz = queue.size();

            for (int i = 0; i < sz; i++) {
                RoomNode room = queue.removeFirst();

                if (room.getRadius() == 10) continue;
                if (room.bridges.size() >= MAX_NEIGHBORS) continue; // || random.nextDouble() > Math.pow(1.15, -getRadius())

                int max = (int) Math.ceil(Math.pow(1.2, -room.getRadius()) * MAX_NEIGHBORS);
                int targetRooms = max - Util.rand(0, 2);
                
                while (room.bridges.size() < targetRooms) {
                    int roomNum = random.nextInt(6);
                    int[] newPos;

                    int count = 0;
                    do { 
                        roomNum = (roomNum + 1) % 6;
                        newPos = new int[] {
                            room.q + HEX_DIRECTIONS[roomNum][0],
                            room.r + HEX_DIRECTIONS[roomNum][1],
                            room.s + HEX_DIRECTIONS[roomNum][2] 
                        };

                        // if no rooms are available, this could happen if there are adjacent rooms which are unconnected to this one and thus not counted in bridges.size()
                        if (count++ == MAX_NEIGHBORS) continue;
                    }
                    while (allRooms[newPos[0]][newPos[1]][newPos[2]] != null && allRooms[newPos[0]][newPos[1]][newPos[2]].bridges.size() > MAX_NEIGHBORS);
                    // while chosen room is occupied && at max bridges, choose another

                    RoomNode neighbor = (allRooms[newPos[0]][newPos[1]][newPos[2]] == null)?
                    new RoomNode(newPos[0], newPos[1], newPos[2]) :
                    allRooms[newPos[0]][newPos[1]][newPos[2]];

                    buildBridge(room, neighbor);

                    queue.add(neighbor);

                    TOTAL_RADIUS = Math.max(TOTAL_RADIUS, neighbor.getRadius());
                }
            }

        }
    }

    private void addNeighborsDFS(Random random) {

        if (getRadius() == 10) return;
        if (bridges.size() >= MAX_NEIGHBORS) return; // || random.nextDouble() > Math.pow(1.15, -getRadius())

        int max = (int) Math.ceil(Math.pow(1.2, -getRadius()) * MAX_NEIGHBORS);
        int targetRooms = max - Util.rand(0, 2);
        
        while (bridges.size() < targetRooms) {
            int room = random.nextInt(6);
            int[] newPos;

            int count = 0;
            do { 
                room = (room + 1) % 6;
                newPos = new int[] {
                    q + HEX_DIRECTIONS[room][0],
                    r + HEX_DIRECTIONS[room][1],
                    s + HEX_DIRECTIONS[room][2] 
                };

                // if no rooms are available, this could happen if there are adjacent rooms which are unconnected to this one and thus not counted in bridges.size()
                if (count++ == MAX_NEIGHBORS) return;
            }
            while (allRooms[newPos[0]][newPos[1]][newPos[2]] != null && allRooms[newPos[0]][newPos[1]][newPos[2]].bridges.size() > MAX_NEIGHBORS);
            // while chosen room is occupied && at max bridges, choose another

            RoomNode neighbor = (allRooms[newPos[0]][newPos[1]][newPos[2]] == null)?
            new RoomNode(newPos[0], newPos[1], newPos[2]) :
            allRooms[newPos[0]][newPos[1]][newPos[2]];

            buildBridge(this, neighbor);

            neighbor.addNeighborsDFS(random);

            TOTAL_RADIUS = Math.max(TOTAL_RADIUS, neighbor.getRadius());
        }

    }

    public void setAsMap() {
        playerQ = q;
        playerR = r;
        playerS = s;

        map = new char[ROOM_SIZE][ROOM_SIZE];
        
        Random random = new Random(seed);

        walk(HALF_SIZE, HALF_SIZE, 51, random);
        
        fill(0, 0, WATER);
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                fill(y, x, LAND);
            }
        }

        // no strands when walk() generates thick paths
        // removeStrands();

        addObstacles(random.nextInt(2, 5), random);

        addGates();
        
        LandTiles.calculateRotationMap();

        Map.minimap.clearAndDrawMap();
        Map.map.clearAndDrawMap();

        if (!cleared) 
            addEnemies();
    }

    public void addEnemies() {

        int enemyPoints = 1; //(int) Math.ceil(ENEMIES_SLOPE * getRadius());

        int ceil = region.sortedCosts.length-1;

        while (enemyPoints >= region.sortedCosts[0]) {
            
            ceil = region.biggestSpawnerIndex(enemyPoints, 0, ceil);

            int cost = region.sortedCosts[(int)(Math.random() * (ceil + 1))];

            /** all enemies with this cost  */
            ArrayList<SpawnFunction> choices = region.spawnFuncs.get(cost);

            Enemy enemy = choices.get( (int) (Math.random() * choices.size()) ).spawn();
            int[] pos = randomLand();
            int[] truePos = LandTiles.getTruePos(pos[0], pos[1]);
            enemy.setPosition(truePos[0], truePos[1]);
            enemy.enable();
            
            enemyPoints -= cost;
        }
    }

    public int[] randomLand() {
        int[] res;
        
        do { res = new int[] { (int) (Math.random() * ROOM_SIZE), (int) (Math.random() * ROOM_SIZE) }; } 
        while (map[res[0]][res[1]] != LAND);
        
        return res;
    }

    public static RoomNode getCurrentRoom() {
        return allRooms[playerQ][playerR][playerS];
    }

    /** how drastic (increase in enemies as you leave the center) is  */
    private static final double ENEMIES_SLOPE = 0.5;

    /** dist from center == biggest abs value of q r s */
    public int getRadius() {
        final int CENTER = WORLD_SIZE / 2;
        return Math.max(Math.max(Math.abs(q - CENTER), Math.abs(r - CENTER)), Math.abs(s - CENTER));
    }

    /** HEX_DIRECTIONS[i] is in (i * 60) + 30 degrees cw from north */
    public static final int[][] HEX_DIRECTIONS = { {1, -1, 0}, {1, 0, -1}, {0, 1, -1}, {-1, 1, 0}, {-1, 0, 1}, {0, -1, 1} };

    public static int hexDirectionToI(int[] hexDir) {
        for (int i = 0; i < HEX_DIRECTIONS.length; i++) {
            if (HEX_DIRECTIONS[i][0] == hexDir[0] &&
                HEX_DIRECTIONS[i][1] == hexDir[1] &&
                HEX_DIRECTIONS[i][2] == hexDir[2])
                return i;
        }
        return -1;
    }

    public static int[] getOppositeHexDirection(int[] hexDir) {
        return HEX_DIRECTIONS[(hexDirectionToI(hexDir) + 3) % 6];
    }

    /** returns hashmap of (direction, tile location) */
    public void addGates() {
        
        int[][] allGatePos = {
            nthLandInDirection( (int) (HALF_SIZE + HALF_EDGE_LENGTH), 0, -HEXAGON_SLOPE, -1, 2 ),
            nthLandInDirection( ROOM_SIZE-1, HALF_SIZE, 0, -1, 2 ),
            nthLandInDirection( (int) (HALF_SIZE + HALF_EDGE_LENGTH), ROOM_SIZE-1,  HEXAGON_SLOPE, -1, 2 ),
            nthLandInDirection( (int) (HALF_SIZE - HALF_EDGE_LENGTH), ROOM_SIZE-1, -HEXAGON_SLOPE, 1, 2 ),
            nthLandInDirection(0, HALF_SIZE, 0, 1, 2 ),
            nthLandInDirection( (int) (HALF_SIZE - HALF_EDGE_LENGTH), 0, HEXAGON_SLOPE, 1, 2 ),
        };

        for (int[] b : bridges) {
            int[] gatePos = allGatePos[hexDirectionToI(b)];
            new Gate(region.gateGIF, gatePos[0], gatePos[1], b).enable();
        }
    }

    /** since it only checks 1 point per |dir| column, the line it draws has gaps with sleep slopes */
    private static int[] nthLandInDirection(int startX, int startY, double slope, int dir, int n) {
        int y = startY, x = startX;

        while (inBounds(y, x)) {
            if (map[y][x] == LAND) {
                if (--n == 0) return new int[] {x, y};
            }

            x += dir;
            y = (int) (slope * (x - startX) + startY); // y = m(x - x1) + y1
        }
        
        return null;
    }

    private static  boolean inBounds(int x, int y, int thickness) {
        return y >= thickness && y < map.length - thickness && x >= thickness && x < map[0].length - thickness;
    }
    public static boolean inBounds(int x, int y) {
        return inBounds(x, y, 0);
    }
    
    private void walk(int x, int y, int step, Random random) {
        if (step == 0) return;

        int len = random.nextInt(4, 8);
        int thickness = random.nextInt(4, 6);

        final int[][] directions = {{0, (int)Math.signum(HALF_SIZE - y)}, {(int)Math.signum(HALF_SIZE - x), 0},
                                    {0,1}, {1,0}, {-1,0}, {0,-1}};
                                    
        int[] dir = directions[random.nextInt(2)];
        for (int n = 2; n < directions.length; n++) {
            if (random.nextInt(n) == 0) {
                int[] dest = {x + directions[n][0] * len, y + directions[n][1] * len};
                if (inBounds(dest[0], dest[1], thickness))
                    dir = directions[n];
            }
        }

        for (int l = 0; l < len; l++) {
            for (int wx = 0; wx < thickness; wx++) {
                for (int wy = 0; wy < thickness; wy++) {
                    map[y - thickness/2 + wx][x - thickness/2 + wy] = LAND;
                }
            }
            x += dir[0];
            y += dir[1];
        }
        
        walk(x, y, step - 1, random);
    }

    public void addObstacles(int amt, Random random) {
        for (int i = 0; i < amt; i++) {
            ObstacleInfo ob = region.obstacles[random.nextInt(region.obstacles.length)];
            
            int[] pos = new int[2];

            do { 
                pos[0] = random.nextInt(ROOM_SIZE); 
                pos[1] = random.nextInt(ROOM_SIZE); 
            }
            while (!obstacleFitsHere(ob, pos[0], pos[1]));
            
            ob.placeObstacleAt(pos[0], pos[1]);
        }
    }

    /** y and x are lower left corner 
     * if theres a hole inside the corners of the obstacle, will return true. the obstacle will cover the hole anyways 
    */
    private boolean obstacleFitsHere(ObstacleInfo ob, int x, int y) {
        final int[][] corners = { 
            {y, x}, 
            {y - ob.tileHitboxSize[1], x}, 
            {y, x + ob.tileHitboxSize[0]}, 
            {y - ob.tileHitboxSize[1], x + ob.tileHitboxSize[0]}
        };

        for (int[] c : corners) {
            if (!inBounds(c[0], c[1]) || map[c[0]][c[1]] != LAND) {
                return false;
            }
        }
        return true;
    }

    public RoomNode getAdjacentRoom(int q, int r, int s) {
        return RoomNode.allRooms[this.q + q][ this.r + r][ this.s + s];
    }

    public static void enterNextRoom(int q, int r, int s) {

        Entity.allEntities.forEachSynced((Entity e) -> {
            if (e.disableOnRoomChange) e.disable();
        });

        getCurrentRoom().getAdjacentRoom(q, r, s).setAsMap();
        
    }

    private void removeStrands() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                removeStrandHere(i, j);
            }
        }
    }
    private void removeStrandHere(int x, int y) {
        if (!inBounds(x, y)) return;
        if (map[y][x] != LAND) return;

        final int[][] directions = {{0,1}, {1,0}, {-1,0}, {0,-1}};
        int empty = 0;
        for (int[] d : directions) {
            if (!inBounds(x + d[0], y + d[1]) || map[y + d[0]][x + d[1]] == WATER) empty++;
        }
        if (empty >= 3) {
            map[y][x] = WATER;

            removeStrandHere(x + 1, y);
            removeStrandHere(x - 1, y);
            removeStrandHere(x, y + 1);
            removeStrandHere(x, y - 1);
        }
    
    }


    /** only fills blank areas */
    private void fill(int x, int y, char material) {
        if (!inBounds(x, y)) return;
        if (map[y][x] != 0) return;

        map[y][x] = material;

        fill(x + 1, y, material);
        fill(x - 1, y, material);
        fill(x, y + 1, material);
        fill(x, y - 1, material);
    }

    public static void print() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                char symbol = ((map[i][j] == 0)? ' ' : map[i][j] );

                if (i == HALF_SIZE && j == HALF_SIZE) symbol = '@';
                else {
                    for (Gate g : Gate.allGates) {
                        if (j == LandTiles.getTileVal(g.x) && i == LandTiles.getTileVal(g.y)) {
                            symbol = 'O';
                            break;
                        }
                    }
                }

                System.out.print(symbol + " ");
            }
            System.out.println("|");
        }
    }

}
