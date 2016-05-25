package graphfusion;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public abstract class Linker {
	protected GraphDatabaseService graphDb = null;
	
	public Linker(String dbPath){
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
	}
	
	public abstract void link();
}
