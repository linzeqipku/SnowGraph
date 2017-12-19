package searcher.codepattern.code.ir.statement;

import searcher.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import searcher.codepattern.code.ir.IRExpression;
import searcher.codepattern.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRAssert extends IRStatement {
	private IRExpression condition;

	public IRAssert(IRExpression condition) {
		addUse(condition);

		this.condition = condition;
	}

	@Override
	public String toString() {
		return String.format("assert %s", condition);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		// TODO: 16-1-8
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.add(condition).build();
	}

	@Override
	public MiningNode toMiningNode() {
		return MiningNode.ASSERT;
	}

}
