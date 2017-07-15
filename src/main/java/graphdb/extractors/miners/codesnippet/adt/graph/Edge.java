package graphdb.extractors.miners.codesnippet.adt.graph;

public interface Edge<NodeType> {
	NodeType getNodeA();
	NodeType getNodeB();
}
