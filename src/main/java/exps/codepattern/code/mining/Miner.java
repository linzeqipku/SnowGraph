package exps.codepattern.code.mining;

import de.parsemis.graph.Graph;
import de.parsemis.graph.ListGraph;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.general.IntFrequency;
import de.parsemis.parsers.IntLabelParser;
import de.parsemis.strategy.BFSStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import exps.codepattern.code.cfg.ddg.DDG;
import exps.codepattern.utils.FileUtils;
import exps.codepattern.utils.Predicates;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Miner {
	public static Logger logger = LoggerFactory.getLogger(Miner.class);

	public static Settings<MiningNode, Integer> createSetting(int minFreq, int minNodes) {
		Settings<MiningNode, Integer> setting = new Settings<>();
		setting.algorithm = new de.parsemis.algorithms.gSpan.Algorithm<>();
		setting.strategy = new BFSStrategy<>();
		setting.minFreq = new IntFrequency(minFreq);
		setting.factory = new ListGraph.Factory<>(new MiningNodeParser(), new IntLabelParser());
		setting.minNodes = minNodes;

		return setting;
	}

	public static List<Graph<MiningNode, Integer>> mineFromFiles(List<String> files) {
		Settings<MiningNode, Integer> setting = new Settings<>();
		setting.algorithm = new de.parsemis.algorithms.gSpan.Algorithm<>();
		setting.strategy = new BFSStrategy<>();
		setting.minFreq = new IntFrequency(2);
		setting.factory = new ListGraph.Factory<>(new MiningNodeParser(), new IntLabelParser());
		setting.minNodes = 2;

		return mineFromFiles(files, setting);
	}

	public static List<Graph<MiningNode, Integer>> mineFromFiles(List<String> files, Settings<MiningNode, Integer> setting) {
		List<String> bodys = files.stream()
			.map(File::new)
			.map(FileUtils::getFileContent)
			.collect(Collectors.toList());

		return mine(bodys, setting);
	}

	public static List<Graph<MiningNode, Integer>> mine(List<String> bodys, Settings<MiningNode, Integer> setting) {
		List<Graph<MiningNode, Integer>> graphs = new ArrayList<>();
		List<DDG> ddgs = bodys.stream()
			.map(DDG::createCFG)
			.filter(Predicates.notNull())
			.collect(Collectors.toList());
		return mineGraphFromDDG(ddgs, setting);
	}

	public static List<Graph<MiningNode, Integer>> mineGraph(List<String> bodys, Settings<MiningNode, Integer> setting) {
		List<DDG> ddgs = bodys.stream()
			.map(DDG::createCFG)
			.map(x -> (DDG) x)
			.filter(Predicates.notNull())
			.collect(Collectors.toList());
		return mineGraphFromDDG(ddgs, setting);
	}

	public static List<Graph<MiningNode, Integer>> mineGraphFromDDG(Collection<DDG> ddgs, Settings<MiningNode, Integer> setting) {
		List<Graph<MiningNode, Integer>> graphs = ddgs.stream().map(MiningGraph::convertDDGToMiningGraph).collect(Collectors.toList());
		logger.info("Mining from {} ddgs", graphs.size());
		return MiningGraph.resultFilter(de.parsemis.Miner.mine(graphs, setting));
	}

}
