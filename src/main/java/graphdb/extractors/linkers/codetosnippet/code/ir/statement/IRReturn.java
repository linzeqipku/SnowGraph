package graphdb.extractors.linkers.codetosnippet.code.ir.statement;

import graphdb.extractors.linkers.codetosnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.IRExpression;
import graphdb.extractors.linkers.codetosnippet.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRReturn extends IRStatement {
	private IRExpression result;

	public IRReturn(IRExpression result) {
		addUse(result);

		this.result = result;
	}

	@Override
	public String toString() {
		if (result == null) return "return";
		return String.format("return %s", result);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		block.setNext(block.getCFG().getExit());
		return block.getCFG().createRegularBlock();
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.add(result).build();
	}

	@Override
	public MiningNode toMiningNode() {
		return MiningNode.RETURN;
	}

}
