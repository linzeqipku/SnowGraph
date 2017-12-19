package searcher.codepattern.code.ir.statement;

import searcher.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import searcher.codepattern.code.ir.IRExpression;
import searcher.codepattern.code.ir.IRScope;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class IRTry implements IRAbstractStatement {
	private List<IRExpression> resources = new ArrayList<>();
	private List<IRAbstractStatement> tryStatements = new ArrayList<>();
	private List<IRAbstractStatement> finallyStatements = new ArrayList<>();

	public IRTry(List<IRExpression> resources) {
		this.resources = resources;
	}

	public IRScope getTryScope() {
		return tryStatements::add;
	}

	public IRScope getFinallyScope() {
		return finallyStatements::add;
	}

	@Override
	public String toString() {
		String result = String.format("try (%s):\n", Joiner.on(", ").join(resources));
		for (IRAbstractStatement statement : tryStatements)
			result += statement + "\n";
		if (!finallyStatements.isEmpty())
			result += "finally:\n";
		for (IRAbstractStatement statement : finallyStatements)
			result += "\n";
		result += "end try";
		return result;
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		for (IRAbstractStatement tryStatement : tryStatements) block = tryStatement.buildCFG(block);
		for (IRAbstractStatement finallyStatement : finallyStatements) block = finallyStatement.buildCFG(block);
		return block;
	}

}
