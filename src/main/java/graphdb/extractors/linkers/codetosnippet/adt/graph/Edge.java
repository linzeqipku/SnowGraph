package graphdb.extractors.linkers.codetosnippet.adt.graph;

public interface Edge<NodeType> {
	NodeType getNodeA();
	NodeType getNodeB();
}
