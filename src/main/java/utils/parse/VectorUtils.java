package utils.parse;

import java.util.List;
import java.util.Map;

public class VectorUtils {

    public static double dist(long node1, long node2, Map<Long, List<Double>> id2Vec) {
        if (!id2Vec.containsKey(node1))
            return Double.MAX_VALUE;
        if (!id2Vec.containsKey(node2))
            return Double.MAX_VALUE;
        double r = 0;
        for (int i = 0; i < id2Vec.get(node1).size(); i++)
            r += (id2Vec.get(node1).get(i) - id2Vec.get(node2).get(i))
                    * (id2Vec.get(node1).get(i) - id2Vec.get(node2).get(i));
        r = Math.sqrt(r);
        return r;
    }

}
