package graphdb.framework;

import org.neo4j.graphdb.GraphDatabaseService;

public interface Extractor {

    void run(GraphDatabaseService graphDB);

}
