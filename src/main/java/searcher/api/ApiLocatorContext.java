package searcher.api;

import graphdb.extractors.miners.codeembedding.line.LINEExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.tartarus.snowball.ext.EnglishStemmer;
import utils.parse.TokenizationUtils;

import java.util.*;

public class ApiLocatorContext {
    final QueryStringToQueryWordsConverter converter = new QueryStringToQueryWordsConverter();
    public final Driver connection;
    public final Map<Long, List<Double>> id2Vec = new HashMap<>();
    final Map<Long, String> id2Sig = new HashMap<>();
    final Map<Long, String> id2Name = new HashMap<>();
    final Map<Long, Set<String>> id2Words = new HashMap<>();
    final Set<Long> typeSet = new HashSet<>(); // 类或接口类型的结点集合
    final Map<Long, Set<String>> id2OriginalWords = new HashMap<>();
    final Map<String, Set<Long>> word2Ids = new HashMap<>();

    public ApiLocatorContext(Driver connection) {
        this.connection = connection;
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
            Set<String> words = new HashSet<>();
            Set<String> originalWords = new HashSet<>();
            for (String e : name.split("[^A-Za-z]+")) {
                for (String word : TokenizationUtils.camelSplit(e)) {
                    originalWords.add(word);
                    word = stem(word);
                    if (!word2Ids.containsKey(word))
                        word2Ids.put(word, new HashSet<>());
                    word2Ids.get(word).add(id);
                    words.add(word);
                }
            }
            id2OriginalWords.put(id, originalWords);
            id2Words.put(id, words);
            id2Vec.put(id, vec);
            id2Sig.put(id, sig);
            if (!m) // 如果是一个类或接口结点，加入typeset中
                typeSet.add(id);
            id2Name.put(id, name.toLowerCase()); // 未切词前的方法名，是否要stem? 因为query中的词都被stem了
            if (!word2Ids.containsKey(id2Name.get(id)))
                word2Ids.put(id2Name.get(id), new HashSet<>());
            word2Ids.get(id2Name.get(id)).add(id);
        }
        session.close();
    }

    private String stem(String word) {
        EnglishStemmer stemmer = new EnglishStemmer();
        if (word.matches("\\w+")) {
            stemmer.setCurrent(word.toLowerCase());
            stemmer.stem();
            word = stemmer.getCurrent();
        }
        return word;
    }

}