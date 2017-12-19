package searcher.codepattern.adt.graph;

public interface Edge<NodeType> {
	NodeType getNodeA();
	NodeType getNodeB();
}
