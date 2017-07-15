package graphdb.extractors.miners.codesnippet.code.cfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import graphdb.extractors.miners.codesnippet.adt.graph.Node;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRStatement;

public interface CFGBlock extends Node {
	@Override
	ImmutableSet<? extends CFGBlock> getNexts();

	@Override
	ImmutableSet<? extends CFGBlock> getPrevs();

	ImmutableList<IRStatement> getStatements();
}
