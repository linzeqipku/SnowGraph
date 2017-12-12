package searcher.graph;

import java.sql.SQLException;
import java.util.*;

import apps.Config;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.tartarus.snowball.ext.EnglishStemmer;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;

public class GraphSearcher {
    public static final double RHO = 0.25;

    private static String codeRels = JavaCodeExtractor.EXTEND + "|" + JavaCodeExtractor.IMPLEMENT + "|" + JavaCodeExtractor.THROW + "|"
            + JavaCodeExtractor.PARAM + "|" + JavaCodeExtractor.RT + "|" + JavaCodeExtractor.HAVE_METHOD + "|"
            + JavaCodeExtractor.HAVE_FIELD + "|" + JavaCodeExtractor.CALL_METHOD + "|" + JavaCodeExtractor.CALL_FIELD
            + "|" + JavaCodeExtractor.TYPE + "|" + JavaCodeExtractor.VARIABLE;

    private Driver connection = null;
    private GraphSearchData graphData = null;

    private Map<Long, Double> scoreMap = null;
    private Map<String, Set<Long>> candidateMap = new HashMap<>();

    public GraphSearcher(GraphSearchData graphData, Driver driver) {
        this.graphData = graphData;
        this.connection = driver;
    }

    public SearchResult queryExpand(String queryString) {
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
                Session session = connection.session();
                String stat = "match p=shortestPath((n1)-[:" + codeRels + "*..10]-(n2)) where id(n1)=" + seed1 + " and id(n2)=" + seed2
                        + " unwind relationships(p) as r return id(startNode(r)), id(endNode(r)), id(r)";
                StatementResult rs = session.run(stat);
                while (rs.hasNext()) {
                    Record item=rs.next();
                    long node1 = item.get("id(startNode(r))").asLong();
                    long node2 = item.get("id(endNode(r))").asLong();
                    long rel = item.get("id(r)").asLong();
                    r.nodes.add(node1);
                    r.nodes.add(node2);
                    r.edges.add(rel);
                    flags.add(seed2);
                }
                session.close();
            }
            flags.add(seed1);
        }
        return r;
    }

    public SearchResult query(String queryString) {

        if (queryString.matches("^[\\d\\s]+$")){ // 只有结点id构成的query
            List<Long> idList=new ArrayList<>();
            String[] eles=queryString.trim().split("\\s+");
            for (String e:eles)
                if (e.length()>0) {
                    Session session = connection.session();
                    long id=Long.parseLong(e);
                    String stat="match (n) where id(n)="+id+" return id(n)";
                    StatementResult rs = session.run(stat);
                    if (rs.hasNext()) {
                        idList.add(id);
                    }
                    session.close();
                }
            return idTest(idList);
        }

        /*
         * seedMap: - key: 定位到的代码元素结点的集合 - value: 这个集合的离散度，离散度越低，说明这个图的质量越好
		 */
        candidateMap.clear(); // 清空candidateMap, 对于每个query即时生成
        List<SearchResult> graphs = myFindSubGraphs(queryString);
        //graphs.sort(Comparator.comparingDouble(r -> r.cost));
        return graphs.get(0);
    }

    private SearchResult idTest(List<Long> idList){
        SearchResult r=new SearchResult();
        r.nodes.addAll(idList);
        return r;
    }

    private List<SearchResult> findSubGraphs(String queryString) {

        List<SearchResult> r = new ArrayList<>();

        Set<String> queryWordSet = WordsConverter.convert(queryString);

        Set<String> tmpSet = new HashSet<>();
        for (String word : queryWordSet) { // 除去不在代码中出现过的词，可能会去掉同义词！
            if (graphData.stemWord2Ids.containsKey(word) && graphData.stemWord2Ids.get(word).size() > 0)
                tmpSet.add(word);
        }
        queryWordSet.clear();
        queryWordSet.addAll(tmpSet);

        scoreMap = ScoreUtils.getAPISimScore(queryWordSet, graphData.id2StemWords);

        Set<Long> anchors = findAnchors(queryWordSet);

        if (anchors.size() > 0) {
            Set<Long> subGraph = new HashSet<>();
            subGraph.addAll(anchors); // anchor作为必须包含在子图中的初始结点
            for (String queryWord : queryWordSet) {

                boolean hit = false;
                for (long id : subGraph)
                    if (graphData.id2StemWords.get(id).contains(queryWord)) {
                        hit = true;
                        break;
                    }
                if (hit) // 如果这个词已经被包含在子图中，则跳过，可能有问题
                    continue;

                /*
                 * 准确包含了这个词的结点作为候选集，找一个距离当前子图最近的加入进来
                 * 过于贪心的方法，没有使得最终子图的总的距离最小，且加入结点的顺序是重要的
                 */
                double minDist = Double.MAX_VALUE;
                long minDistNode = -1;
                for (long node : graphData.stemWord2Ids.get(queryWord)) {
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

            Set<Long> candidateNodes = candidate(queryWordSet);

			/*
             * 对于每一个候选的起点， 从每个查询单词对应的代码元素集合中选取一个离它最近的代码元素
			 */
            for (long cNode : candidateNodes) {
                Set<Long> minDistNodeSet = new HashSet<>();
                minDistNodeSet.add(cNode);
                for (String queryWord : queryWordSet) {
                    if (graphData.stemWord2Ids.get(queryWord).contains(cNode)) // 如果这个词包含在起始结点中，则跳过
                        continue;
                    double minDist = Double.MAX_VALUE;
                    long minDistNode = -1;
                    for (long node : graphData.stemWord2Ids.get(queryWord)) { // 找到距离起始节点最近的加入进来
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
                double cost = sumDist(cNode, minDistNodeSet); // 整个子图的代价为到起始结点的距离之和
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

    private List<SearchResult> myFindSubGraphs(String queryString){
        List<SearchResult> r = new ArrayList<>();

        Set<String> queryWordSet = WordsConverter.convertWithoutStem(queryString);
        Set<Long> anchors = findAnchors(queryWordSet); // 可能会往candidateMap加入数据
        // 做 beamsearch 时寻找候选时可以stem，扩大匹配的范围
        System.out.println(queryWordSet);
        ScoreUtils.generateCandidateMap(candidateMap, queryWordSet, graphData.originalWord2Ids, graphData.stemWord2Ids);

        // 计算 API score
        ScoreUtils.getAPISimScore(queryWordSet, graphData.id2OriginalWords);

        if (anchors.size() > 0){
            SearchResult initialGraph = new SearchResult();
            initialGraph.nodes = anchors;
            r.add(initialGraph);
            r = beamSearch(queryWordSet, r);
        }
        else {
            Set<Long> seedSet = findSeed(scoreMap);
            for (long seed: seedSet){ // for every seed, beam search to get top k result
                SearchResult initialGraph = new SearchResult();
                Set<Long> initNodes = new HashSet<>();
                initNodes.add(seed);
                initialGraph.nodes = initNodes;
                initialGraph.gain = scoreMap.get(seed);

                List<SearchResult> curRes = new ArrayList<>();
                curRes.add(initialGraph);
                curRes = beamSearch(queryWordSet, curRes);
                r.addAll(curRes);
            }
            // sort all results, usually the size will > 10
            Comparator<SearchResult> comparator = Comparator.comparingDouble(s->s.gain);
            r.sort(comparator.reversed());
        }

        return r;
    }

    private List<SearchResult> beamSearch(Set<String> querWordSet, List<SearchResult> initialGraph){
        List<SearchResult> results = initialGraph;

        List<SearchResult> agenda = new ArrayList<>();
        for (String word: querWordSet){
            for (SearchResult curGraph: results){
                Set<Long> current = curGraph.nodes;

                List<Pair<Long, Double>> gainList = new ArrayList<>();
                for (long node: candidateMap.get(word)){
                    double gain = scoreMap.get(node);
                    if (!current.contains(node)) // 如果包含在之前的graph中，距离为 0
                        gain -= RHO * minDist(node, current);
                    gainList.add(Pair.of(node, gain));
                }
                Comparator<Pair<Long, Double>> comparator = Comparator.comparingDouble(p->p.getRight());
                gainList.sort(comparator.reversed());

                for (int i = 0; i < gainList.size() && i < 10; ++i){
                    Set<Long> next = new HashSet<>();
                    next.addAll(current);
                    next.add(gainList.get(i).getLeft());
                    SearchResult nextGraph = new SearchResult();
                    nextGraph.nodes = next;
                    nextGraph.gain = curGraph.gain + gainList.get(i).getRight();
                    agenda.add(nextGraph);
                }
            }
            Comparator<SearchResult> comparator = Comparator.comparingDouble(r->r.gain);
            agenda.sort(comparator.reversed());

            // clear and add new top 10 to results
            results.clear();
            int size = agenda.size();
            results.addAll(agenda.subList(0, size > 10 ? 10 : size));
            agenda.clear();
        }
        return results;
    }

    private Set<Long> findSeed(Map<Long, Double>scoreMap){
        Set<Long> seedSet = new HashSet<>();

        double maxScore = 0;
        for (long id: scoreMap.keySet())
            if (scoreMap.get(id) > maxScore)
                maxScore = scoreMap.get(id);

        for (long id: scoreMap.keySet()) {
            double curScore = scoreMap.get(id);
            if (curScore == maxScore) // 分数最高的结点
                seedSet.add(id);
            // 分数最高的结点可能只有几个，为增加更多seed提高容错性，把分数高于一定值的类结点也作为seed
            else if (graphData.typeSet.contains(id) && curScore >= 0.8 * maxScore)
                seedSet.add(id);
        }
        return seedSet;
    }

    private Set<Long> candidate(Set<String> queryWordSet) {

        Set<Long> candidateNodes = new HashSet<>();

        for (long node : graphData.typeSet) // 如果类名(不是全名)中含有query中的词，优先加入进候选集
            if (queryWordSet.contains(graphData.id2Name.get(node)))
                candidateNodes.add(node);

        /*
         * 统计每个结点中的词在query中出现的次数，如果超过2个，则加入countSet
         * 将出现次数最多的结点加入候选集
         */
        Set<Pair<Long, Integer>> countSet = new HashSet<>();
        int maxCount = 0;
        for (long node : graphData.id2Name.keySet()) {
            int count = 0;
            for (String word : graphData.id2StemWords.get(node))
                if (queryWordSet.contains(word))
                    count++;

            if (count >= 2) {
                countSet.add(new ImmutablePair<>(node, count));
                if (count > maxCount)
                    maxCount = count;
            }
        }

        for (Pair<Long, Integer> pair : countSet) {
            if (pair.getValue() == maxCount)
                candidateNodes.add(pair.getKey());
        }

        if (candidateNodes.size() > 0)
            return candidateNodes;

        /*
         * 如果没有超过2次的，优先找类结点作为候选，否则才找方法结点
         */
        for (String queryWord : queryWordSet)
            for (long node : graphData.stemWord2Ids.get(queryWord)) {
                if (!graphData.typeSet.contains(node))
                    continue;
                candidateNodes.add(node);
            }

        if (candidateNodes.size() > 0)
            return candidateNodes;

        for (String queryWord : queryWordSet)
            candidateNodes.addAll(graphData.stemWord2Ids.get(queryWord));

        return candidateNodes;
    }

    private Set<Long> findAnchors(Set<String> queryWordSet) {

        Set<Long> anchors = new HashSet<>();

        Map<String, Set<Long>> fullNameMatchMap = new HashMap<>();
        for (long node : graphData.id2Name.keySet()) {
            String fullName = graphData.id2Name.get(node);
            if (queryWordSet.contains(fullName)) {
                if (!fullNameMatchMap.containsKey(fullName))
                    fullNameMatchMap.put(fullName, new HashSet<>());
                fullNameMatchMap.get(fullName).add(node); // 一个全名可能对应多个同名的结点
            }
        }

        for (String name : fullNameMatchMap.keySet()) {
            Set<Long> nodes = fullNameMatchMap.get(name);
            for (long id: nodes){ // 全名匹配的结点 score = 1
                scoreMap.put(id, 1.0);
            }
            System.out.println(name + " " + nodes);

            if (nodes.size() == 1) { // 如果全名匹配的只有一个结点，那么加入anchor中
                anchors.addAll(nodes);
            } else { // 否则，如果只有一个类名结点与之匹配，加入anchor中
                Set<Long> types = new HashSet<>();
                for (long node : nodes){
                    if (graphData.typeSet.contains(node))
                        types.add(node);
                }
                if (types.size() == 1)
                    anchors.addAll(types);
                else // 如果不只一个，则加入candidate, 如queryparser会对应两个结点
                    candidateMap.putAll(fullNameMatchMap);
            }
        }
        System.out.println("anchor" + anchors);
        return anchors;
    }

    private double sumDist(long cNode, Set<Long> minDistNodeSet) {
        double r = 0;
        for (long node : minDistNodeSet) {
            double dist = dist(cNode, node);
            r += dist;
        }
        return r;
    }

    public double dist(long node1, long node2) {
        if (!graphData.id2Vec.containsKey(node1))
            return Double.MAX_VALUE;
        if (!graphData.id2Vec.containsKey(node2))
            return Double.MAX_VALUE;
        double r = 0;
        for (int i = 0; i < graphData.id2Vec.get(node1).size(); i++)
            r += (graphData.id2Vec.get(node1).get(i) - graphData.id2Vec.get(node2).get(i))
                    * (graphData.id2Vec.get(node1).get(i) - graphData.id2Vec.get(node2).get(i));
        r = Math.sqrt(r);
        return r;
    }

    public double minDist(long node, Set<Long>nodeSet){
        double mindist = Double.MAX_VALUE;
        for (long another: nodeSet){
            double cur = dist(node, another);
            if (cur < mindist)
                mindist = cur;
        }
        return mindist;
    }

    public static void main(String[] args){
        Config.getGraphSearcher().query("how to get document length in lucene");
    }
}
