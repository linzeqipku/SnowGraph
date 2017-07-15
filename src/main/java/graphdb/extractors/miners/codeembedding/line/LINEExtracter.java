package graphdb.extractors.miners.codeembedding.line;

import graphdb.extractors.miners.codeembedding.trans.TransE;
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
        line.writeData(db);
    }
}
