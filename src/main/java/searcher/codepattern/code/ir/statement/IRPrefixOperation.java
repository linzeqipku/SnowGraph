package searcher.codepattern.code.ir.statement;

import com.github.javaparser.ast.expr.UnaryExpr;
import searcher.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import searcher.codepattern.code.ir.IRExpression;
import searcher.codepattern.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRPrefixOperation extends IRStatement {
	private UnaryExpr.Operator operator;
	private IRExpression operand;

	public IRPrefixOperation(UnaryExpr.Operator operator, IRExpression operand, IRExpression.IRAbstractVariable target) {
		addUse(operand);
		addDef(target);

		this.operator = operator;
		this.operand = operand;
		this.target = target;
	}

	public UnaryExpr.Operator getOperator() {
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
		if (operator == UnaryExpr.Operator.PLUS) return MiningNode.PRE_PLUS;
		if (operator == UnaryExpr.Operator.MINUS) return MiningNode.PRE_MINUS;
		if (operator == UnaryExpr.Operator.BITWISE_COMPLEMENT) return MiningNode.PRE_COMPLEMENT;
		if (operator == UnaryExpr.Operator.LOGICAL_COMPLEMENT) return MiningNode.PRE_NOT;
		throw new RuntimeException("Unknown operator!");
	}

}
