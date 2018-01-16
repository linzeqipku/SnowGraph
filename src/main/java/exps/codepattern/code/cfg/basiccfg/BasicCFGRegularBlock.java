package exps.codepattern.code.cfg.basiccfg;

import exps.codepattern.code.ir.statement.IRPhi;
import exps.codepattern.code.ir.statement.IRStatement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;

public class BasicCFGRegularBlock extends AbstractBasicCFGBlock {
	private List<IRStatement> nodes = new ArrayList<>();
	private AbstractBasicCFGBlock next;

	public BasicCFGRegularBlock(BasicCFG cfg, int id) {
		super(cfg, id);
	}

	public void addNode(IRStatement node) {
		nodes.add(node);
		node.setBelongBlock(this);
	}

	public AbstractBasicCFGBlock getNext() {
		return next;
	}

	public void setNext(AbstractBasicCFGBlock next) {
		this.next = next;
		next.addPrev(this);
	}

	public void removeStatement(IRStatement statement) {
		nodes.remove(statement);
		statement.getDef().getVariable().removeDef(statement);
		statement.getUseVariables().forEach(x -> x.getVariable().removeUse(statement));
	}

	@Override
	public ImmutableList<IRStatement> getStatements() {
		return ImmutableList.copyOf(nodes);
	}

	@Override
	public String toString() {
		return String.format("[Regular Block %d]", getID());
	}

	@Override
	public void visit() {
		if (reachable) return;
		reachable = true;
		next.visit();
	}

	@Override
	public void insertPhi(CFGVariableImpl exp) {
		nodes.add(0, new IRPhi(prevs.size(), exp));
	}

	public void removeBlock() {
		next.prevs.addAll(prevs);
		next.prevs.remove(this);
		for (AbstractBasicCFGBlock prev : prevs) {
			if (prev instanceof BasicCFGRegularBlock) ((BasicCFGRegularBlock) prev).setNext(next);
			else if (prev instanceof BasicCFGSpecialBlock.Entry) ((BasicCFGSpecialBlock.Entry) prev).setNext(next);
			else if (prev instanceof BasicCFGConditionBlock) ((BasicCFGConditionBlock) prev).replaceNext(this, next);
		}
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getNexts() {
		return ImmutableSet.of(next);
	}
}
