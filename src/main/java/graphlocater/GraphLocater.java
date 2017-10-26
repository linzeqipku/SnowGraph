package graphlocater;


import cn.edu.pku.sei.SnowView.servlet.Config;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import graphlocater.utils.WordsUtils;
import graphlocater.wrapper.PhraseInfo;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.tartarus.snowball.ext.EnglishStemmer;
import utils.mywork.WordsUtil;

import java.io.File;
import java.util.*;

import static graphlocater.utils.WordsUtils.tokenizeCodeNames;

public class GraphLocater {
    public static double PHRASE_SIM_THRESHOLD = 0.2;

    GraphDatabaseService db = null;
    HashMap<String, Set<String>> nodeDescMap = new HashMap<>();
    EnglishStemmer stemmer = new EnglishStemmer();

    public GraphLocater(GraphDatabaseService graphdb){
        this.db = graphdb;
        try (Transaction tx = db.beginTx()) {
            ResourceIterable<Node> nodes = db.getAllNodes();
            Set<String> description = null;
            for (Node node : nodes) {
                if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
                    String name = (String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
                    description = WordsUtils.tokenizeCodeNames(name);
                    nodeDescMap.put(String.valueOf(node.getId()), description);
                }
                else if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                    String name = (String) node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                    description = WordsUtils.tokenizeCodeNames(name);
                    nodeDescMap.put(String.valueOf(node.getId()), description);
                }
                else if(node.hasLabel(Label.label(JavaCodeExtractor.METHOD))) {
                    String methodName = (String) node.getProperty(JavaCodeExtractor.METHOD_NAME);
                    Iterator<Relationship> iter = node.getRelationships(RelationshipType.withName(JavaCodeExtractor.HAVE_METHOD)).iterator();
                    Node anotherNode = iter.next().getOtherNode(node);
                    String otherName = "";
                    if (anotherNode.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
                        otherName = (String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
                    }
                    if (anotherNode.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                        otherName = (String) node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                    }
                    description = WordsUtils.tokenizeCodeNames(methodName);
                    description.addAll(tokenizeCodeNames(otherName));
                    String key = node.getId() + " " + anotherNode.getId();
                    nodeDescMap.put(key, description);
                }
            }
            tx.success();
        }
    }

    public void query(String qString) {
        List<PhraseInfo> phraseList = VPExtractor.parseSentence(qString);
        for (PhraseInfo phrase: phraseList){
            Set<String> candidate = findCandiate(phrase.getCleanWordSet());
        }
    }

    public Set<String> findCandiate(Set<String> wordSet){
        Set<String> candidate = new HashSet<>();
        for (String key: nodeDescMap.keySet()){
            Set<String> descSet = nodeDescMap.get(key);
            double score = WordsUtils.getWordSetSim(wordSet, descSet);
            if (score > PHRASE_SIM_THRESHOLD){
                candidate.add(key);
            }
        }
        return candidate;
    }

    public static void main(String[] args) {
        String graphPath = "/media/laurence/TEMP/lucene-primitive/";
        GraphDatabaseService graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphPath));

        try (Transaction tx = graphdb.beginTx()) {
            ResourceIterable<Node> nodes = graphdb.getAllNodes();
            for (Node node : nodes) {
                if (node.hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
                    String text = (String)node.getProperty(StackOverflowExtractor.QUESTION_TITLE
                            + " " + StackOverflowExtractor.QUESTION_BODY);
                    System.out.println(text);
                }
            }
            tx.success();
        }
    }
}
