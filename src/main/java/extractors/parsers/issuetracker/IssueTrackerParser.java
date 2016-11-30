package extractors.parsers.issuetracker;

import org.neo4j.graphdb.GraphDatabaseService;

public interface IssueTrackerParser {
    public void parse(GraphDatabaseService db);
}
