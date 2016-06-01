package similarquestions.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class BM25Similarity {
	
	private static double K1=2;
	private static double B=0.75;
	
	private Map<String, Double> idfMap=new HashMap<String,Double>();
	private double avgDL=0;
	
	public void setIdfMap(Map<String, Double> idfMap){
		this.idfMap=idfMap;
	}
	
	public void setAvgDL(double avgDL){
		this.avgDL=avgDL;
	}

	public double sim(String line1,String line2){
		List<String> tokenList1=new ArrayList<String>();
		double n=0;
		for (String token:line1.split(" "))
			tokenList1.add(token);
		Map<String,Integer> tokenMap2=new HashMap<String,Integer>();
		for (String token:line2.split(" ")){
			if (!tokenMap2.containsKey(token))
				tokenMap2.put(token, 0);
			tokenMap2.put(token, tokenMap2.get(token)+1);
			n++;
		}
		double r=0;
		for (String token:tokenList1){
			if (!tokenMap2.containsKey(token))
				continue;
			double idf=idfMap.get(token);
			double u=(K1+1)*tokenMap2.get(token)/n;
			double d=tokenMap2.get(token)/n+K1*(1.0-B+B*n/avgDL);
			r+=idf*u/d;
		}
		return r;
	}
	
}
