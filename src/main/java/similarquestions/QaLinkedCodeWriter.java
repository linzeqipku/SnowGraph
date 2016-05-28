package similarquestions;

import java.util.Set;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import graphmodel.ManageElements;

public class QaLinkedCodeWriter {

	SimilarQuestionTaskConfig config = null;
	
	public static void main(String[] args){
		QaLinkedCodeWriter p=new QaLinkedCodeWriter("apache-poi");
		p.run();
	}

	public QaLinkedCodeWriter(String projectName) {
		config = new SimilarQuestionTaskConfig(projectName);
	}

	public void run(){
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasLabel(ManageElements.Labels.QUESTION)&&!node.hasLabel(ManageElements.Labels.ANSWER))
					continue;
				Set<Long> codeSet=new HashSet<Long>();
				Iterator<Relationship> rels=node.getRelationships(ManageElements.RelTypes.DOC_LEVEL_REFER,Direction.OUTGOING).iterator();
				while (rels.hasNext())
					codeSet.add(rels.next().getEndNode().getId());
				rels=node.getRelationships(ManageElements.RelTypes.LEX_LEVEL_REFER,Direction.OUTGOING).iterator();
				while (rels.hasNext())
					codeSet.add(rels.next().getEndNode().getId());
				if (codeSet.size()==0)
					continue;
				String codeLine="";
				for (Long id:codeSet)
					codeLine+=id+" ";
				codeLine=codeLine.trim();
				node.setProperty(SimilarQuestionTaskConfig.CODES_LINE, codeLine);
			}
			tx.success();
		}
		db.shutdown();
	}

}
