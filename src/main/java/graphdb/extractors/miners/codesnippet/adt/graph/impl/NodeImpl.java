package graphdb.extractors.miners.codesnippet.adt.graph.impl;

import com.google.common.collect.ImmutableSet;
import graphdb.extractors.miners.codesnippet.adt.graph.Node;

import java.util.HashSet;
import java.util.Set;

public class NodeImpl implements Node {
	private Set<Node> next = new HashSet<>();
	private Set<Node> prev = new HashSet<>();
	private String name;

	public NodeImpl(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void add(NodeImpl n) {
		next.add(n);
		n.prev.add(this);
	}

	@Override
	public ImmutableSet<Node> getNexts() {
		return ImmutableSet.copyOf(next);
	}

	@Override
	public ImmutableSet<Node> getPrevs() {
		return ImmutableSet.copyOf(prev);
	}
}
