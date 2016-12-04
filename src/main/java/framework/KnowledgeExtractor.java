package framework;

import org.neo4j.graphdb.GraphDatabaseService;

public interface KnowledgeExtractor {

    void run(GraphDatabaseService graphDB);

}
