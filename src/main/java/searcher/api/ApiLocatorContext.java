package searcher.api;

import graphdb.extractors.miners.codeembedding.line.LINEExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import jdk.nashorn.internal.parser.Token;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.index.LuceneSearcher;
import utils.TokenizationUtils;

import java.util.*;

public class ApiLocatorContext {
    public final Driver connection;

    final LuceneSearcher luceneSearcher;

    final Set<Long> typeSet = new HashSet<>(); // 类或接口类型的结点集合

    public final Map<Long, List<Double>> id2Vec = new HashMap<>();
    final Map<Long, String> id2Sig = new HashMap<>();
    final Map<Long, String> id2Name = new HashMap<>();

    final Map<Long, Set<String>> id2StemWords = new HashMap<>();
    final Map<Long, Set<String>> id2OriginalWords = new HashMap<>();
    final Map<String, Set<Long>> stemWord2Ids = new HashMap<>();
    final Map<String, Set<Long>> originalWord2Ids = new HashMap<>();

    public ApiLocatorContext(Driver connection, String dataDir) {
        this.connection = connection;
        this.luceneSearcher=new LuceneSearcher(this, dataDir);
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
            String shortName = name;
            String className = "";
            if (name.contains(".")) // 获取最低一级的名字作为他的全名，对于方法名是否合适？
                shortName = name.substring(name.lastIndexOf(".") + 1);
            if (m){
                if (shortName.matches("[A-Z]\\w+")) // 忽略大写字母开头的方法 - 构造方法
                    continue;
                String[] nameList = name.split("\\.");
                className = nameList[nameList.length - 2];
            }
            Set<String> words = new HashSet<>();
            Set<String> originalWords = new HashSet<>();
            for (String e : shortName.split("[^A-Za-z]+")) {
                for (String word : TokenizationUtils.camelSplit(e)) {
                    originalWords.add(word); // contains stop words like is with...
                    if (!originalWord2Ids.containsKey(word))
                        originalWord2Ids.put(word, new HashSet<>());
                    originalWord2Ids.get(word).add(id);
                    word = WordsConverter.stem(word);
                    if (!stemWord2Ids.containsKey(word))
                        stemWord2Ids.put(word, new HashSet<>());
                    stemWord2Ids.get(word).add(id);
                    words.add(word);
                }
            }
            if (m){ // 加入方法的类名作为结点的描述词，在计算权重时用到，而索引时不需要
                originalWords.addAll(TokenizationUtils.camelSplit(className));
            }
            id2OriginalWords.put(id, originalWords);
            id2StemWords.put(id, words);
            id2Vec.put(id, vec);
            id2Sig.put(id, sig);
            if (!m) // 如果是一个类或接口结点，加入typeset中
                typeSet.add(id);
            id2Name.put(id, shortName.toLowerCase()); // 未切词前的方法名，是否要stem? 因为query中的词都被stem了
            if (!stemWord2Ids.containsKey(WordsConverter.stem(shortName.toLowerCase())))
                stemWord2Ids.put(WordsConverter.stem(shortName.toLowerCase()), new HashSet<>());
            stemWord2Ids.get(WordsConverter.stem(shortName.toLowerCase())).add(id);
            if (!originalWord2Ids.containsKey(shortName.toLowerCase()))
                originalWord2Ids.put(shortName.toLowerCase(), new HashSet<>());
            originalWord2Ids.get(shortName.toLowerCase()).add(id);
        }
        session.close();
    }

    public LuceneSearcher getLuceneSearcher() {
        return luceneSearcher;
    }

    public Map<Long, String> getId2Sig() {
        return id2Sig;
    }

    public Map<Long, String> getId2Name() {
        return id2Name;
    }
}