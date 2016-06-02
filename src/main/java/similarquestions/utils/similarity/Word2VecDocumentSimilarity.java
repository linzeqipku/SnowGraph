package similarquestions.utils.similarity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Searcher.UnknownWordException;
import com.medallia.word2vec.Word2VecModel;

public class Word2VecDocumentSimilarity {

	Map<String, Double> idfMap=new HashMap<String, Double>();
	Searcher searcher=null;
	Map<Pair<String, String>, Double> cacheMap=new HashMap<Pair<String, String>, Double>();
	
	public void setIdfMap(List<List<String>> corpus){
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
	
	public void setWord2VecModel(String path){
		try {
			searcher=Word2VecModel.fromBinFile(new File(path)).forSearch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double sim(List<String> d1, List<String> d2) throws UnknownWordException{
		cacheMap.clear();
		Map<String, Integer> countMap1=new HashMap<String, Integer>();
		for (String token:d1)
			if (countMap1.containsKey(token))
				countMap1.put(token, countMap1.get(token)+1);
			else
				countMap1.put(token,1);
		Map<String, Integer> countMap2=new HashMap<String, Integer>();
		for (String token:d2)
			if (countMap2.containsKey(token))
				countMap2.put(token, countMap2.get(token)+1);
			else
				countMap2.put(token,1);
		return 0.5*(sim0(countMap1, new HashSet<String>(d2))+sim0(countMap2, new HashSet<String>(d1)));
	}
	
	private double sim0(Map<String, Integer> d1, Set<String> d2) throws UnknownWordException{
		double u=0,d=0;
		for (String token:d1.keySet()){
			u+=idfMap.get(token)*sim0(token, d2)*d1.get(token);
			d+=idfMap.get(token)*d1.get(token);
		}
		if (d==0)
			return 0;
		return u/d;
	}
	
	private double sim0(String token, Set<String> d2) throws UnknownWordException{
		double r=0;
		for (String token2:d2){
			Pair<String,String> pair=new ImmutablePair<String, String>(token, token2);
			double v=0;
			if (cacheMap.containsKey(pair))
				v=cacheMap.get(pair);
			else {
				v=searcher.cosineDistance(token, token2);
				cacheMap.put(pair, v);
			}
			double sim=0.5+0.5*v;
			if (sim>r)
				r=sim;
		}
		return r;
	}
	
}
