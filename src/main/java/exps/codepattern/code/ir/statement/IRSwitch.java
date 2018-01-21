package exps.codepattern.code.ir.statement;

import exps.codepattern.code.cfg.basiccfg.BasicCFGConditionBlock;
import exps.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import exps.codepattern.code.ir.IRExpression;
import exps.codepattern.code.ir.IRScope;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class IRSwitch implements IRAbstractStatement {
	private IRExpression expression;
	private List<Pair<IRExpression, List<IRAbstractStatement>>> cases = new ArrayList<>();
	private List<IRAbstractStatement> defaultCase = new ArrayList<>();

	private int scopeIndex = 0;

	public IRSwitch(IRExpression expression) {
		this.expression = expression;
	}

	public void addCase(IRExpression caseExp) {
		cases.add(Pair.of(caseExp, new ArrayList<>()));
	}

	public IRScope getRegularScope() {
		IRScope scope = cases.get(scopeIndex).getValue()::add;
		++scopeIndex;
		return scope;
	}

	public IRScope getDefaultScope() {
		return defaultCase::add;
	}

	@Override
	public String toString() {
		String result = String.format("switch (%s):\n", expression);
		for (Pair<IRExpression, List<IRAbstractStatement>> casePair : cases) {
			IRExpression caseExpression = casePair.getKey();
			result += String.format("case (%s):\n", caseExpression);
			for (IRAbstractStatement statement : casePair.getValue())
				result += statement + "\n";
		}

		if (defaultCase != null) {
			result += "default:\n";
			for (IRAbstractStatement statement : defaultCase)
				result += statement + "\n";
		}
		result += "end switch";
		return result;
	}

	@Override
	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		// TODO: 16-1-8 处理break和fall through
		BasicCFGRegularBlock endBlock = block.getCFG().createRegularBlock();
		BasicCFGConditionBlock conditionBlock = block.getCFG().createConditionBlock();
		block.setNext(conditionBlock);
		cases.forEach(casePair -> {
			BasicCFGRegularBlock caseBlock = block.getCFG().createRegularBlock();
			conditionBlock.addNext(new BasicCFGConditionBlock.Condition.CaseCondition(expression, casePair.getKey()), caseBlock);
			for (IRAbstractStatement statement : casePair.getValue()) caseBlock = statement.buildCFG(caseBlock);
			caseBlock.setNext(endBlock);
		});
		BasicCFGRegularBlock defaultBlock = block.getCFG().createRegularBlock();
		conditionBlock.addNext(BasicCFGConditionBlock.Condition.DEFAULT, defaultBlock);
		for (IRAbstractStatement statement : defaultCase) defaultBlock = statement.buildCFG(defaultBlock);
		defaultBlock.setNext(endBlock);
		return endBlock;
	}

}
