package searcher.api;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import utils.VectorUtils;


public class ApiLocator {

    public class SubGraph {
        private Set<Long> nodes=new HashSet<>();
        private Set<Long> edges=new HashSet<>();
        double cost = 0, gain = 0;
        public Set<Long> getNodes(){
            return nodes;
        }
        public Set<Long> getEdges(){
            return edges;
        }
    }

    private static final boolean debug = true;

    private static final double RHO = 0.25;
    private static final String codeRels = JavaCodeExtractor.EXTEND + "|" + JavaCodeExtractor.IMPLEMENT + "|" + JavaCodeExtractor.THROW + "|"
            + JavaCodeExtractor.PARAM + "|" + JavaCodeExtractor.RT + "|" + JavaCodeExtractor.HAVE_METHOD + "|"
            + JavaCodeExtractor.HAVE_FIELD + "|" + JavaCodeExtractor.CALL_METHOD + "|" + JavaCodeExtractor.CALL_FIELD
            + "|" + JavaCodeExtractor.TYPE + "|" + JavaCodeExtractor.VARIABLE;

    private ApiLocatorContext context;

    private Map<Long, Double> scoreMap = new HashMap<>();
    private Map<String, Set<Long>> candidateMap = new HashMap<>();

    private ApiLocator(ApiLocatorContext context) {
        this.context=context;
    }

    public static SubGraph query(String queryString, ApiLocatorContext context, boolean expand){
        return expand?new ApiLocator(context).queryExpand(queryString):new ApiLocator(context).query(queryString);
    }

