package similarquestions;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import similarquestions.utils.SimilarQuestionTaskConfig;

public class T7_Evaluation {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	int N=30;
	Map<Long, boolean[]> map0=new HashMap<Long, boolean[]>();
	Map<Long, boolean[]> map1=new HashMap<Long, boolean[]>();
	DecimalFormat df = new DecimalFormat( "0.0000");
	
	public static void main(String[] args){
		T7_Evaluation p=new T7_Evaluation("apache-poi");
		p.run();
	}
	
	public T7_Evaluation(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		getMap();
		
		//MRR
		System.out.println();
		System.out.println("MRR Metric:");
		mrr();
		
		//NDCG@K
		System.out.println();
		System.out.println("NDCG@K Metrics:");
		ndcg(5);
		ndcg(10);
		ndcg(20);
		ndcg(30);
		
		//Hit@K
		System.out.println();
		System.out.println("Hit@K Metrics:");
		hitAt(5);
		hitAt(10);
		hitAt(20);
		hitAt(30);
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
		System.out.println("MRR:"+df.format(mrr0)+"-->"+df.format(mrr1));
	}
	
	private void ndcg(int K){
		if (K>N)
			return;
		double r0=0,r1=0;
		for (Long id:map0.keySet()){
			boolean[] list0=map0.get(id);
			boolean[] list1=map1.get(id);
			double maxDCG=0,nDCG0=0,nDCG1=0;
			for (int i=1;i<=K;i++){
				double discount=1.0/Math.log(1+i);
				maxDCG+=discount;
				nDCG0+=discount*(list0[i]?1:0);
				nDCG1+=discount*(list1[i]?1:0);
			}
			r0+=nDCG0/maxDCG;
			r1+=nDCG1/=maxDCG;
		}
		r0/=map0.size();
		r1/=map0.size();
		System.out.println("NDCG@"+K+":"+df.format(r0)+"-->"+df.format(r1));
	}
	
	private void hitAt(int K){
		if (K>N)
			return;
		double c0=0,c1=0;
		for (Long id:map0.keySet()){
			boolean[] list0=map0.get(id);
			boolean[] list1=map1.get(id);
			boolean h0=false,h1=false;
			for (int i=1;i<=K;i++){
				h0=h0|list0[i];
				h1=h1|list1[i];
			}
			c0+=h0?1:0;
			c1+=h1?1:0;
		}
		c0/=map0.size();
		c1/=map0.size();
		System.out.println("Hit@"+K+":"+df.format(c0)+"-->"+df.format(c1));
	}
	
}
