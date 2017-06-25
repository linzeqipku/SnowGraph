package graphdb.extractors.linkers.codetosnippet.code.cfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import graphdb.extractors.linkers.codetosnippet.adt.graph.Node;
import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRStatement;

public interface CFGBlock extends Node {
	@Override
	ImmutableSet<? extends CFGBlock> getNexts();

	@Override
	ImmutableSet<? extends CFGBlock> getPrevs();

	ImmutableList<IRStatement> getStatements();
}
