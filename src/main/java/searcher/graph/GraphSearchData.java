package searcher.graph;

import graphdb.extractors.miners.codeembedding.line.LINEExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import utils.parse.TokenizationUtils;
import java.util.*;

public class GraphSearchData {
    private Driver connection = null;

    public Set<Long> typeSet = new HashSet<>(); // 类或接口类型的结点集合
    
    public Map<Long, List<Double>> id2Vec = new HashMap<>();
    public Map<Long, String> id2Sig = new HashMap<>();
    public Map<Long, String> id2Name = new HashMap<>();
    
    public Map<Long, Set<String>> id2StemWords = new HashMap<>();
    public Map<Long, Set<String>> id2OriginalWords = new HashMap<>();

    public Map<String, Set<Long>> originalWord2Ids = new HashMap<>();
    public Map<String, Set<Long>> stemWord2Ids = new HashMap<>();

    public GraphSearchData(Driver driver){
        try{
            connection = driver;
            Session session = connection.session();
            // 获取所有Method Class Interface 的结点
            String stat = "match (n) where not n:" + JavaCodeExtractor.FIELD + " and exists(n." + LINEExtractor.LINE_VEC
                    + ") return " + "id(n), n." + LINEExtractor.LINE_VEC + ", n." + JavaCodeExtractor.SIGNATURE;
            StatementResult rs = session.run(stat);
            while (rs.hasNext()) {
                Record item = rs.next();
                String[] eles = item.get("n." + LINEExtractor.LINE_VEC).asString().trim().split("\\s+");
                List<Double> vec = new ArrayList<>();
                for (String e : eles)
                    vec.add(Double.parseDouble(e));
                long id = item.get("id(n)").asLong();
                String sig = item.get("n." + JavaCodeExtractor.SIGNATURE).asString();
                if (sig.toLowerCase().contains("test")) // 规则： 去掉含有test的结点
                    continue;
                String name = sig;
                boolean m = false; // 是否是一个方法结点
                if (name.contains("(")) {
                    name = name.substring(0, name.indexOf("("));
                    m = true;
                }
                if (name.contains(".")) // 获取最低一级的名字作为他的全名，对于方法名是否合适？
                    name = name.substring(name.lastIndexOf(".") + 1);
                if (m && name.matches("[A-Z]\\w+")) // 忽略大写字母开头的方法？
                    continue;
                Set<String> stemWords = new HashSet<>();
                Set<String> originalWords = new HashSet<>();
                for (String e : name.split("[^A-Za-z]+")) {
                    for (String word : TokenizationUtils.camelSplit(e)) {
                        originalWords.add(word);
                        if (!originalWord2Ids.containsKey(word))
                            originalWord2Ids.put(word, new HashSet<>());
                        originalWord2Ids.get(word).add(id);

                        word = WordsConverter.stem(word);
                        if (!stemWord2Ids.containsKey(word))
                            stemWord2Ids.put(word, new HashSet<>());
                        stemWord2Ids.get(word).add(id);
                        stemWords.add(word);
                    }
                }
                id2OriginalWords.put(id, originalWords);
                id2StemWords.put(id, stemWords);
                id2Vec.put(id, vec);
                id2Sig.put(id, sig);
                if (!m) // 如果是一个类或接口结点，加入typeset中
                    typeSet.add(id);
                id2Name.put(id, name.toLowerCase()); // 未切词前的方法名，是否要stem? 因为query中的词都被stem了
                if (!stemWord2Ids.containsKey(id2Name.get(id)))
                    stemWord2Ids.put(id2Name.get(id), new HashSet<>());
                stemWord2Ids.get(id2Name.get(id)).add(id);
            }
            session.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
