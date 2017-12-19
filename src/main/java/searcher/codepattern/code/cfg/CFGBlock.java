package searcher.codepattern.code.cfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import searcher.codepattern.adt.graph.Node;
import searcher.codepattern.code.ir.statement.IRStatement;

public interface CFGBlock extends Node {
	@Override
    ImmutableSet<? extends CFGBlock> getNexts();

	@Override
    ImmutableSet<? extends CFGBlock> getPrevs();

	ImmutableList<IRStatement> getStatements();
}
