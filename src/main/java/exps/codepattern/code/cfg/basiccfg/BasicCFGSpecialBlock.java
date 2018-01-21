package exps.codepattern.code.cfg.basiccfg;

import exps.codepattern.code.ir.statement.IRStatement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public abstract class BasicCFGSpecialBlock extends AbstractBasicCFGBlock {
	public BasicCFGSpecialBlock(BasicCFG cfg, int id) {
		super(cfg, id);
	}

	@Override
	public void insertPhi(CFGVariableImpl exp) {
		throw new RuntimeException("Phi can not be inserted here!");
	}

	@Override
	public ImmutableList<IRStatement> getStatements() {
		return ImmutableList.of();
	}

	public static class Entry extends BasicCFGSpecialBlock {
		private AbstractBasicCFGBlock next;

		public Entry(BasicCFG cfg, int id) {
			super(cfg, id);
		}

		public void setNext(AbstractBasicCFGBlock next) {
			this.next = next;
			next.addPrev(this);
		}

		@Override
		public void visit() {
			if (reachable) return;
			reachable = true;
			next.visit();
		}

		@Override
		public String toString() {
			return "<ENTRY>";
		}

		@Override
		public ImmutableSet<AbstractBasicCFGBlock> getNexts() {
			return ImmutableSet.of(next);
		}

	}

	public static class Exit extends BasicCFGSpecialBlock {
		public Exit(BasicCFG cfg, int id) {
			super(cfg, id);
		}

		@Override
		public void visit() {
			if (reachable) return;
			reachable = true;
		}

		@Override
		public String toString() {
			return "<EXIT>";
		}

		@Override
		public ImmutableSet<AbstractBasicCFGBlock> getNexts() {
			return ImmutableSet.of();
		}

	}
}
