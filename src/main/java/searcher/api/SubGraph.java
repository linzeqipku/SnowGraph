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

    public boolean containSameNodes(Set<Long> s){
        if (nodes.size() != s.size())
            return false;
        if (nodes.containsAll(s))
            return true;
        else
            return false;
    }
}