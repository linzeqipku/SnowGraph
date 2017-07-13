package graphdb.extractors.miners.codesnippet.code.ir.statement;

import graphdb.extractors.miners.codesnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression;
import graphdb.extractors.miners.codesnippet.code.mining.MiningNode;
import org.eclipse.jdt.core.dom.PrefixExpression;

import java.util.stream.Stream;

public class IRPrefixOperation extends IRStatement {
	private PrefixExpression.Operator operator;
	private IRExpression operand;

	public IRPrefixOperation(PrefixExpression.Operator operator, IRExpression operand, IRExpression.IRAbstractVariable target) {
		addUse(operand);
		addDef(target);

		this.operator = operator;
		this.operand = operand;
		this.target = target;
	}

	public PrefixExpression.Operator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		return String.format("%s = %s%s", target, operator, operand);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.add(operand).build();
	}

	@Override
	public MiningNode toMiningNode() {
		if (operator == PrefixExpression.Operator.PLUS) return MiningNode.PRE_PLUS;
		if (operator == PrefixExpression.Operator.MINUS) return MiningNode.PRE_MINUS;
		if (operator == PrefixExpression.Operator.COMPLEMENT) return MiningNode.PRE_COMPLEMENT;
		if (operator == PrefixExpression.Operator.NOT) return MiningNode.PRE_NOT;
		throw new RuntimeException("Unknown operator!");
	}

}
