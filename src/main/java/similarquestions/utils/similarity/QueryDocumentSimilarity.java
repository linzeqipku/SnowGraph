package similarquestions.utils.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryDocumentSimilarity {

	Map<String, Double> idfMap=new HashMap<String, Double>();
	
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
	
	public double sim(List<String> d1, List<String> d2){
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
	
	private double sim0(Map<String, Integer> d1, Set<String> d2){
		double u=0,d=0;
		for (String token:d1.keySet()){
			u+=idfMap.get(token)*d1.get(token)*(d2.contains(token)?1:0);
			d+=idfMap.get(token)*d1.get(token);
		}
		if (d==0)
			return 0;
		return u/d;
	}
	
}
