package exps.codepattern.code.cfg;

import exps.codepattern.code.ir.statement.IRStatement;
import com.google.common.collect.ImmutableSet;

public interface CFGVariable {
	String getName();

	ImmutableSet<? extends CFGBlock> getDefBlocks();

	ImmutableSet<? extends CFGBlock> getUseBlocks();

	ImmutableSet<IRStatement> getDefStatements();

	ImmutableSet<IRStatement> getUseStatements();

}
