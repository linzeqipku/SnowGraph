package exps.codepattern.code.cfg.basiccfg;

import com.google.common.collect.ImmutableSet;
import exps.codepattern.code.cfg.CFGVariable;
import exps.codepattern.code.ir.VariableUnit;
import exps.codepattern.code.ir.statement.IRStatement;
import exps.codepattern.utils.Predicates;

import java.util.stream.Collectors;

public class CFGVariableImpl implements CFGVariable {
	private VariableUnit variableUnit;

	public CFGVariableImpl(VariableUnit variableUnit) {
		this.variableUnit = variableUnit;
	}

	public VariableUnit getVariableUnit() {
		return variableUnit;
	}

	@Override
	public String getName() {
		return variableUnit.getName();
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getDefBlocks() {
		return ImmutableSet.copyOf(variableUnit.getDefBoxes().map(IRStatement::getBelongBlock).filter(Predicates.notNull()).collect(Collectors.toSet()));
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getUseBlocks() {
		return ImmutableSet.copyOf(variableUnit.getUseBoxes().map(IRStatement::getBelongBlock).filter(Predicates.notNull()).collect(Collectors.toSet()));
	}

	@Override
	public ImmutableSet<IRStatement> getDefStatements() {
		return ImmutableSet.copyOf(variableUnit.getDefBoxes().collect(Collectors.toSet()));
	}

	@Override
	public ImmutableSet<IRStatement> getUseStatements() {
		return ImmutableSet.copyOf(variableUnit.getUseBoxes().collect(Collectors.toSet()));
	}

	@Override
	public String toString() {
		return String.format("[CFGVariableImpl]%s", variableUnit.getName());
	}
}
