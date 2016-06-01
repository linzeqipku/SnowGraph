package similarquestions.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.medallia.word2vec.Searcher.UnknownWordException;
import com.medallia.word2vec.Word2VecModel;

public class Word2VecDocumentSimilarity {

	Map<String, Double> idfMap=new HashMap<String, Double>();
	Word2VecModel model=null;
	
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
			model=Word2VecModel.fromBinFile(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double sim(List<String> d1, List<String> d2) throws UnknownWordException{
		return sim0(d1, d2)+sim0(d2, d1);
	}
	
	private double sim0(List<String> d1, List<String> d2) throws UnknownWordException{
		double u=0,d=0;
		for (String token:d1){
			u+=idfMap.get(token)*sim0(token, d2);
			d+=idfMap.get(token);
		}
		if (d==0)
			return 0;
		return u/d;
	}
	
	private double sim0(String token, List<String> d2) throws UnknownWordException{
		double r=0;
		for (String token2:d2){
			double sim=model.forSearch().cosineDistance(token, token2);
			if (sim>r)
				r=sim;
		}
		return r;
	}
	
}
