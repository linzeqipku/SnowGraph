package graphdb.extractors.linkers.codetosnippet.code.ir.statement;


import graphdb.extractors.linkers.codetosnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.IRExpression;
import graphdb.extractors.linkers.codetosnippet.code.mining.MiningNode;

import java.util.stream.Stream;

public class IRFieldAccess extends IRStatement {
	private IRExpression receiver;
	private String field;

	public IRFieldAccess(IRExpression receiver, String field, IRExpression.IRAbstractVariable target) {
		addUse(receiver);
		addDef(target);

		this.receiver = receiver;
		this.field = field;
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("%s = %s.%s", target, receiver, field);
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.add(receiver).build();
	}

	@Override
	public MiningNode toMiningNode() {
		return new MiningNode(MiningNode.OPType.FIELD_ACCESS, field);
	}
}
