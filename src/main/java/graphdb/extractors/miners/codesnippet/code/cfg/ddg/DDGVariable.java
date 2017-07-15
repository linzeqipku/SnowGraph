package graphdb.extractors.miners.codesnippet.code.cfg.ddg;

import graphdb.extractors.miners.codesnippet.code.cfg.CFGVariable;
import graphdb.extractors.miners.codesnippet.code.cfg.basiccfg.AbstractBasicCFGBlock;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression;
import graphdb.extractors.miners.codesnippet.code.ir.VariableUnit;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRStatement;
import com.google.common.collect.ImmutableSet;

import java.util.Objects;
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
		return ImmutableSet.copyOf(variableUnit.getDefBoxes().map(IRStatement::getBelongBlock).filter(Objects::nonNull).collect(Collectors.toSet()));
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getUseBlocks() {
		return ImmutableSet.copyOf(variableUnit.getUseBoxes().map(IRStatement::getBelongBlock).filter(Objects::nonNull).collect(Collectors.toSet()));
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
