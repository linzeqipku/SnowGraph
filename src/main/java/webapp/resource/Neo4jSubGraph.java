package webapp.resource;

import searcher.api.SubGraph;
import webapp.SnowGraphContext;

import java.util.ArrayList;
import java.util.List;

public class Neo4jSubGraph {

    private final List<Neo4jNode> nodes = new ArrayList<>();

    private final List<Neo4jRelation> relationships = new ArrayList<>();

    public Neo4jSubGraph(SubGraph subgraph, SnowGraphContext context){
        for (long node:subgraph.getNodes())
            nodes.add(Neo4jNode.get(node, context));
        for (long edge:subgraph.getEdges())
            relationships.add(Neo4jRelation.get(edge, context));
    }

    public List<Neo4jNode> getNodes() {
        return nodes;
    }

    public List<Neo4jRelation> getRelationships() {
        return relationships;
    }

}
