package framework;

import org.neo4j.graphdb.GraphDatabaseService;

public interface KnowledgeExtractor {

    public void run(GraphDatabaseService graphDB);

}
