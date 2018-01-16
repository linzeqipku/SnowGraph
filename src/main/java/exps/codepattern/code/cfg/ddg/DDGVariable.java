package exps.codepattern.code.cfg.ddg;

import exps.codepattern.code.cfg.CFGVariable;
import exps.codepattern.code.cfg.basiccfg.AbstractBasicCFGBlock;
import exps.codepattern.code.ir.IRExpression;
import exps.codepattern.code.ir.VariableUnit;
import exps.codepattern.code.ir.statement.IRStatement;
import exps.codepattern.utils.Predicates;
import com.google.common.collect.ImmutableSet;

import java.util.stream.Collectors;

public class DDGVariable implements CFGVariable {
	private VariableUnit variableUnit;
	private int version;

	public DDGVariable(IRExpression.IRAbstractVariable variable) {
		this.variableUnit = variable.getVariable();
		this.version = variable.getVersion();
	}

	@Override
	public int hashCode() {
		return variableUnit.hashCode() ^ version;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DDGVariable)) return false;
		DDGVariable other = (DDGVariable) obj;
		return variableUnit == other.variableUnit && version == other.version;
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
		return String.format("[DDGVariable]%s@%s", variableUnit.getName(), version);
	}
}
