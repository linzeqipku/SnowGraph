package extractors.utils.mywork;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import extractors.parsers.javacode.JavaCodeKnowledgeExtractor;
import extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor;
import framework.KnowledgeExtractor;
import org.neo4j.graphdb.*;

/**
 * Created by laurence on 17-3-9.
 */
public class WordsUtil implements KnowledgeExtractor{
    GraphDatabaseService db = null;
    StanfordCoreNLP pipeline = null;
    Set<String> stopwords = new HashSet<>();
    Map<String, MutableInt> wordsInStackoverflow = new HashMap<>();
    Map<String, MutableInt> wordsInCode = new HashMap<>();

    @Override
    public void run(GraphDatabaseService graphDB) {
        this.db = graphDB;
        try{
            FileInputStream iStream = new FileInputStream("/home/laurence/Documents/java/working/stopwordlist");
            Scanner scanner = new Scanner(iStream);
            while (scanner.hasNext()){
                stopwords.add(scanner.next());
            }
            iStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        Properties prop = new Properties();
        prop.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(prop);

        //getCodeText();
        getStackOverflowText();

        //String text = "<p>(, _p.SnapShotDeletionPolicy\n newIndexWriterConfig 100 123 I second Sphinx, Lucene but <a href=\"http://lucene.apache.org/\">Lucene</a>";
        //String text = " href=http://lucene.apache.org/java.TooManyClauses.html  \"addon-0.4.5-an+fx+tb+fn+sm.xpi\" \"dictionaries/ru.dic\" users utils";
        //tokenize(text, wordsInCode);
    }
    public List<String> cut(String text){
        text = text.replaceAll("(\\d+)", "");

        List<String> result = new ArrayList<>();
        List<Integer> upperPos = new ArrayList<>();
        List<String> seq = new ArrayList<>();
        upperPos.add(0);
        for (int i = 0; i < text.length(); ++i){
            if (Character.isUpperCase(text.charAt(i))){
                upperPos.add(i);
            }
        }
        upperPos.add(text.length());
        for (int i = 0; i < upperPos.size() - 1; ++i){
            String sub = text.substring(upperPos.get(i), upperPos.get(i+1)); // may contain ""
            if (!sub.equals(""))
                seq.add(sub);
        }

        String buffer = "";
        for (String element : seq){
            if (element.length() == 1 && Character.isUpperCase(element.charAt(0))){
                buffer += element;
            }else{
                if (!buffer.equals("")) {
                    result.add(buffer.toLowerCase());
                    buffer = "";
                }
                result.add(element.toLowerCase());
            }
        }
        if (!buffer.equals(""))
            result.add(buffer.toLowerCase());
        return result;
    }

    public void tokenize(String originText, Map<String, MutableInt>map){
        String text = originText.replaceAll("<.*?>", " ");
        text = text.replaceAll("u[0-9a-f]{4}|0x[0-9a-f]+|https?://[^ ]+|\"[\\w+\\+\\.\\-/_]+\\.[a-zA-Z]+\"", " ");
        text = text.replaceAll("[^a-zA-Z]", " ");

        String cleanText = "";
        for (String token : text.trim().split(" ")){
            if (token.length() <= 2 )
                continue;
            String current = StringUtils.join(cut(token), " ");
            cleanText += " " + current;
            if (current.equals("elasticsearch")){
                //System.out.println(originText);
            }
        }
        //System.out.println(cleanText);

        Annotation document = new Annotation(cleanText);
        pipeline.annotate(document);
        List<CoreMap> sents = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sent : sents){
            for (CoreLabel token : sent.get(CoreAnnotations.TokensAnnotation.class)){
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                if (stopwords.contains(lemma.toLowerCase()))
                    continue;
                if (word.length() <= 2)
                    continue;
                MutableInt initVal = new MutableInt(1);
                MutableInt oldVal = map.put(word, initVal); // lemma for stack, word for code
                if (oldVal != null){
                    initVal.set(oldVal.get() + 1);
                }
            }
        }
    }

    public void getCodeText(){
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while(nodeIter.hasNext()){
                Node node = nodeIter.next();
                if (node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS))){
                    String text = (String)node.getProperty(JavaCodeKnowledgeExtractor.CLASS_CONTENT);
                    tokenize(text, wordsInCode);
                }
            }
            tx.success();
        }
        System.out.println(wordsInCode.size());

        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream("/home/laurence/Documents/java/codewords"));
            for (String key : wordsInCode.keySet()){
                writer.write(key + " " + wordsInCode.get(key).get() + '\n');
            }
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void getStackOverflowText(){
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while(nodeIter.hasNext()) {
                Node node = nodeIter.next();
                String text = "";
                if (node.hasLabel(Label.label(StackOverflowKnowledgeExtractor.QUESTION))){
                    text = (String)node.getProperty(StackOverflowKnowledgeExtractor.QUESTION_TITLE);
                    text += " " + node.getProperty(StackOverflowKnowledgeExtractor.QUESTION_BODY);
                }
                if (!text.equals("")){
                   tokenize(text, wordsInStackoverflow);
                }
            }
            tx.success();
        }
        System.out.println(wordsInStackoverflow.size());
        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream("/home/laurence/Documents/java/stackwords"));
            for (String key : wordsInStackoverflow.keySet()){
                writer.write(key + " " + wordsInStackoverflow.get(key).get() + '\n');
            }
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

class MutableInt{
    int val;
    public MutableInt(int i){
        val = i;
    }
    public int get(){
        return val;
    }
    public void set(int i){
        val = i;
    }
}