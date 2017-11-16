package searcher.graph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.tartarus.snowball.ext.EnglishStemmer;

import cn.edu.pku.sei.SnowView.servlet.Config;
import graphdb.extractors.miners.codeembedding.line.LINEExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.utils.TokenizationUtils;

public class GraphSearcher {

	Connection connection = null;

	String codeRels = JavaCodeExtractor.EXTEND + "|" + JavaCodeExtractor.IMPLEMENT + "|" + JavaCodeExtractor.THROW + "|"
			+ JavaCodeExtractor.PARAM + "|" + JavaCodeExtractor.RT + "|" + JavaCodeExtractor.HAVE_METHOD + "|"
			+ JavaCodeExtractor.HAVE_FIELD + "|" + JavaCodeExtractor.CALL_METHOD + "|" + JavaCodeExtractor.CALL_FIELD
			+ "|" + JavaCodeExtractor.TYPE + "|" + JavaCodeExtractor.VARIABLE;

	static EnglishStemmer stemmer = new EnglishStemmer();
	static QueryStringToQueryWordsConverter converter = new QueryStringToQueryWordsConverter();

	public Map<Long, List<Double>> id2Vec = new HashMap<>();
	Map<Long, String> id2Sig = new HashMap<>();
	Map<Long, String> id2Name = new HashMap<>();
	Map<Long, Set<String>> id2Words = new HashMap<>();
	Set<Long> typeSet = new HashSet<>();

	Map<String, Set<Long>> word2Ids = new HashMap<>();

	Set<String> queryWordSet = new HashSet<>();

