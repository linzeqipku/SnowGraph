package searcher.codepattern.code.cfg.basiccfg;

import searcher.codepattern.code.cfg.CFGBlock;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

/**
 * 常规控制流图基本块的抽象类
 *
 * @author huacy
 */
public abstract class AbstractBasicCFGBlock implements CFGBlock {
	protected boolean reachable = false;
	protected Set<AbstractBasicCFGBlock> prevs = new HashSet<>();
	private BasicCFG cfg;
	private int id;

	public AbstractBasicCFGBlock(BasicCFG cfg, int id) {
		this.cfg = cfg;
		this.id = id;
	}

	public BasicCFG getCFG() {
		return cfg;
	}

	public int getID() {
		return id;
	}

	public abstract void visit();

	public abstract void insertPhi(CFGVariableImpl exp);

	public void addPrev(AbstractBasicCFGBlock prev) {
		prevs.add(prev);
	}

	public int getPrevIndex(AbstractBasicCFGBlock prev) {
		int index = 0;
		for (AbstractBasicCFGBlock p : prevs) {
			if (p == prev) return index;
			++index;
		}
		throw new RuntimeException(String.format("%s is not a prev of %s", prev, this));
	}

	public void checkPrev() {
		prevs.removeIf(p -> !cfg.getBlocks().contains(p));
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getPrevs() {
		return ImmutableSet.copyOf(prevs);
	}

	@Override
	public abstract ImmutableSet<AbstractBasicCFGBlock> getNexts();

}
