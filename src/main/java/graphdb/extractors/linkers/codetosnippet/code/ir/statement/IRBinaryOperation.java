package graphdb.extractors.linkers.codetosnippet.code.ir.statement;

import graphdb.extractors.linkers.codetosnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.IRExpression;
import graphdb.extractors.linkers.codetosnippet.code.mining.MiningNode;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class IRBinaryOperation extends IRStatement {
	private Operator operator;
	private IRExpression left;
	private IRExpression right;

	public IRBinaryOperation(Operator operator, IRExpression left, IRExpression right, IRExpression.IRAbstractVariable target) {
		addUse(left);
		addUse(right);
		addDef(target);

		this.operator = operator;
		this.left = left;
		this.right = right;
		this.target = target;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		return String.format("%s = %s %s %s", target, left, operator, right);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.add(left).add(right).build();
	}

	@Override
	public MiningNode toMiningNode() {
		return new MiningNode(MiningNode.OPType.BINARY, operator.toString());
	}

	public enum Operator {
		TIMES("*"),
		DIVIDE("/"),
		REMAINDER("%"),
		PLUS("+"),
		MINUS("-"),
		LEFT_SHIFT("<<"),
		RIGHT_SHIFT_SIGNED(">>"),
		RIGHT_SHIFT_UNSIGNED(">>>"),
		LESS("<"),
		GREATER(">"),
		LESS_EQUALS("<="),
		GREATER_EQUALS(">="),
		EQUALS("=="),
		NOT_EQUALS("!="),
		XOR("^"),
		OR("|"),
		AND("&"),
		CONDITIONAL_OR("||"),
		CONDITIONAL_AND("&&"),
		INSTANCE_OF("instanceof");

		private static final Map<String, Operator> CODES = new HashMap<>();

		static {
			Operator[] ops = new Operator[]{
				TIMES, DIVIDE, REMAINDER, PLUS, MINUS, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED, LESS,
				GREATER, LESS_EQUALS, GREATER_EQUALS, EQUALS, NOT_EQUALS, XOR, OR, AND, CONDITIONAL_OR, CONDITIONAL_AND
			};
			for (Operator op : ops) CODES.put(op.toString(), op);
		}

		private String token;

		Operator(String token) {
			this.token = token;
		}

		public static Operator toOperator(String token) {
			return CODES.get(token);
		}

		@Override
		public String toString() {
			return token;
		}
	}
}
