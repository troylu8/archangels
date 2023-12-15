package src.util;

/** many helpful tools */
public class Util {

    public static <T> String arrStr(T[] arr) {
        if (arr.length == 0) return "[ ]";
        StringBuilder res = new StringBuilder("[ ");
        for (int i = 0; i < arr.length-1; i++) 
            res.append(arr[i] + " , ");
        return res.toString() + arr[arr.length-1] + " ]";
    }
    public static String arrStr(int[] arr) {
        if (arr.length == 0) return "[ ]";
        StringBuilder res = new StringBuilder("[ ");
        for (int i = 0; i < arr.length-1; i++) 
            res.append(arr[i] + " , ");
        return res.toString() + arr[arr.length-1] + " ]";
    }
    public static String arrStr(double[] arr) {
        if (arr.length == 0) return "[ ]";
        StringBuilder res = new StringBuilder("[ ");
        for (int i = 0; i < arr.length-1; i++) 
            res.append(arr[i] + " , ");
        return res.toString() + arr[arr.length-1] + " ]";
    }

    /** both inclusive */
    public static int rand(int min, int max) {
        return (int) (Math.random() * ((max - min) + 1) + min);
    }
    /** both inclusive */
    public static double rand(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    public static double dist(int[] pos1, int[] pos2) {
        return dist(pos1[0], pos1[1], pos2[0], pos2[1]);
    }

    /** only threads that dont start with capital letter (ones that i made) (and main) */
    public static void printAllThreads() {
        System.out.println("==threads===");
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            String name = t.getName();
            if (name.charAt(0) < 'A' || name.charAt(0) > 'Z') 
                System.out.println(name);
        }
        System.out.println("============");
    }

    public static void sleepTilInterrupt(int ms) {
        try { Thread.sleep(ms); } 
        catch (InterruptedException e) {}
    }

    public static int clamp(int val, int lo, int hi) {
        return Math.max(Math.min(val, hi), lo);
    }
    public static double clamp(double val, double lo, double hi) {
        return Math.max(Math.min(val, hi), lo);
    }

    /** xDir and yDir are relative to center */
    public static double directionToTheta(double xDir, double yDir) {
        if (xDir == 0) {
            if      (yDir > 0)  return Math.PI / 2;
            else if (yDir < 0)  return 3 * Math.PI / 2;
        }
        if (yDir == 0) {
            if      (xDir < 0)  return Math.PI;
            else if (xDir > 0)  return 0;
        }
        
        if (xDir < 0) return Math.atan( yDir / xDir ) + Math.PI;
        
        return Math.atan( yDir / xDir );
    }

    public static double[] rotatePoint(double x, double y, double originX, double originY, double radiansCW) {
        if (x == originX && y == originY)
            return new double[] {x, y};

        double newTheta = directionToTheta(x - originX, y - originY) + radiansCW;

        double dist = dist(originX, originY, x, y);

        return new double[] {
            dist * Math.cos(newTheta) + originX,
            dist * Math.sin(newTheta) + originY
        };
    }

    /** theta in radians */
    public static double[] thetaToUnitVector(double theta) {
        return new double[] {Math.cos(theta), Math.sin(theta)};
    }
}
