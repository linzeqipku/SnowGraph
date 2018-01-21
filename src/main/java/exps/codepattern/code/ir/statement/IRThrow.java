package exps.codepattern.code.ir.statement;

import exps.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import exps.codepattern.code.ir.IRExpression;
import exps.codepattern.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRThrow extends IRStatement {
	private IRExpression exception;

	public IRThrow(IRExpression exception) {
		addUse(exception);

		this.exception = exception;
	}

	@Override
	public String toString() {
		return String.format("throw %s", exception);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		// TODO: 16-1-8
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return Stream.empty();
	}

	@Override
	public MiningNode toMiningNode() {
		return MiningNode.THROW;
	}

}
