package graphdb.extractors.miners.codesnippet.adt.graph.impl;

import graphdb.extractors.miners.codesnippet.adt.graph.Edge;

public class EdgeImpl<NodeType> implements Edge<NodeType> {
	private NodeType nodeA, nodeB;

	public <E> EdgeImpl(de.parsemis.graph.Edge<NodeType, E> edge) {
		if (edge.getDirection() != de.parsemis.graph.Edge.INCOMING) {
			nodeA = edge.getNodeA().getLabel();
			nodeB = edge.getNodeB().getLabel();
		} else {
			nodeA = edge.getNodeB().getLabel();
			nodeB = edge.getNodeA().getLabel();
		}
	}

	@Override
	public NodeType getNodeA() {
		return nodeA;
	}

	@Override
	public NodeType getNodeB() {
		return nodeB;
	}

	@Override
	public int hashCode() {
		return nodeA.hashCode() ^ nodeB.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EdgeImpl)) return false;
		EdgeImpl other = (EdgeImpl) obj;
		return nodeA.equals(other.nodeA) && nodeB.equals(other.nodeB);
	}
}
