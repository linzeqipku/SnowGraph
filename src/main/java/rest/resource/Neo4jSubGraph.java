package rest.resource;

import rest.resource.Neo4jNode;
import rest.resource.Neo4jRelation;
import searcher.api.ApiLocator;

import java.util.ArrayList;
import java.util.List;

public class Neo4jSubGraph {

    private final List<Neo4jNode> nodes = new ArrayList<>();

    private final List<Neo4jRelation> relationships = new ArrayList<>();

    public Neo4jSubGraph(ApiLocator.SubGraph subgraph){
        for (long node:subgraph.getNodes())
            nodes.add(Neo4jNode.get(node));
        for (long edge:subgraph.getEdges())
            relationships.add(Neo4jRelation.get(edge));
    }

    public List<Neo4jNode> getNodes() {
        return nodes;
    }

    public List<Neo4jRelation> getRelationships() {
        return relationships;
    }

}
