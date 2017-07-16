package graphdb.extractors.miners.codeembedding.line;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.PropertyDeclaration;
import org.neo4j.graphdb.*;

/**
 * Created by laurence on 17-7-15.
 */
public class LINEExtracter implements Extractor{
    @PropertyDeclaration
    public static final String CODE_TRANSE_VEC = "lineVec";
    LINE line = null;
    GraphDatabaseService db = null;

    public void run(GraphDatabaseService db) {
        this.db = db;
        line = new LINE();
        line.readData(db);
        line.run();
        writeData();
    }
    public void writeData(){
        try(Transaction tx = db.beginTx()){
            for (long key : line.vertex.keySet()){
                double[] embedding = line.vertex.get(key).emb_vertex;
                String line = "";
                for (double x : embedding)
                    line += x + " ";
                line = line.trim();
                Node node = db.getNodeById(key);
                node.setProperty(LINEExtracter.CODE_TRANSE_VEC, line);
            }
            tx.success();
        }
    }
}
