package exps.codepattern.code.ir.statement;

import exps.codepattern.code.cfg.basiccfg.BasicCFGConditionBlock;
import exps.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import exps.codepattern.code.ir.IRExpression;
import exps.codepattern.code.ir.IRScope;

import java.util.ArrayList;
import java.util.List;

public class IRIf implements IRAbstractStatement {
	private IRExpression condition;
	private List<IRAbstractStatement> thenStatements = new ArrayList<>();
	private List<IRAbstractStatement> elseStatements = new ArrayList<>();

	private IRScope thenScope = thenStatements::add;
	private IRScope elseScope = elseStatements::add;

	public IRIf(IRExpression condition) {
		this.condition = condition;
	}

	public IRScope getThenScope() {
		return thenScope;
	}

	public IRScope getElseScope() {
		return elseScope;
	}

	@Override
	public String toString() {
		String result = String.format("if (%s):\n", condition);
		for (IRAbstractStatement statement : thenStatements)
			result += statement + "\n";
		if (!elseStatements.isEmpty()) {
			result += "else:\n";
			for (IRAbstractStatement statement : elseStatements)
				result += statement + "\n";
		}

		result += "end if";
		return result;
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		BasicCFGRegularBlock thenBlock = block.getCFG().createRegularBlock();
		BasicCFGRegularBlock elseBlock = block.getCFG().createRegularBlock();
		BasicCFGRegularBlock endBlock = block.getCFG().createRegularBlock();
		BasicCFGConditionBlock conditionBlock = block.getCFG().createConditionBlock();
		block.setNext(conditionBlock);
		conditionBlock.addNext(new BasicCFGConditionBlock.Condition.BooleanCondition(condition, IRExpression.TRUE), thenBlock);
		conditionBlock.addNext(new BasicCFGConditionBlock.Condition.BooleanCondition(condition, IRExpression.FALSE), elseBlock);
		for (IRAbstractStatement thenStatement : thenStatements) thenBlock = thenStatement.buildCFG(thenBlock);
		for (IRAbstractStatement elseStatement : elseStatements) elseBlock = elseStatement.buildCFG(elseBlock);
		thenBlock.setNext(endBlock);
		elseBlock.setNext(endBlock);
		return endBlock;
	}

}
