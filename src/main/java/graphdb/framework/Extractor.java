package graphdb.framework;

import org.neo4j.graphdb.GraphDatabaseService;

public interface Extractor {

    void config(String[] args);

    void run(GraphDatabaseService graphDB);

}
