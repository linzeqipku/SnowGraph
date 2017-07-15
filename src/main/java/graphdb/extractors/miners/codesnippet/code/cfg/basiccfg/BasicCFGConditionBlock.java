package graphdb.extractors.miners.codesnippet.code.cfg.basiccfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRPhi;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRStatement;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BasicCFGConditionBlock extends AbstractBasicCFGBlock {
	private List<MutablePair<Condition, AbstractBasicCFGBlock>> nexts = new ArrayList<>();
	private List<IRPhi> phis = new ArrayList<>();

	public BasicCFGConditionBlock(BasicCFG cfg, int id) {
		super(cfg, id);
	}

	public void addNext(Condition condition, AbstractBasicCFGBlock next) {
		nexts.add(new MutablePair<>(condition, next));
		next.addPrev(this);
	}

	public void replaceNext(AbstractBasicCFGBlock oldNext, AbstractBasicCFGBlock newNext) {
		nexts.forEach(p -> {
			if (p.getValue() == oldNext) p.setValue(newNext);
		});
	}

	@Override
	public String toString() {
		return String.format("[Condition Block %d]", getID());
	}

	@Override
	public void visit() {
		if (reachable) return;
		reachable = true;
		nexts.forEach(x -> x.getValue().visit());
	}

	@Override
	public void insertPhi(CFGVariableImpl exp) {
		phis.add(new IRPhi(prevs.size(), exp));
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getNexts() {
		return ImmutableSet.copyOf(nexts.stream().map(Pair::getValue).iterator());
	}

	@Override
	public ImmutableList<IRStatement> getStatements() {
		return ImmutableList.copyOf(phis);
	}

	public abstract static class Condition {
		public static final CaseElseCondition DEFAULT = new CaseElseCondition();

		public abstract Set<IRExpression> getUses();

		public static class BooleanCondition extends Condition {
			private IRExpression expression;
			private IRExpression.IRBoolean value;

			public BooleanCondition(IRExpression expression, IRExpression.IRBoolean value) {
				this.expression = expression;
				this.value = value;
			}

			@Override
			public String toString() {
				return (value == IRExpression.TRUE ? "" : "!") + expression;
			}

			@Override
			public ImmutableSet<IRExpression> getUses() {
				return ImmutableSet.of(expression);
			}
		}

		public static class CaseCondition extends Condition {
			private IRExpression expression;
			private IRExpression caseValue;

			public CaseCondition(IRExpression expression, IRExpression caseValue) {
				this.expression = expression;
				this.caseValue = caseValue;
			}

			@Override
			public String toString() {
				return expression + " == " + caseValue;
			}

			@Override
			public Set<IRExpression> getUses() {
				return ImmutableSet.of(expression, caseValue);
			}
		}

		public static class CaseElseCondition extends Condition {

			private CaseElseCondition() {
			}

			@Override
			public String toString() {
				return "else";
			}

			@Override
			public Set<IRExpression> getUses() {
				return ImmutableSet.of();
			}
		}
	}

}
