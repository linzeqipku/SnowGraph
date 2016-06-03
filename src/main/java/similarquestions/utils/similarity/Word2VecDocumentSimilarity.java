package similarquestions.utils.similarity;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Searcher.UnknownWordException;
import com.medallia.word2vec.Word2VecModel;

public class Word2VecDocumentSimilarity implements Serializable{

	Map<Long, List<String>> documents=null;
	
	Map<String, Double> idfMap=new HashMap<String, Double>();
	String word2VecModelPath="";
	Searcher searcher=null;
	Map<String, Map<Long, Double>> simMap=new HashMap<String, Map<Long, Double>>();
	
	public Word2VecDocumentSimilarity(Map<Long, List<String>> documents, String word2VecModelPath){
		this.word2VecModelPath=word2VecModelPath;
		setWord2VecModel(word2VecModelPath);
		setIdfMap(documents.values());
		setSimMap(documents);
	}
	
	public double sim(long docId1, long docId2){
		return 0.5*(sim0(docId1, docId2)+sim0(docId2, docId1));
	}
	
	private void setIdfMap(Collection<List<String>> corpus){
		idfMap.clear();
		Map<String, Integer> countMap=new HashMap<String, Integer>();
		for (List<String> doc:corpus){
			Set<String> tokenSet=new HashSet<String>();
			tokenSet.addAll(doc);
			for (String token:tokenSet)
				if (!countMap.containsKey(token))
					countMap.put(token, 1);
				else
					countMap.put(token, countMap.get(token)+1);
		}
		for (String token:countMap.keySet())
			idfMap.put(token, Math.log(((double)corpus.size()))/countMap.get(token));
	}
	
	private void setSimMap(Map<Long, List<String>> documents){
		Map<String, Map<String, Double>> cacheMap=new HashMap<String, Map<String, Double>>();
		this.documents=documents;
		int c=0;
		for (String token:idfMap.keySet()){
			if (c%100==0)
				System.out.println("文本相似度模型预处理:"+(100.0*c/idfMap.size())+"%"+" "+new Date());
			simMap.put(token, new HashMap<Long,Double>());
			for (Long id:documents.keySet()){
				double v=0;
				try {
					v=sim0(token, new HashSet<String>(documents.get(id)),cacheMap);
				} catch (UnknownWordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				simMap.get(token).put(id, v);
			}
			c++;
		}
		System.out.println("文本相似度模型预处理完成.");
	}
	
	private void setWord2VecModel(String path){
		try {
			searcher=Word2VecModel.fromBinFile(new File(path)).forSearch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double sim0(long docId1, long docId2){
		double u=0,d=0;
		for (String token:documents.get(docId1)){
			u+=idfMap.get(token)*simMap.get(token).get(docId2);
			d+=idfMap.get(token);
		}
		if (d==0)
			return 0;
		return u/d;
	}
	
	private double sim0(String token, Set<String> d2,Map<String, Map<String, Double>> cacheMap) throws UnknownWordException{
		double r=0;
		for (String token2:d2){
			String k1=token,k2=token2;
			if (k1.compareTo(k2)>0){
				k1=token2;
				k2=token;
			}
			double v=0;
			if (cacheMap.containsKey(k1)&&cacheMap.get(k1).containsKey(k2))
				v=cacheMap.get(k1).get(k2);
			else {
				v=searcher.cosineDistance(k1, k2);
				if (!cacheMap.containsKey(k1))
					cacheMap.put(k1, new HashMap<String, Double>());
				cacheMap.get(k1).put(k2, v);
			}
			double sim=0.5+0.5*v;
			if (sim>r)
				r=sim;
		}
		return r;
	}
	
	public void writeObject(ObjectOutputStream out) throws IOException{
		out.writeObject(documents);
		out.writeObject(idfMap);
		out.writeObject(simMap);
	}
	
	public Word2VecDocumentSimilarity(ObjectInputStream in){
		try {
			documents=(Map<Long, List<String>>) in.readObject();
			idfMap=(Map<String, Double>) in.readObject();
			simMap=(Map<String, Map<Long, Double>>) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
