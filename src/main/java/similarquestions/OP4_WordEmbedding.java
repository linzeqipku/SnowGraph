package similarquestions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.Word2VecTrainerBuilder.TrainingProgressListener;
import com.medallia.word2vec.neuralnetwork.NeuralNetworkType;
import com.medallia.word2vec.util.Format;

import similarquestions.utils.SimilarQuestionTaskConfig;

public class OP4_WordEmbedding {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	public static void main(String[] args){
		OP4_WordEmbedding p=new OP4_WordEmbedding("apache-poi");
		try {
			p.run();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public OP4_WordEmbedding(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run() throws InterruptedException, IOException{
		
		List<List<String>> partitioned=getPartitioned();
		
		Word2VecModel model = Word2VecModel.trainer()
				.setMinVocabFrequency(1)
				.useNumThreads(20)
				.setWindowSize(8)
				.type(NeuralNetworkType.SKIP_GRAM)
				.setLayerSize(200)
				.useNegativeSamples(25)
				.setDownSamplingRate(1e-4)
				.setNumIterations(10)
				.setListener(new TrainingProgressListener() {
					@Override public void update(Stage stage, double progress) {
						System.out.println(String.format("%s is %.2f%% complete", Format.formatEnum(stage), progress * 100));
					}
				})
				.train(partitioned);
		FileOutputStream fo=new FileOutputStream(new File(config.word2vecPath));
		model.toBinFile(fo);
		
	}
	
	private List<List<String>> getPartitioned(){
		List<List<String>> r=new ArrayList<List<String>>();
		try (Transaction tx=db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasProperty(SimilarQuestionTaskConfig.TOKENS_LINE))
					continue;
				List<String> tokenList=new ArrayList<String>();
				for (String token:((String)node.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split("\\s+"))
					tokenList.add(token);
				r.add(tokenList);
			}
			tx.success();
		}
		return r;
	}
	
}
