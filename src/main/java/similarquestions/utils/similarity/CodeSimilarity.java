package similarquestions.utils.similarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeSimilarity {

	Map<String, Double> idfMap=new HashMap<String, Double>();
	Map<String, Double[]> vecMap=new HashMap<String, Double[]>();
	
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
	
	public void setVecMap(Map<String, Double[]> vecMap){
		this.vecMap.clear();
		this.vecMap.putAll(vecMap);
	}
	
	public double sim(List<String> d1, List<String> d2){
		return 0.5*(sim0(d1, d2)+sim0(d2, d1));
	}
	
	private double sim0(List<String> d1, List<String> d2){
		double u=0,d=0;
		for (String token:d1){
			u+=idfMap.get(token)*sim0(token, d2);
			d+=idfMap.get(token);
		}
		if (d==0)
			return 0;
		return u/d;
	}
	
	private double sim0(String token, List<String> d2){
		double r=0;
		for (String token2:d2){
			double sim=sim0(token, token2);
			if (sim>r)
				r=sim;
		}
		return r;
	}
	
	private double sim0(String token1, String token2){
		double r=0;
		Double[] vec1=vecMap.get(token1);
		Double[] vec2=vecMap.get(token2);
		if (vec1==null||vec2==null||vec1.length!=vec2.length)
			return 0;
		for (int i=0;i<vec1.length;i++)
			r+=Math.abs(vec1[i]-vec2[i]);
		return 1-0.5*r;
	}
	
}
