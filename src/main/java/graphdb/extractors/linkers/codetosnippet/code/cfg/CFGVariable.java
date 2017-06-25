package graphdb.extractors.linkers.codetosnippet.code.cfg;

import com.google.common.collect.ImmutableSet;
import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRStatement;

public interface CFGVariable {
	String getName();

	ImmutableSet<? extends CFGBlock> getDefBlocks();

	ImmutableSet<? extends CFGBlock> getUseBlocks();

	ImmutableSet<IRStatement> getDefStatements();

	ImmutableSet<IRStatement> getUseStatements();

}
