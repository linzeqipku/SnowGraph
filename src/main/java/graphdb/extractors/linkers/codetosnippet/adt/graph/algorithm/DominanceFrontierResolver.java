package graphdb.extractors.linkers.codetosnippet.adt.graph.algorithm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import graphdb.extractors.linkers.codetosnippet.adt.graph.Graph;
import graphdb.extractors.linkers.codetosnippet.adt.graph.Node;

import java.util.Map;

public class DominanceFrontierResolver {
	private static Multimap<Node, Node> df;
	private static Map<Node, Node> idom;
	private static Multimap<Node, Node> child;

	public static Multimap<Node, Node> resolve(Graph g, Node entry) {
		// step 1: calc idom
		idom = ImmediateDominatorResolver.resolve(g, entry);

		// step 2: build idom tree
		child = HashMultimap.create();
		idom.forEach((k, v) -> child.put(v, k));

		// step 3: compute dominance frontier
		df = HashMultimap.create();
		compute(entry);
		return df;
	}

	public static Multimap<Node, Node> getIDomTree() {
		return child;
	}

	private static void compute(Node n) {
		// 计算 DF local
		n.getNexts().forEach(y -> {
			if (idom.get(y) != n) df.put(n, y);
		});

		// 计算 DF up
		child.get(n).forEach(c -> {
			compute(c);
			df.get(c).forEach(w -> {
				if (!isDominator(n, w) || n == w) df.put(n, w);
			});
		});
	}

	/**
	 * 计算一个结点是否是另一结点的支配点
	 *
	 * @param a 结点a
	 * @param b 结点b
	 * @return a是否是b的支配点
	 */
	private static boolean isDominator(Node a, Node b) {
		while (b != null) {
			if (a == b) return true;
			b = idom.get(b);
		}
		return false;
	}
}
