package similarquestions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import similarquestions.utils.Features;
import similarquestions.utils.SimilarQuestionTaskConfig;

public class P5_Evaluation {

	SimilarQuestionTaskConfig config = null;
	Features features=null;
	
	Set<Long> samples=new HashSet<Long>();
	Set<Long> candidates=new HashSet<Long>();
	
	int K=100;
	DecimalFormat df4 = new DecimalFormat( "0.0000");
	DecimalFormat df2 = new DecimalFormat( "0.00");
	
	public static void main(String[] args){
		P5_Evaluation p=new P5_Evaluation("apache-poi");
		p.run();
	}
	
	public P5_Evaluation(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		File file=new File(config.featuresPath);
		try {
			FileInputStream fis=new FileInputStream(file);
			ObjectInputStream ois=new ObjectInputStream(fis);
			features=(Features)ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Pair<Long, Long> pair:features.standards)
			samples.add(pair.getLeft());
		for (Pair<Long, Long> pair:features.surfaceFeature.keySet())
			candidates.add(pair.getRight());
		filterSamples();
	}

	public void run(){
		double step=0.1;
		Map<Double, Map<Long, List<Pair<Long,Double>>>> m=new HashMap<Double, Map<Long, List<Pair<Long,Double>>>>();
		for (double w=0.0;w<=1.0+step/10;w+=step){
			Map<Long, List<Pair<Long,Double>>> rankMap=rank(w);
			m.put(w, rankMap);
		}
		
		//ROC Value
		System.out.println("ROC="+df4.format(roc(m.get(0.0))));
		
		System.out.println("==========");
		
		//NDCG
		for (double w=0.0;w<=1.0+step/10;w+=step)
			System.out.println("NDCG@"+df2.format(w)+"="+df4.format(ndcg(m.get(w))));
	}
	
	private void filterSamples(){
		Set<Long> r=new HashSet<Long>();
		int k=20;
		Map<Long, List<Pair<Long,Double>>> rankMap=rank(0.3);
		for (long sample:rankMap.keySet()){
			int p=k+1;
			for (int i=k-1;i>=0;i--)
				if (features.standards.contains(new ImmutablePair<Long, Long>(sample,rankMap.get(sample).get(i).getLeft())))
					p=i+1;
			if (p<k+1)
				r.add(sample);
		}
		samples=r;
		System.out.println("|Samples|="+samples.size());
	}
	
	double ndcg(Map<Long, List<Pair<Long,Double>>> rankMap){
		int k=10;
		Map<Long, Double> scoreMap=new HashMap<Long, Double>();
		for (long sample:rankMap.keySet()){
			int p=k+1;
			for (int i=k-1;i>=0;i--)
				if (features.standards.contains(new ImmutablePair<Long, Long>(sample,rankMap.get(sample).get(i).getLeft())))
					p=i+1;
			scoreMap.put(sample, Math.log(2)/Math.log(p+1));
		}
		double r=0;
		for (long sample:rankMap.keySet())
			r+=scoreMap.get(sample);
		return r/rankMap.size();
	}
	
	double roc(Map<Long, List<Pair<Long,Double>>> rankMap){
		double r=0;
		double fpr0=falsePositiveRate(rankMap, -0.05);
		double tpr0=truePositiveRate(rankMap, -0.05);
		r+=fpr0*tpr0/2;
		for (double t=0;t<=1.001;t+=0.05){
			double fpr=falsePositiveRate(rankMap, t);
			double tpr=truePositiveRate(rankMap, t);
			r+=(fpr-fpr0)*tpr0+(fpr-fpr0)*(tpr-tpr0)/2;
			fpr0=fpr;
			tpr0=tpr;
		}
		return r;
	}
	
	double falsePositiveRate(Map<Long, List<Pair<Long,Double>>> rankMap, double thresholds){
		double r=0,c=0;
		for (long sample:rankMap.keySet()){
			List<Pair<Long,Double>> list=rankMap.get(sample);
			for (int i=0;i<list.size();i++){
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, list.get(i).getKey());
				boolean yes=features.standards.contains(pair);
				if (yes){
					c++;
					if (list.get(i).getValue()<=thresholds)
						r++;
				}
			}
		}
		if (c>0)
			r/=c;
		return r;
	}
	
	double truePositiveRate(Map<Long, List<Pair<Long,Double>>> rankMap, double thresholds){
		double r=0,c=0;
		for (long sample:rankMap.keySet()){
			List<Pair<Long,Double>> list=rankMap.get(sample);
			for (int i=0;i<list.size();i++){
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, list.get(i).getKey());
				boolean yes=features.standards.contains(pair);
				if (!yes){
					c++;
					if (list.get(i).getValue()<=thresholds)
						r++;
				}
			}
		}
		if (c>0)
			r/=c;
		return r;
	}
	
	private Map<Long, List<Pair<Long, Double>>> rank(double cWeight){
		Map<Long, List<Pair<Long,Double>>> r=new HashMap<Long, List<Pair<Long,Double>>>();
		for (long sample:samples){
			List<Pair<Long,Double>> list=new ArrayList<Pair<Long,Double>>();
			for (long candidate:candidates){
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, candidate);
				if (sample==candidate)
					continue;
				double v=(1.0-cWeight)*features.surfaceFeature.get(pair);
				v+=cWeight*features.code2vecFeature.get(pair);
				list.add(new ImmutablePair<Long, Double>(candidate, v));
			}
			Collections.sort(list, new Comparator<Pair<Long,Double>>() {
				public int compare(Pair<Long,Double> o1, Pair<Long,Double> o2) {
					if (o1.getValue().equals(o2.getValue()))
						return 0;
					return o2.getValue()-o1.getValue()<0?-1:1;
				}
			});
			r.put(sample, list);
		}
		return r;
	}
	
}
