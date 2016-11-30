package extractors.miners.codeembedding;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import extractors.parsers.javacode.JavaCodeKnowledgeExtractor;
import framework.KnowledgeExtractor;
import framework.annotations.PropertyDeclaration;

public class CodeEmbeddingKnowledgeExtractor implements KnowledgeExtractor {

    @PropertyDeclaration
    public static final String CODE_TRANSE_VEC = "transVec";

    GraphDatabaseService db = null;
    TransE transE = null;

    public void run(GraphDatabaseService db) {
        this.db = db;
        transE = new TransE();
        prepare();
        transE.run();
        writeVecLines();
    }

    private void prepare() {
        List<String> entities = new ArrayList<String>();
        List<String> relations = new ArrayList<String>();
        List<Triple<String, String, String>> triples = new ArrayList<Triple<String, String, String>>();
        try (Transaction tx = db.beginTx()) {
            for (Node node : db.getAllNodes()) {
                if (!node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS)) &&
                        !node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.INTERFACE)) &&
                        !node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.METHOD)) &&
                        !node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.FIELD)))
                    continue;
                entities.add("" + node.getId());
            }

            for (Relationship rel : db.getAllRelationships()) {
                Node node1 = rel.getStartNode();
                if (!node1.hasLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS)) &&
                        !node1.hasLabel(Label.label(JavaCodeKnowledgeExtractor.INTERFACE)) &&
                        !node1.hasLabel(Label.label(JavaCodeKnowledgeExtractor.METHOD)) &&
                        !node1.hasLabel(Label.label(JavaCodeKnowledgeExtractor.FIELD)))
                    continue;
                Node node2 = rel.getEndNode();
                if (!node2.hasLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS)) &&
                        !node2.hasLabel(Label.label(JavaCodeKnowledgeExtractor.INTERFACE)) &&
                        !node2.hasLabel(Label.label(JavaCodeKnowledgeExtractor.METHOD)) &&
                        !node2.hasLabel(Label.label(JavaCodeKnowledgeExtractor.FIELD)))
                    continue;
                triples.add(new ImmutableTriple<String, String, String>("" + node1.getId(), "" + node2.getId(), rel.getType().name()));
                if (!relations.contains(rel.getType().name()))
                    relations.add(rel.getType().name());
            }
            tx.success();
        }
        transE.prepare(entities, relations, triples);
    }

    private void writeVecLines() {
        Map<String, double[]> embeddings = transE.getEntityVecMap();
        List<String> keys = new ArrayList<String>(embeddings.keySet());
        for (int i = 0; i < keys.size(); i += 1000) {
            try (Transaction tx = db.beginTx()) {
                for (int j = 0; j < 1000; j++) {
                    if (i + j >= keys.size())
                        break;
                    String nodeIdString = keys.get(i + j);
                    Node node = db.getNodeById(Long.parseLong(nodeIdString));
                    String line = "";
                    for (double d : embeddings.get(nodeIdString))
                        line += d + " ";
                    line = line.trim();
                    setVec(node, line);
                }
                tx.success();
            }
        }
    }

    private void setVec(Node node, String line) {
        node.setProperty(CODE_TRANSE_VEC, line);
    }

}
