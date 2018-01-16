package searcher.api;

import java.util.HashSet;
import java.util.Set;

public class SubGraph {
    Set<Long> nodes=new HashSet<>();
    Set<Long> edges=new HashSet<>();
    public double cost = 0, gain = 0;

    public Set<Long> getNodes() {
        return nodes;
    }

    public Set<Long> getEdges() {
        return edges;
    }
}