	public GraphSearcher() {
		try {
			init();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void init() throws SQLException {
		connection = Config.getNeo4jBoltConnection();
		try (Statement statement = connection.createStatement()) {
			String stat = "match (n) where not n:" + JavaCodeExtractor.FIELD + " and exists(n." + LINEExtractor.LINE_VEC
					+ ") return " + "id(n), n." + LINEExtractor.LINE_VEC + ", n." + JavaCodeExtractor.SIGNATURE;
			ResultSet rs = statement.executeQuery(stat);
			while (rs.next()) {
				String[] eles = rs.getString("n." + LINEExtractor.LINE_VEC).trim().split("\\s+");
				List<Double> vec = new ArrayList<Double>();
				for (String e : eles)
					vec.add(Double.parseDouble(e));
				long id = rs.getLong("id(n)");
				String sig = rs.getString("n." + JavaCodeExtractor.SIGNATURE);
				if (sig.toLowerCase().contains("test"))
					continue;
				String name = sig;
				boolean m = false;
				if (name.contains("(")) {
					name = name.substring(0, name.indexOf("("));
					m = true;
				}
				if (name.contains("."))
					name = name.substring(name.indexOf(".") + 1);
				if (m && name.matches("[A-Z]\\w+"))
					continue;
				Set<String> words = new HashSet<>();
				for (String e : name.split("[^A-Za-z]+"))
					for (String word : TokenizationUtils.camelSplit(e)) {
						word = stem(word);
						if (!word2Ids.containsKey(word))
							word2Ids.put(word, new HashSet<>());
						word2Ids.get(word).add(id);
						words.add(word);
					}
				id2Words.put(id, words);
				id2Vec.put(id, vec);
				id2Sig.put(id, sig);
				if (!m)
					typeSet.add(id);
				id2Name.put(id, stem(name.toLowerCase()));
				if (!word2Ids.containsKey(id2Name.get(id)))
					word2Ids.put(id2Name.get(id), new HashSet<>());
				word2Ids.get(id2Name.get(id)).add(id);
			}

		}
	}

	public SearchResult queryExpand(String queryString){
		SearchResult r = new SearchResult();
		SearchResult searchResult1 = query(queryString);
		r.nodes.addAll(searchResult1.nodes);
		r.cost = searchResult1.cost;
		Set<Long> flags = new HashSet<>();
		for (long seed1 : searchResult1.nodes) {
			if (flags.contains(seed1))
				continue;
			for (long seed2 : searchResult1.nodes) {
				if (seed1 == seed2)
					continue;
				if (flags.contains(seed2))
					continue;
				try (Statement statement = connection.createStatement()){
					String stat="match p=shortestPath((n1)-[:"+codeRels+"*..10]-(n2)) where id(n1)="+seed1+" and id(n2)="+seed2
							+" unwind relationships(p) as r return id(startNode(r)), id(endNode(r)), id(r)";
					ResultSet rs=statement.executeQuery(stat);
					while (rs.next()){
						long node1=rs.getLong("id(startNode(r))");
						long node2=rs.getLong("id(endNode(r))");
						long rel=rs.getLong("id(r)");
						r.nodes.add(node1);
						r.nodes.add(node2);
						r.edges.add(rel);
						flags.add(seed2);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			flags.add(seed1);
		}
		return r;
	}

	public SearchResult query(String queryString) {
		/*
		 * seedMap: - key: 定位到的代码元素结点的集合 - value: 这个集合的离散度，离散度越低，说明这个图的质量越好
		 */
		List<SearchResult> graphs = findSubGraphs(queryString);

		Collections.sort(graphs, (r1, r2) -> Double.compare(r1.cost, r2.cost));

		SearchResult r = graphs.get(0);

		/*
		 * for (long id : r.nodes) { System.out.print(id2Name.get(id) + ":");
		 * List<String> wordSet = new ArrayList<>(); for (String word :
		 * id2Words.get(id)) if (queryWordSet.contains(word)) wordSet.add(word);
		 * System.out.print(StringUtils.join(wordSet, "/") + " "); }
		 * System.out.println();
		 */

		return r;
	}

	public List<SearchResult> findSubGraphs(String queryString) {

		List<SearchResult> r = new ArrayList<>();

		queryWordSet = new HashSet<>();
		queryWordSet = converter.convert(queryString);

		Set<String> tmpSet = new HashSet<>();
		for (String word : queryWordSet) {
			if (word2Ids.containsKey(word) && word2Ids.get(word).size() > 0)
				tmpSet.add(word);
		}
		queryWordSet.clear();
		queryWordSet.addAll(tmpSet);

		Set<Long> anchors = findAnchors();

		if (anchors.size() > 0) {
			Set<Long> subGraph = new HashSet<>();
			subGraph.addAll(anchors);
			for (String queryWord : queryWordSet) {

				boolean hit = false;
				for (long id : subGraph)
					if (id2Words.get(id).contains(queryWord))
						hit = true;
				if (hit)
					continue;

				double minDist = Double.MAX_VALUE;
				long minDistNode = -1;
				for (long node : word2Ids.get(queryWord)) {
					for (long anchor : anchors) {
						double dist = dist(anchor, node);
						if (dist < minDist) {
							minDist = dist;
							minDistNode = node;
						}
					}
				}
				if (minDistNode == -1)
					continue;
				subGraph.add(minDistNode);
			}
			SearchResult searchResult = new SearchResult();
			searchResult.nodes.addAll(subGraph);
			searchResult.cost = 1.0;
			r.add(searchResult);
		} else {

			Set<Long> candidateNodes = candidate();

			/*
			 * 对于每一个候选的起点， 从每个查询单词对应的代码元素集合中选取一个离它最近的代码元素
			 */
			for (long cNode : candidateNodes) {
				Set<Long> minDistNodeSet = new HashSet<>();
				minDistNodeSet.add(cNode);
				for (String queryWord : queryWordSet) {
					if (word2Ids.get(queryWord).contains(cNode))
						continue;
					double minDist = Double.MAX_VALUE;
					long minDistNode = -1;
					for (long node : word2Ids.get(queryWord)) {
						double dist = dist(cNode, node);
						if (dist < minDist) {
							minDist = dist;
							minDistNode = node;
						}
					}
					if (minDistNode == -1)
						continue;
					minDistNodeSet.add(minDistNode);
				}
				double cost = sumDist(cNode, minDistNodeSet);
				SearchResult searchResult = new SearchResult();
				searchResult.nodes.addAll(minDistNodeSet);
				searchResult.cost = cost;
				r.add(searchResult);
			}

			if (r.size() == 0)
				r.add(new SearchResult());

		}

		return r;

	}

	/**
	 * 判断queryWordSet中的哪些单词可以作为搜索的起点
	 * 
	 * @return
	 */
	Set<Long> candidate() {

		Set<Long> candidateNodes = new HashSet<>();

		for (long node : typeSet)
			if (queryWordSet.contains(id2Name.get(node)))
				candidateNodes.add(node);

		/**
		 * countSet: {<key=代码元素，value=这个代码元素对应到了几个查询单词>|value>=2}
		 */
		Set<Pair<Long, Integer>> countSet = new HashSet<>();
		int maxCount = 0;
		for (long node : id2Name.keySet()) {
			int count = 0;
			for (String word : id2Words.get(node))
				if (queryWordSet.contains(word))
					count++;

			if (count >= 2) {
				countSet.add(new ImmutablePair<Long, Integer>(node, count));
				if (count > maxCount)
					maxCount = count;
			}
		}

		/**
		 * 如果一个代码元素对应到了多个查询单词，且是最多的，则它可以作为搜索的起点
		 * 例如：代码元素SoftKittyWarmKittyLittleBallOfFur, 查询单词soft,kitty, warm,
		 * little, fur
		 */
		for (Pair<Long, Integer> pair : countSet) {
			if (pair.getValue() == maxCount)
				candidateNodes.add(pair.getKey());
		}

		/**
		 * 如果以上步骤能找到搜索的起点，那么就用它们 否则的话，再去找质量可能不是那么好的起点
		 */
		if (candidateNodes.size() > 0)
			return candidateNodes;

		for (String queryWord : queryWordSet)
			for (long node : word2Ids.get(queryWord)) {
				if (!typeSet.contains(node))
					continue;
				candidateNodes.add(node);
			}

		if (candidateNodes.size() > 0)
			return candidateNodes;

		for (String queryWord : queryWordSet)
			for (long node : word2Ids.get(queryWord)) {
				candidateNodes.add(node);
			}

		return candidateNodes;
	}

	Set<Long> findAnchors() {

		Set<Long> anchors = new HashSet<>();

		Map<String, Set<Long>> fullNameMatchMap = new HashMap<>();
		for (long node : typeSet)
			if (queryWordSet.contains(id2Name.get(node))) {
				if (!fullNameMatchMap.containsKey(id2Name.get(node)))
					fullNameMatchMap.put(id2Name.get(node), new HashSet<>());
				fullNameMatchMap.get(id2Name.get(node)).add(node);
			}
		for (String name : fullNameMatchMap.keySet())
			if (fullNameMatchMap.get(name).size() == 1)
				anchors.addAll(fullNameMatchMap.get(name));

		for (String queryWord : queryWordSet) {
			Set<Long> nodes = word2Ids.get(queryWord);
			Set<Long> types = new HashSet<>();
			for (long node : nodes)
				if (typeSet.contains(node))
					types.add(node);
			if (types.size() == 1)
				anchors.addAll(types);
		}

		// System.out.println(anchors);

		return anchors;

	}

	double sumDist(long cNode, Set<Long> minDistNodeSet) {
		double r = 0;
		for (long node : minDistNodeSet) {
			double dist = dist(cNode, node);
			r += dist;
		}
		return r;
	}

	String stem(String word) {
		if (word.matches("\\w+")) {
			stemmer.setCurrent(word.toLowerCase());
			stemmer.stem();
			word = stemmer.getCurrent();
		}
		return word;
	}

	public double dist(long node1, long node2) {
		if (!id2Vec.containsKey(node1))
			return Double.MAX_VALUE;
		if (!id2Vec.containsKey(node2))
			return Double.MAX_VALUE;
		double r = 0;
		for (int i = 0; i < id2Vec.get(node1).size(); i++)
			r += (id2Vec.get(node1).get(i) - id2Vec.get(node2).get(i))
					* (id2Vec.get(node1).get(i) - id2Vec.get(node2).get(i));
		r = Math.sqrt(r);
		return r;
	}

}