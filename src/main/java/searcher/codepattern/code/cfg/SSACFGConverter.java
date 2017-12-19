package searcher.codepattern.code.cfg;

import searcher.codepattern.adt.WorkingList;
import searcher.codepattern.adt.graph.Node;
import searcher.codepattern.adt.graph.algorithm.DominanceFrontierResolver;
import searcher.codepattern.code.cfg.basiccfg.AbstractBasicCFGBlock;
import searcher.codepattern.code.cfg.basiccfg.BasicCFG;
import searcher.codepattern.code.cfg.basiccfg.CFGVariableImpl;
import searcher.codepattern.code.ir.statement.IRPhi;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 将CFG转换为SSA形式的工具类
 * @author huacy
 */
public class SSACFGConverter {
	private static Map<CFGVariableImpl, WrappedVariable> varMap = new HashMap<>();
	private static Multimap<Node, Node> idomTree;

	/**
	 * 将给定的<code>BasicCFG</code>转换为SSA形式，该函数会改变该<code>BasicCFG</code>
	 * @param cfg 需要转换为SSA形式的<code>BasicCFG</code>
	 */
	public static void convertToSSAForm(BasicCFG cfg) {
		if (cfg.isSSAForm()) return;

		cfg.ssaSetup();

		Multimap<Node, Node> df = DominanceFrontierResolver.resolve(cfg, cfg.getEntry());
		idomTree = DominanceFrontierResolver.getIDomTree();

		placePhi(cfg, df);

		varMap.clear();
		cfg.getVariables().stream().forEach(x -> varMap.put(x, new WrappedVariable()));
		renaming(cfg.getEntry());

	}

	private static void placePhi(BasicCFG cfg, Multimap<Node, Node> df) {
		WorkingList<AbstractBasicCFGBlock> workingList = new WorkingList<>();
		Multimap<Node, CFGVariableImpl> phiMap = HashMultimap.create();

		cfg.getVariables().stream().filter(
			v -> v.getVariableUnit().getDefBoxes().count() > 1
		).forEach(a -> {
			workingList.clear();
			workingList.addAll(a.getDefBlocks());
			while (!workingList.isEmpty()) {
				AbstractBasicCFGBlock n = workingList.pop();
				df.get(n).forEach(y -> {
					if (!phiMap.get(y).contains(a)) {
						((AbstractBasicCFGBlock) y).insertPhi(a);
						phiMap.put(y, a);
						if (!a.getDefBlocks().contains(n)) workingList.push((AbstractBasicCFGBlock) y);
					}
				});
			}
		});
	}

	private static void renaming(AbstractBasicCFGBlock block) {

		block.getStatements().forEach(s -> {
			if (!(s instanceof IRPhi)) {
				s.getUseVariables().forEach(v -> {
					CFGVariableImpl var = block.getCFG().getVariable(v.getVariable());
					int i = varMap.get(var).stack.peek();
					v.setVersion(i);
				});
			}
			if (s.getDef() == null) return;
			CFGVariableImpl variable = block.getCFG().getVariable(s.getDef().getVariable());
			WrappedVariable wrappedVariable = varMap.get(variable);
			++wrappedVariable.count;
			wrappedVariable.stack.add(wrappedVariable.count);
			s.getDef().setVersion(wrappedVariable.count);
		});

		block.getNexts().forEach(y -> {
			int j = y.getPrevIndex(block);
			y.getStatements().stream().filter(x -> x instanceof IRPhi).forEach(x -> {
				IRPhi phi = (IRPhi) x;
				int i = varMap.get(phi.getVar()).stack.peek();
				phi.replaceVar(j, i);
			});
		});

		idomTree.get(block).forEach(x -> renaming((AbstractBasicCFGBlock) x));

		block.getStatements().forEach(s -> {
			if (s.getDef() == null) return;
			CFGVariableImpl variable = block.getCFG().getVariable(s.getDef().getVariable());
			WrappedVariable wrappedVariable = varMap.get(variable);
			wrappedVariable.stack.pop();
		});
	}

	private static class WrappedVariable {
		private int count = 0;
		private Stack<Integer> stack = new Stack<>();

		private WrappedVariable() {
			stack.push(0);
		}
	}
}
