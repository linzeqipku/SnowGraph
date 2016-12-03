package extractors.miners.mailcode;

import extractors.miners.codeembedding.TransE;
import framework.KnowledgeExtractor;
import framework.annotations.PropertyDeclaration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class MailCodeExtractor implements KnowledgeExtractor {

    @PropertyDeclaration
    public static final String CODE_TRANSE_VEC = "transVec";

    GraphDatabaseService db = null;
    TransE transE = null;

    public void run(GraphDatabaseService db) {
        this.db = db;
        transE = new TransE();
        prepare();
        transE.run();
    }

    private void prepare() {

    }

    private void setVec(Node node, String line) {
        node.setProperty(CODE_TRANSE_VEC, line);
    }

}
