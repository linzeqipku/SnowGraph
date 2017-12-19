package searcher.codepattern.code.ir.statement;

import searcher.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import searcher.codepattern.code.ir.IRExpression;
import searcher.codepattern.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRReference extends IRStatement {
	private IRExpression operand;
	private String ref;

	public IRReference(IRExpression operand, String ref, IRExpression.IRAbstractVariable target) {
		addUse(operand);
		addDef(target);

		this.operand = operand;
		this.ref = ref;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("%s = %s::%s", target, operand, ref);
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
		return new MiningNode(MiningNode.OPType.REFERENCE, ref);
	}

}
