package graphdb.extractors.miners.codesnippet.adt.graph;

import com.google.common.collect.ImmutableSet;

public interface Graph {
	ImmutableSet<? extends Node> getNodes();
}
