package exps.codepattern.code.mining;

import exps.codepattern.adt.LispList;
import exps.codepattern.adt.graph.impl.EdgeImpl;
import exps.codepattern.code.cfg.CFG;
import exps.codepattern.code.cfg.ddg.DDG;
import exps.codepattern.code.cfg.ddg.DDGBlock;
import exps.codepattern.code.cfg.plaincfg.PlainCFG;
import exps.codepattern.code.ir.IRExpression;
import exps.codepattern.code.ir.statement.IRAbstractStatement;
import exps.codepattern.code.ir.statement.IRStatement;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Ordering;
import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.ListGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.general.Fragment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MiningGraph {

	public static ListGraph<MiningNode, Integer> convertDDGToMiningGraph(DDG ddg) {
	    return convertDDGToMiningGraph(ddg, false);
    }

	public static ListGraph<MiningNode, Integer> convertDDGToMiningGraph(DDG ddg, boolean dataNode) {
		Map<DDGBlock, Node<MiningNode, Integer>> blockMap = new HashMap<>();

		ListGraph<MiningNode, Integer> graph = new ListGraph<>();

		for (DDGBlock block : ddg.getBlocks()) {
			IRStatement s = block.getStatement();
			MiningNode node;
			if (s == null) node = MiningNode.EMPTY;
			else node = s.toMiningNode();
			blockMap.put(block, graph.addNode(node));
		}

		// 将DDG的边对应至MiningGraph的边
		blockMap.forEach((block, node) ->
			block.getNexts().stream().map(blockMap::get).filter(x -> node != x).forEach(x -> graph.addEdge(node, x, 0, Edge.OUTGOING))
		);

//		 添加常数、extern结点
		if (dataNode) {
			blockMap.forEach((block, node) -> {
				if (block.getStatement() == null) return;
				block.getStatement().getUses(new IRAbstractStatement.PreDefinedFilter())
					.filter(x -> !(x instanceof IRExpression.IRAbstractVariable))
					.forEach(use -> graph.addEdge(graph.addNode(use.toMiningNode()), node, 0, Edge.OUTGOING));
			});
		}

		return graph;
	}

	public static <N, E> void printMiningGraph(Graph<N, E> g) {
		Iterator<Edge<N, E>> ite = g.edgeIterator();
		while (ite.hasNext()) {
			Edge<N, E> e = ite.next();
			switch (e.getDirection()) {
				case Edge.INCOMING:
					System.out.println(e.getNodeB().getLabel() + " -> " + e.getNodeA().getLabel());
					break;
				case Edge.UNDIRECTED:
					System.out.println(e.getNodeA().getLabel() + " -- " + e.getNodeB().getLabel());
					break;
				case Edge.OUTGOING:
					System.out.println(e.getNodeA().getLabel() + " -> " + e.getNodeB().getLabel());
					break;
			}
		}
		System.out.println();
	}

	public static <N, E> List<Graph<N, E>> resultFilter(Collection<Fragment<N, E>> result) {
		List<Graph<N, E>> graphs = new ArrayList<>();
		List<Set<exps.codepattern.adt.graph.Edge<N>>> edgeSets = new ArrayList<>();
		result.stream().map(Fragment::toGraph).sorted(Ordering.natural().reverse().onResultOf(Graph::getEdgeCount)).forEach(graph -> {
			Iterable<Edge<N, E>> ite = graph::edgeIterator;
			Set<exps.codepattern.adt.graph.Edge<N>> edges = StreamSupport.stream(ite.spliterator(), false).map(EdgeImpl<N>::new).collect(Collectors.toSet());
			boolean add = true;
			for (Set<exps.codepattern.adt.graph.Edge<N>> edgeSet : edgeSets) {
				if (edgeSet.containsAll(edges)) {
					add = false;
					break;
				}
			}
			if (add) {
				graphs.add(graph);
				edgeSets.add(edges);
			}
		});
		return graphs;
	}

	public static CFG createCFGFromMiningGraph(Collection<DDG> ddgs, Graph<MiningNode, Integer> result) {
		for (DDG ddg : ddgs) {
			Set<DDGBlock> subBlocks = MiningGraph.findSubDDG(ddg, result);
			if (subBlocks == null) continue;
			CFG cfg = PlainCFG.createCFG(subBlocks);
			return cfg;
		}
		return null;
	}

	/**
	 * 判断挖掘出的图是否是给定DDG的一个子图
	 * @param ddg 给定的DDG
	 * @param sub 挖掘出的图
	 * @return 如果是子图，则返回sub在ddg中对应结点的集合，反之，返回null
	 */
	public static Set<DDGBlock> findSubDDG(DDG ddg, Graph<MiningNode, Integer> sub) {
		BiMap<DDGBlock, Node<MiningNode, Integer>> nodeMap = HashBiMap.create();
		LispList<Node<MiningNode, Integer>> remain = LispList.copyOf(sub.nodeIterator());
		if(findSubDDG(ddg, nodeMap, remain.car(), remain.cdr())) return nodeMap.keySet();
		return null;
	}

	private static boolean findSubDDG(DDG ddg, BiMap<DDGBlock, Node<MiningNode, Integer>> nodeMap, Node<MiningNode, Integer> current, LispList<Node<MiningNode, Integer>> remain) {
		for (DDGBlock block : ddg.getBlocks()) {
			if (nodeMap.containsKey(block)) continue;
			if (!equals(block, current.getLabel())) continue;

			boolean outDiff = false;
			Iterator<Edge<MiningNode, Integer>> outs = current.outgoingEdgeIterator();
			while (outs.hasNext()) {
				Edge<MiningNode, Integer> outEdge = outs.next();
				Node<MiningNode, Integer> other = outEdge.getOtherNode(current);
				if (!nodeMap.containsValue(other)) continue;
				DDGBlock mappedDDGBlock = nodeMap.inverse().get(other);
				if (!block.getNexts().contains(mappedDDGBlock)) {
					outDiff = true;
					break;
				}
			}
			if (outDiff) continue;

			boolean inDiff = false;
			Iterator<Edge<MiningNode, Integer>> ins = current.incommingEdgeIterator();
			while (ins.hasNext()) {
				Edge<MiningNode, Integer> inEdge = ins.next();
				Node<MiningNode, Integer> other = inEdge.getOtherNode(current);
				if (!nodeMap.containsValue(other)) continue;
				DDGBlock mappedDDGBlock = nodeMap.inverse().get(other);
				if (!mappedDDGBlock.getNexts().contains(block)) {
					inDiff = true;
					break;
				}
			}
			if (inDiff) continue;

			nodeMap.put(block, current);
			if (remain.isEmpty()) return true;
			if (findSubDDG(ddg, nodeMap, remain.car(), remain.cdr())) return true;
			nodeMap.remove(block);
		}

		return false;
	}

	/**
	 * 判断一个DDG是否是另一个DDG的一个子图
	 * @param ddg 给定的DDG
	 * @param sub 给定的子图
	 * @return 如果是子图，则返回true
	 */
	public static boolean findSubDDG(DDG ddg, DDG sub) {
		BiMap<DDGBlock, DDGBlock> nodeMap = HashBiMap.create();
		LispList<DDGBlock> remain = new LispList<>(sub.getBlocks().asList());
		if(findSubDDG(ddg, nodeMap, remain.car(), remain.cdr())) return true;
		return false;
	}

	private static boolean findSubDDG(DDG ddg, BiMap<DDGBlock, DDGBlock> nodeMap, DDGBlock current, LispList<DDGBlock> remain) {
		for (DDGBlock block : ddg.getBlocks()) {
			if (nodeMap.containsKey(block)) continue;
			if (!equals(block, current)) continue;

			boolean outDiff = false;

			for (DDGBlock out: current.getNexts()) {
				if (!nodeMap.containsValue(out)) continue;
				DDGBlock mappedDDGBlock = nodeMap.inverse().get(out);
				if (!block.getNexts().contains(mappedDDGBlock)) {
					outDiff = true;
					break;
				}
			}
			if (outDiff) continue;

			boolean inDiff = false;

			for (DDGBlock in: current.getPrevs()) {
				if (!nodeMap.containsValue(in)) continue;
				DDGBlock mappedDDGBlock = nodeMap.inverse().get(in);
				if (!block.getNexts().contains(mappedDDGBlock)) {
					inDiff = true;
					break;
				}
			}
			if (inDiff) continue;

			nodeMap.put(block, current);
			if (remain.isEmpty()) return true;
			if (findSubDDG(ddg, nodeMap, remain.car(), remain.cdr())) return true;
			nodeMap.remove(block);
		}

		return false;
	}

	private static boolean equals(DDGBlock block, MiningNode node) {
		if (block.getStatement() == null) return node == MiningNode.EMPTY;
		return block.getStatement().toMiningNode().equals(node);
	}

	private static boolean equals(DDGBlock block, DDGBlock node) {
		if (node.getStatement() == null) return equals(block, MiningNode.EMPTY);
		return equals(block, node.getStatement().toMiningNode());
	}
}
