package pfr.framework;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import pfr.PFR;

public class KnowledgeGraphBuilder
{

	List<PFR> pfrPlugins=null;
	public String graphPath=null;
	
	public void setPfrPlugins(List<PFR> plugins){
		pfrPlugins=new ArrayList<PFR>(plugins);
	}
	
	public void setGraphPath(String graphPath){
		this.graphPath=graphPath;
	}
	
	public void buildGraph(){
		File f=new File(graphPath);
		try
		{
			FileUtils.deleteDirectory(f);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GraphDatabaseService db=new GraphDatabaseFactory().newEmbeddedDatabase(f);
		for (PFR pfr:pfrPlugins){
			pfr.run(db);
			System.out.println(pfr.getClass().getName()+" finished. ["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]");
		}
		db.shutdown();
	}
	
}
