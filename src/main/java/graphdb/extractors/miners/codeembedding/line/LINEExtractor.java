package graphdb.extractors.miners.codeembedding.line;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.PropertyDeclaration;
import org.neo4j.graphdb.*;

/**
 * Created by laurence on 17-7-15.
 */
public class LINEExtractor implements Extractor{
    @PropertyDeclaration
    public static final String LINE_VEC = "lineVec";
    private LINE line = null;
    private GraphDatabaseService db = null;

    @Override
    public void config(String[] args) {

    }

    public void run(GraphDatabaseService db) {
        this.db = db;
        try (Transaction tx=db.beginTx()) {
            for (Node node : db.getAllNodes()) {
                if (node.hasProperty(LINE_VEC))
                    node.removeProperty(LINE_VEC);
            }
            tx.success();
        }
        line = new LINE();
        line.readData(db);
        line.run();
        writeData();
    }
    private void writeData(){
        try(Transaction tx = db.beginTx()){
            for (String key : line.vertex.keySet()){
                double[] embedding = line.vertex.get(key).emb_vertex;
                String line = "";
                for (double x : embedding)
                    line += x + " ";
                line = line.trim();
                Node node = db.findNode(Label.label(JavaCodeExtractor.CLASS),JavaCodeExtractor.SIGNATURE, key);
                if (node==null)
                    node = db.findNode(Label.label(JavaCodeExtractor.INTERFACE),JavaCodeExtractor.SIGNATURE, key);
                if (node==null)
                    node = db.findNode(Label.label(JavaCodeExtractor.METHOD),JavaCodeExtractor.SIGNATURE, key);
                if (node!=null)
                    node.setProperty(LINEExtractor.LINE_VEC, line);
            }
            tx.success();
        }
    }
}