    public SubGraph queryExpand(String queryString) {
        SubGraph r = new SubGraph();
        SubGraph searchResult1 = query(queryString);
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
                Session session = context.connection.session();
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

    public SubGraph query(String queryString) {

        if (queryString.matches("^[\\d\\s]+$")){ // 只有结点id构成的query
            List<Long> idList=new ArrayList<>();
            String[] eles = queryString.trim().split("\\s+");
            for (String e:eles)
                if (e.length()>0) {
                    Session session = context.connection.session();
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
        candidateMap.clear();// 清空candidateMap, 对于每个query即时生成
        scoreMap.clear();
        List<SubGraph> graphs = myFindSubGraphs(queryString);
        if (graphs.size() > 0) {
            if (debug) {
                for (SubGraph graph: graphs) {
                    System.out.println("nodes: " + graph.getNodes() + " cost: " + graph.cost);
                    for (long id: graph.getNodes())
                        System.out.println(context.id2Name.get(id) + " score: " + scoreMap.get(id));
                    System.out.println("------------------");
                }
            }
            return graphs.get(0);
        }
        return new SubGraph();
    }

    private SubGraph idTest(List<Long> idList){
        SubGraph r=new SubGraph();
        r.nodes.addAll(idList);
        return r;
    }

    private List<SubGraph> findSubGraphs(String queryString) {

        List<SubGraph> r = new ArrayList<>();

        Set<String> queryWordSet = WordsConverter.convert(queryString);

        Set<String> tmpSet = new HashSet<>();
        for (String word : queryWordSet) { // 除去不在代码中出现过的词，可能会去掉同义词！
            if (context.stemWord2Ids.containsKey(word) && context.stemWord2Ids.get(word).size() > 0)
                tmpSet.add(word);
        }
        queryWordSet.clear();
        queryWordSet.addAll(tmpSet);

        Set<Long> anchors = findAnchors(queryWordSet);

        if (anchors.size() > 0) {
            Set<Long> subGraph = new HashSet<>();
            subGraph.addAll(anchors); // anchor作为必须包含在子图中的初始结点
            for (String queryWord : queryWordSet) {

                boolean hit = false;
                for (long id : subGraph)
                    if (context.id2StemWords.get(id).contains(queryWord)) {
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
                for (long node : context.stemWord2Ids.get(queryWord)) {
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
            SubGraph searchResult = new SubGraph();
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
                    if (context.stemWord2Ids.get(queryWord).contains(cNode)) // 如果这个词包含在起始结点中，则跳过
                        continue;
                    double minDist = Double.MAX_VALUE;
                    long minDistNode = -1;
                    for (long node : context.stemWord2Ids.get(queryWord)) { // 找到距离起始节点最近的加入进来
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
                SubGraph searchResult = new SubGraph();
                searchResult.nodes.addAll(minDistNodeSet);
                searchResult.cost = cost;
                r.add(searchResult);
            }

            if (r.size() == 0)
                r.add(new SubGraph());
        }
        return r;
    }

    private List<SubGraph> myFindSubGraphs(String queryString){
        List<SubGraph> r = new ArrayList<>();

        Set<String> queryWordSet = WordsConverter.convertWithoutStem(queryString);
        if (debug)
            System.out.println(queryWordSet);

        Set<Long> anchors = findAnchors(queryWordSet); // 可能修改candidateMap, scoreMap

        // 做 beamsearch 时寻找候选时可以stem，扩大匹配的范围
        ScoreUtils.generateCandidateMap(candidateMap, queryWordSet, context.originalWord2Ids, context.stemWord2Ids);
        Set<Long> allCandidates = new HashSet<>();
        for (String key: candidateMap.keySet()){
            allCandidates.addAll(candidateMap.get(key));
        }
        // 计算 API score, queryWordSet可能含有candidateMap中没有的词
       ScoreUtils.getAPISimScore(scoreMap, queryWordSet, allCandidates, context.id2OriginalWords, context.id2StemWords);

        if (anchors.size() > 0){
            SubGraph initialGraph = new SubGraph();
            initialGraph.nodes = anchors;
            r.add(initialGraph);
            r = beamSearch(r);
        }
        else {
            Set<Long> seedSet = findSeed(scoreMap);
            for (long seed: seedSet){ // for every seed, beam search to get top k result
                SubGraph initialGraph = new SubGraph();
                Set<Long> initNodes = new HashSet<>();
                initNodes.add(seed);
                initialGraph.nodes = initNodes;
                initialGraph.gain = scoreMap.get(seed);

                List<SubGraph> curRes = new ArrayList<>();
                curRes.add(initialGraph);
                curRes = beamSearch(curRes);
                r.addAll(curRes);
            }
            // sort all results, usually the size will > 10
            r.sort(Comparator.comparingDouble(s->s.cost));
        }

        return r;
    }

    private List<SubGraph> beamSearch(List<SubGraph> initialGraph){
        List<SubGraph> results = initialGraph;

        List<SubGraph> agenda = new ArrayList<>();
        for (String word: candidateMap.keySet()){
            for (SubGraph curGraph: results){
                Set<Long> current = curGraph.nodes;

                List<Pair<Long, Double>> gainList = new ArrayList<>();
                for (long node: candidateMap.get(word)){
                    double cost = 0;
                    if (!current.contains(node)) // 如果包含在之前的graph中，距离为 0
                        cost = sumDist(node, current);
                    gainList.add(Pair.of(node, cost));
                }
                gainList.sort(Comparator.comparingDouble(p->p.getRight()));

                for (int i = 0; i < gainList.size() && i < 10; ++i){
                    Set<Long> next = new HashSet<>();
                    next.addAll(current);
                    next.add(gainList.get(i).getLeft());
                    SubGraph nextGraph = new SubGraph();
                    nextGraph.nodes = next;
                    nextGraph.cost = curGraph.cost + gainList.get(i).getRight();
                    agenda.add(nextGraph);
                }
            }
            agenda.sort(Comparator.comparingDouble(r->r.cost));

            // clear and add new top 10 to results
            results.clear();
            int size = agenda.size();
            results.addAll(agenda.subList(0, size > 10 ? 10 : size));
            agenda.clear();
        }
        return results;
    }

    private Set<Long> findSeed(Map<Long, Double>scoreMap){
        List<Pair<Long, Double>> seedSet = new ArrayList<>();

        double maxScore = 0;
        for (double val: scoreMap.values())
            if (val > maxScore)
                maxScore = val;

        for (long id: scoreMap.keySet()) {
            double curScore = scoreMap.get(id);
            if (id == 4422)
                System.out.println("4422: " + curScore);
            if (curScore == maxScore) // 分数最高的结点
                seedSet.add(Pair.of(id, curScore));
            // 分数最高的结点可能只有1-2个，为增加更多seed提高容错性，把分数高于一定值的类结点也作为seed
            else if (context.typeSet.contains(id) && curScore >= 0.8 * maxScore)
                seedSet.add(Pair.of(id, curScore));
        }
        seedSet.sort(Comparator.comparingDouble((p)->p.getRight()));
        int size = seedSet.size() >  5 ? 5 : seedSet.size(); // seed结点太多会影响效率
        return seedSet.subList(0, size).stream().map(p->p.getLeft()).collect(Collectors.toSet());
    }

    private Set<Long> candidate(Set<String> queryWordSet) {

        Set<Long> candidateNodes = new HashSet<>();

        for (long node : context.typeSet) // 如果类名(不是全名)中含有query中的词，优先加入进候选集
            if (queryWordSet.contains(context.id2Name.get(node)))
                candidateNodes.add(node);

        /*
         * 统计每个结点中的词在query中出现的次数，如果超过2个，则加入countSet
         * 将出现次数最多的结点加入候选集
         */
        Set<Pair<Long, Integer>> countSet = new HashSet<>();
        int maxCount = 0;
        for (long node : context.id2Name.keySet()) {
            int count = 0;
            for (String word : context.id2StemWords.get(node))
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
            for (long node : context.stemWord2Ids.get(queryWord)) {
                if (!context.typeSet.contains(node))
                    continue;
                candidateNodes.add(node);
            }

        if (candidateNodes.size() > 0)
            return candidateNodes;

        for (String queryWord : queryWordSet)
            candidateNodes.addAll(context.stemWord2Ids.get(queryWord));

        return candidateNodes;
    }

   private Set<Long> findAnchors(Set<String> queryWordSet) {
        Set<String> anchorIgnore = new HashSet<String>(){{add("query");}};
        Set<Long> anchors = new HashSet<>();

        Map<String, Set<Long>> fullNameMatchMap = new HashMap<>();
        for (long node : context.id2Name.keySet()) {
            String fullName = context.id2Name.get(node);
            if (queryWordSet.contains(fullName)) {
                if (!fullNameMatchMap.containsKey(fullName))
                    fullNameMatchMap.put(fullName, new HashSet<>());
                fullNameMatchMap.get(fullName).add(node); // 一个全名可能对应多个同名的结点
            }
        }
        candidateMap.putAll(fullNameMatchMap); //加入candidate, 如queryparser会对应两个结点

        for (String name : fullNameMatchMap.keySet()) {
            Set<Long> nodes = fullNameMatchMap.get(name);

            for (long id: nodes) // 全名匹配的结点 score = 1
                scoreMap.put(id, 2.0 / (queryWordSet.size() + 1));


            if (nodes.size() == 1 && !anchorIgnore.contains(name)) { // 如果全名匹配的只有一个结点，那么加入anchor中
                anchors.addAll(nodes);
            } else { // 否则，如果只有一个类名结点与之匹配，加入anchor中
                Set<Long> types = new HashSet<>();
                for (long node : nodes){
                    if (context.typeSet.contains(node))
                        types.add(node);
                }
                if (types.size() == 1 && !anchorIgnore.contains(name))
                    anchors.addAll(types);
            }
        }
        if (debug)
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

    public double minDist(long node, Set<Long>nodeSet){
        double mindist = Double.MAX_VALUE;
        for (long another: nodeSet){
            double cur = dist(node, another);
            if (cur < mindist)
                mindist = cur;
        }
        return mindist;
    }

    private double dist(long node1, long node2){
        // dist(i, j) / (w(i) * w(j))
        return VectorUtils.dist(node1,node2,context.id2Vec)/(scoreMap.get(node1) * scoreMap.get(node2));
    }

}
