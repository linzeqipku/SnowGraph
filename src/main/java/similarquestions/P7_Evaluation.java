package similarquestions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.relation.Relation;

import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;
import similarquestions.utils.SimilarQuestionTaskConfig;
import similarquestions.utils.similarity.CodeSimilarity;
import similarquestions.utils.similarity.QueryDocumentSimilarity;
import similarquestions.utils.similarity.Word2VecDocumentSimilarity;

public class P7_Evaluation {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	int N=30;
	Map<Long, boolean[]> map0=new HashMap<Long, boolean[]>();
	Map<Long, boolean[]> map1=new HashMap<Long, boolean[]>();
	
	public static void main(String[] args){
		P7_Evaluation p=new P7_Evaluation("apache-poi");
		p.run();
	}
	
	public P7_Evaluation(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		getMap();
		mrr();
	}
	
	private void getMap(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Relationship> rels=db.getAllRelationships().iterator();
			while (rels.hasNext()){
				Relationship rel=rels.next();
				if (rel.hasProperty(SimilarQuestionTaskConfig.MARK)&&((int)rel.getProperty(SimilarQuestionTaskConfig.MARK))==1){
					Long id1=rel.getStartNode().getId();
					if (!map0.containsKey(id1)){
						map0.put(id1, new boolean[N+1]);
						map1.put(id1, new boolean[N+1]);
						for (int i=0;i<=N;i++){
							map0.get(id1)[i]=false;
							map1.get(id1)[i]=false;
						}
					}
					int rank0=(int) rel.getProperty(SimilarQuestionTaskConfig.RANK_0);
					int rank1=(int) rel.getProperty(SimilarQuestionTaskConfig.RANK_1);
					if (rank0<=N)
						map0.get(id1)[rank0]=true;
					if (rank1<=N)
						map1.get(id1)[rank1]=true;
				}
			}
			tx.success();
		}
		System.out.println("共标注了"+map0.size()+"个问题样本.");
	}
	
	private void mrr(){
		double mrr0=0,mrr1=0;
		for (Long id:map0.keySet()){
			boolean[] list0=map0.get(id);
			boolean[] list1=map1.get(id);
			int h0=N+1,h1=N+1;
			for (int i=N;i>=1;i--){
				if (list0[i])
					h0=i;
				if (list1[i])
					h1=i;
			}
			if (h0==N+1)
				mrr0+=0;
			else
				mrr0+=1.0/h0;
			if (h1==N+1)
				mrr1+=0;
			else
				mrr1+=1.0/h1;
		}
		mrr0/=map0.size();
		mrr1/=map0.size();
		System.out.println("MRR0="+mrr0+"    MRR1="+mrr1);
	}
	
}
