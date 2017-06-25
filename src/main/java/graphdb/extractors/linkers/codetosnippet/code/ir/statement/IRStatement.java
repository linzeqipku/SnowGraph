package graphdb.extractors.linkers.codetosnippet.code.ir.statement;

import graphdb.extractors.linkers.codetosnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.IRExpression;
import graphdb.extractors.linkers.codetosnippet.code.ir.IRExpression.IRAbstractVariable;
import graphdb.extractors.linkers.codetosnippet.code.mining.MiningNode;

import java.util.stream.Stream;

public abstract class IRStatement implements IRAbstractStatement {
	private BasicCFGRegularBlock belongBlock;
	protected IRExpression.IRAbstractVariable target;

	public void setBelongBlock(BasicCFGRegularBlock belongBlock) {
		this.belongBlock = belongBlock;
	}

	public BasicCFGRegularBlock getBelongBlock() {
		return belongBlock;
	}

	protected final void addUse(IRExpression expression) {
		if (expression instanceof IRExpression.IRAbstractVariable) {
			IRExpression.IRAbstractVariable variable = ((IRExpression.IRAbstractVariable) expression);
			variable.getVariable().addUse(this);
		}
	}

	protected final void addDef(IRExpression.IRAbstractVariable variable) {
		variable.getVariable().addDef(this);
	}

	public void replaceDef(IRExpression.IRAbstractVariable newTarget) {
		target.getVariable().removeDef(this);
		newTarget.getVariable().addDef(this);
		target = newTarget;
	}

	public IRExpression.IRAbstractVariable getDef() {
		return target;
	}

	public final Stream<IRAbstractVariable> getUseVariables() {
		return getUses(new VariableFilter()).map(x -> ((IRAbstractVariable) x));
	}

	public abstract Stream<IRExpression> getUses(ExpressionFilter builder);

	public abstract MiningNode toMiningNode();
}
