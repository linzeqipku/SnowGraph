package graphdb.extractors.linkers.codetosnippet.code.ir.statement;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import graphdb.extractors.linkers.codetosnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.IRExpression;
import graphdb.extractors.linkers.codetosnippet.code.mining.MiningNode;

import java.util.List;
import java.util.stream.Stream;

public class IRArrayCreation extends IRStatement {
	private String type;
	private IRExpression[] size;
	private IRExpression[] initializer;

	public IRArrayCreation(String type, List<IRExpression> size, List<IRExpression> initializer, IRExpression.IRAbstractVariable target) {
		size.forEach(this::addUse);
		if (initializer != null) initializer.forEach(this::addUse);
		addDef(target);

		this.type = type;
		this.size = Iterables.toArray(size, IRExpression.class);
		if (initializer != null) this.initializer = Iterables.toArray(initializer, IRExpression.class);
		this.target = target;
	}

	@Override
	public String toString() {
		if (initializer != null)
			return String.format("%s = new %s[%s]{ %s }", target, type, Joiner.on(", ").join(size), Joiner.on(", ").join(initializer));
		return String.format("%s = new %s[%s]", target, type, Joiner.on(", ").join(size));
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		block.addNode(this);
		return block;
	}

	@Override
	public Stream<IRExpression> getUses(ExpressionFilter builder) {
		return builder.addAll(size).addAll(initializer).build();
	}

	@Override
	public MiningNode toMiningNode() {
		return MiningNode.ARRAY_CREATION;
	}
}
