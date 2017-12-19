package searcher.codepattern.adt.graph.algorithm;

import searcher.codepattern.adt.graph.Graph;
import searcher.codepattern.adt.graph.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImmediateDominatorResolver {
	private static int dfsIndex;
	private static WrappedNode[] dfn;
	private static Map<Node, WrappedNode> map = new HashMap<>();

	/**
	 * 给定图和入口结点，计算每个点的直接支配结点
	 * @param g 给定的图
	 * @param entry 给定的入口结点
	 * @return 每个结点对应至其直接支配结点的map
	 */
	public static Map<Node, Node> resolve(Graph g, Node entry) {
		// step 1: init
		map.clear();
		dfsIndex = 0;
		g.getNodes().stream().forEach(x -> map.put(x, new WrappedNode(x)));
		dfn = new WrappedNode[map.size()];

		// step 2: dfs
		dfs(null, map.get(entry));

		for (int i = dfsIndex - 1; i > 0; --i) {
			WrappedNode n = dfn[i];
			final WrappedNode p = n.parent;

			// step 3
			n.prevs.forEach(prev -> {
				WrappedNode u = eval(prev);
				if (u.semi.dfn < n.semi.dfn) n.semi = u.semi;
			});
			n.semi.bucket.add(n);
			link(p, n);

			// step 4
			p.bucket.forEach(v -> {
				WrappedNode u = eval(v);
				v.idom = u.semi.dfn < p.dfn ? u : p;
			});
		}

		// step 5
		for (int i = 1; i < dfsIndex; ++i) {
			WrappedNode n = dfn[i];
			if (n.idom != n.semi) n.idom = n.idom.idom;
		}

		return Stream.of(dfn).filter(x -> x.idom != null).collect(Collectors.toMap(x -> x.wrappingNode, x -> x.idom.wrappingNode));
	}

	private static WrappedNode eval(WrappedNode v) {
		if (v.ancestor == null) return v.label;
		compress(v);
		if (v.ancestor.label.semi.dfn >= v.label.semi.dfn) return v.label;
		return v.ancestor.label;
	}

	private static void compress(WrappedNode v) {
		if (v.ancestor.ancestor == null) return;
		compress(v.ancestor);
		if (v.ancestor.label.semi.dfn < v.label.semi.dfn) v.label = v.ancestor.label;
		v.ancestor = v.ancestor.ancestor;
	}

	private static void link(WrappedNode v, WrappedNode w) {
		WrappedNode s = w;
		while (s.child != null && w.label.semi.dfn < s.child.label.semi.dfn) {
			int grandChildSize = s.child.child == null ? 0 : s.child.child.size;
			if (s.size + grandChildSize >= 2 * s.child.size) {
				s.child.ancestor = s;
				s.child = s.child.child;
			} else {
				s.child.size = s.size;
				s = s.ancestor = s.child;
			}
		}

		s.label = w.label;
		v.size = v.size + w.size;
		if (v.size < 2 * w.size) {
			WrappedNode temp = s;
			s = v.child;
			v.child = temp;
		}
		while (s != null) {
			s.ancestor = v;
			s = s.child;
		}
	}

	private static void dfs(WrappedNode parent, WrappedNode node) {
		if (node.dfn != -1) return;

		dfn[dfsIndex] = node;
		node.init(dfsIndex, parent);
		++dfsIndex;

		node.nexts.forEach(x -> dfs(node, x));
	}

	private static class WrappedNode {
		private Node wrappingNode;
		private Set<WrappedNode> nexts;     // next node in CFG
		private Set<WrappedNode> prevs;     // prev node in CFG
		private int dfn;                    // dfs number
		private WrappedNode parent;         // parent in dfs tree
		private WrappedNode ancestor;       // ancestor in generated forest
		private WrappedNode child;          //
		private WrappedNode semi;           // semidominator
		private WrappedNode label;          //
		private int size;                   //
		private WrappedNode idom;           // immediate dominator
		private Set<WrappedNode> bucket;    // all nodes whose semidominator is this

		public WrappedNode(Node wrappingNode) {
			this.wrappingNode = wrappingNode;
			this.dfn = -1;
			this.bucket = new HashSet<>();
		}

		public void init(int dfn, WrappedNode parent) {
			this.dfn = dfn;
			this.semi = this;
			this.label = this;
			this.parent = parent;
			this.size = 1;
			this.nexts = wrappingNode.getNexts().stream().map(map::get).collect(Collectors.toSet());
			this.prevs = wrappingNode.getPrevs().stream().map(map::get).collect(Collectors.toSet());
		}

		@Override
		public String toString() {
			return wrappingNode.toString();
		}

		@Override
		public int hashCode() {
			return dfn;
		}
	}
}
