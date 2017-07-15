package graphdb.extractors.miners.codesnippet.code.ir.statement;

import graphdb.extractors.miners.codesnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression;
import graphdb.extractors.miners.codesnippet.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRArrayAccess extends IRStatement {
	private IRExpression array;
	private IRExpression index;

	public IRArrayAccess(IRExpression array, IRExpression index, IRExpression.IRAbstractVariable target) {
		addUse(array);
		addUse(index);
		addDef(target);

		this.array = array;
		this.index = index;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("%s = %s[%s]", target, array, index);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.add(array).add(index).build();
	}

	@Override
	public MiningNode toMiningNode() {
		return MiningNode.ARRAY_ACCESS;
	}

}
