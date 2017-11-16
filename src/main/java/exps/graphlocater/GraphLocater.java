package exps.graphlocater;


import exps.graphlocater.utils.WordsUtils;
import exps.graphlocater.wrapper.PhraseInfo;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import servlet.Config;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import static exps.graphlocater.utils.WordsUtils.tokenizeCodeNames;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GraphLocater {
    public static double PHRASE_SIM_THRESHOLD = 0.5;
    static final boolean WRITE_PHRASE = false;
    GraphDatabaseService db = null;
    HashMap<String, Set<String>> nodeDescMap = new HashMap<>();
    PrintWriter writer = null;


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
                        otherName = (String)anotherNode.getProperty(JavaCodeExtractor.CLASS_NAME);
                    }
                    else if (anotherNode.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                        otherName = (String)anotherNode.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                    }
                    else {
                        System.out.println("method has no class or interface");
                    }
                    description = WordsUtils.tokenizeCodeNames(methodName);
                    description.addAll(tokenizeCodeNames(otherName));
                    String key = node.getId() + " " + anotherNode.getId();
                    nodeDescMap.put(key, description);
                }
            }
            tx.success();
        }
        System.out.println("code node count: " + nodeDescMap.size());
        if (GraphLocater.WRITE_PHRASE) {
            try {
                writer = new PrintWriter(new FileOutputStream("C:\\Users\\Ling\\Documents\\phrases.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void query(String qString) {
        List<PhraseInfo> phraseList = WordsUtils.extractPhraseFromQuery(qString);
        if (GraphLocater.WRITE_PHRASE) {
            for (PhraseInfo phrase : phraseList) {
                writer.write(phrase.getText() + " " + phrase.getProofScore() + '\n');
            }
        }
        for (PhraseInfo phrase: phraseList){
            System.out.println(phrase.getText() + '\n');
            List<Pair<String, Double>> candidate = findCandiate(phrase.getCleanWordSet());
            System.out.println("candidate size: " + candidate.size() + '\n');
        }
    }

    public List<Pair<String, Double>> findCandiate(Set<String> wordSet){
        List<Pair<String, Double>> candidate = new ArrayList<>();
        if (wordSet.size() == 0) // word set maybe empty
            return candidate;

        for (String key: nodeDescMap.keySet()){
            Set<String> descSet = nodeDescMap.get(key);
            double score = WordsUtils.getWordSetSim(wordSet, descSet);
            if (score >= PHRASE_SIM_THRESHOLD){
                candidate.add(Pair.of(key, score));
            }
        }
        Collections.sort(candidate, (x1, x2)->Double.compare(x2.getValue(), x1.getValue()));
        // just for debug, print wordset and score
        System.out.println(wordSet.toString());
        for (Pair<String, Double> pair : candidate){
            Set<String> descSet = nodeDescMap.get(pair.getKey());
            System.out.println(pair.getKey() + " " + descSet.toString() + " " + pair.getValue());
        }
        return candidate;
    }

    public static void main(String[] args) {
        String graphPath = "C:\\Users\\Ling\\Documents\\graphdb-lucene-embedding";
        GraphDatabaseService graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphPath));

        String text = "";
        int count = 0;
        List<String> stackQuery = new ArrayList<>();
        try (Transaction tx = graphdb.beginTx()) {
            ResourceIterable<Node> nodes = graphdb.getAllNodes();
            for (Node node : nodes) {
                if (node.hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
                    text = node.getProperty(StackOverflowExtractor.QUESTION_TITLE)
                            + " " + node.getProperty(StackOverflowExtractor.QUESTION_BODY);
                    count++;
                    if (text.length() < 1000)
                        stackQuery.add(text);
                }
            }
            tx.success();
        }
        System.out.println("total query count: " + count + " myquery: " + stackQuery.size());
        GraphLocater graphLocater = new GraphLocater(graphdb);
        text = "increasing the boolean query's static max clauses";
        //text = "parse the query with * ";
        graphLocater.query(text);
        /*for (String query: stackQuery) {
            //System.out.println(query);
            graphLocater.query(query);
            count++;
            System.out.println(count);
        }*/

    }
}
