package graphdb.extractors.linkers.codetosnippet.code.mining;

import de.parsemis.graph.Graph;
import de.parsemis.graph.ListGraph;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.general.IntFrequency;
import de.parsemis.parsers.IntLabelParser;
import de.parsemis.strategy.BFSStrategy;
import graphdb.extractors.linkers.codetosnippet.code.cfg.CFG;
import graphdb.extractors.linkers.codetosnippet.code.cfg.ddg.DDG;
import graphdb.extractors.linkers.codetosnippet.code.cfg.ddg.DDGBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRMethodInvocation;
import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Miner {
	public static Logger logger = Logger.getLogger("graphdb.extractors.linkers.codetosnippet.code.mining.Miner");

	public static Settings<MiningNode, Integer> createSetting(int minFreq, int minNodes) {
		Settings<MiningNode, Integer> setting = new Settings<>();
		setting.algorithm = new de.parsemis.algorithms.gSpan.Algorithm<>();
		setting.strategy = new BFSStrategy<>();
		setting.minFreq = new IntFrequency(minFreq);
		setting.factory = new ListGraph.Factory<>(new MiningNodeParser(), new IntLabelParser());
		setting.minNodes = minNodes;

		return setting;
	}

	public static List<CFG> mine(List<String> bodys, Settings<MiningNode, Integer> setting) {
		List<Graph<MiningNode, Integer>> graphs = new ArrayList<>();
		List<DDG> ddgs = bodys.stream()
			.map(DDG::createCFG)
			.filter(Objects::nonNull)
			.filter(g -> g.getBlocks().stream().anyMatch(Miner::filterAPI))
			.collect(Collectors.toList());
		return mineFromDDG(ddgs, setting);
	}

	public static List<Graph<MiningNode, Integer>> mineGraph(List<String> bodys, Settings<MiningNode, Integer> setting) {
		List<Graph<MiningNode, Integer>> graphs = new ArrayList<>();
		List<DDG> ddgs = bodys.stream()
			.map(DDG::createCFG)
			.filter(Objects::nonNull)
			.filter(g -> g.getBlocks().stream().anyMatch(Miner::filterAPI))
			.collect(Collectors.toList());
		return mineGraphFromDDG(ddgs, setting);
	}

	public static List<Graph<MiningNode, Integer>> mineGraphFromDDG(Collection<DDG> ddgs, Settings<MiningNode, Integer> setting) {
		List<Graph<MiningNode, Integer>> graphs = ddgs.stream().map(MiningGraph::convertDDGToMiningGraph).collect(Collectors.toList());
		return MiningGraph.resultFilter(de.parsemis.Miner.mine(graphs, setting));
	}

	public static List<CFG> mineFromDDG(List<DDG> ddgs, Settings<MiningNode, Integer> setting) {
		return mineGraphFromDDG(ddgs, setting).stream().map(r -> MiningGraph.createCFGFromMiningGraph(ddgs, r)).collect(Collectors.toList());
	}

	private static boolean filterAPI(DDGBlock block) {
		IRStatement s = block.getStatement();
		if (!(s instanceof IRMethodInvocation)) return false;
		IRMethodInvocation m = (IRMethodInvocation) s;
		return m.getMethodName().equals("search");
	}
}
