package graphlocater;

import graphdb.extractors.miners.codeembedding.line.LINEExtracter;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.neo4j.graphdb.*;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.util.*;

public class GraphLocater {
    GraphDatabaseService db = null;
    HashMap<HashSet<Long>, List<String>> nodeDescMap = new HashMap<>();
    EnglishStemmer stemmer = new EnglishStemmer();

    public GraphLocater(GraphDatabaseService graphdb){
        this.db = graphdb;
        try (Transaction tx = db.beginTx()) {
            ResourceIterable<Node> nodes = db.getAllNodes();
            List<String> description = null;
            for (Node node : nodes) {
                if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
                    String name = (String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
                    description = tokenizeCodeNames(name);
                }
                else if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                    String name = (String) node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                    description = tokenizeCodeNames(name);
                }
                else if(node.hasLabel(Label.label(JavaCodeExtractor.METHOD))) {
                    String name = (String) node.getProperty(JavaCodeExtractor.METHOD_NAME);
                    Iterator<Relationship> iter = node.getRelationships(RelationshipType.withName(JavaCodeExtractor.HAVE_METHOD)).iterator();
                    Node anotherNode = iter.next().getOtherNode(node);
                    String otherName = "";
                    if (anotherNode.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
                        otherName = (String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
                    }
                    if (anotherNode.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                        otherName = (String) node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                    }
                }
            }
            tx.success();
        }
    }

    public static List<String> tokenizeCodeNames(String name){
        List<String> result = new ArrayList<>(4);

        return result;
    }
}
