package pfr.plugins.parsers.issuetracker;

import org.neo4j.graphdb.GraphDatabaseService;

public interface IssueTrackerExtractor
{
	public void extract(GraphDatabaseService db);
}
