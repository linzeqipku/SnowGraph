package graphdb.extractors.miners.codesnippet;

import de.parsemis.graph.Graph;
import graphdb.extractors.miners.codesnippet.code.cfg.ddg.DDG;
import graphdb.extractors.miners.codesnippet.code.mining.MiningGraph;
import graphdb.extractors.miners.codesnippet.code.mining.MiningNode;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sorter {
	public static double rank(DDG ddg, List<Graph<MiningNode, Integer>> minedGraphs) {
		return minedGraphs.stream().filter(g -> MiningGraph.findSubDDG(ddg, g) != null).count();
	}

	public static <T> List<T> sort(Map<T, DDG> ddgs, List<Graph<MiningNode, Integer>> minedGraphs) {
		List<Pair<T, Double>> scores = new ArrayList<>();

		for (T key: ddgs.keySet()) {
			scores.add(Pair.of(key, rank(ddgs.get(key), minedGraphs)));
		}

		Collections.sort(scores, (x, y) -> y.getRight().compareTo(x.getRight()));

		return scores.stream().map(Pair::getLeft).collect(Collectors.toList());
	}
}
