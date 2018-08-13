import java.util.Comparator;

public class Comparing extends Router implements Comparator<Long> {

    public int compare(Long v, Long w) {

        double vBKD = bkd.get(v);
        double vHeuristic = graph.distance(v, destination);
        double vVal = vBKD + vHeuristic;
//        if(v == 4235205784L || v == 1237062769L){
//            System.out.println(v + " priority: " + vVal);
//        }

        double wBKD = bkd.get(w);
        double wHeuristic = graph.distance(w, destination);
        double wVal = wBKD + wHeuristic;
//        if(w == 4235205784L || w == 1237062769L){
//            System.out.println( w + " priority: " + wVal);
//        }

        return Double.compare(vVal, wVal);
    }
}
