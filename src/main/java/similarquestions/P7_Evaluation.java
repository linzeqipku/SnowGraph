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

public class P7_Evaluation {

	SimilarQuestionTaskConfig config = null;
	Features features=null;
	
	Set<Long> samples=new HashSet<Long>();
	Set<Long> candidates=new HashSet<Long>();
	
	int K=100;
	DecimalFormat df4 = new DecimalFormat( "0.0000");
	DecimalFormat df2 = new DecimalFormat( "0.00");
	
	public static void main(String[] args){
		P7_Evaluation p=new P7_Evaluation("apache-poi");
		p.run();
	}
	
	public P7_Evaluation(String projectName){
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
	
	private void filterSamples(){
		Set<Long> r=new HashSet<Long>();
		Map<Long, List<Long>> rankMap0=rank(-1);
		for (long sample:samples){
			List<Long> list0=rankMap0.get(sample);
			double cu=0,cd=0;
			for (int i=0;i<list0.size();i++){
				if (i>=K)
					break;
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, list0.get(i));
				boolean yes=features.standards.contains(pair);
				if (yes){
					cd++;
					if (features.code2vecFeature.containsKey(pair))
						cu+=features.code2vecFeature.get(pair);
				}
			}
			double c=2;
			if (cd>0)
				c=cu/cd;
			if (c>0.1)
				r.add(sample);
		}
		samples=r;
		System.out.println("|samples|="+samples.size());
	}

	public void run(){
		Map<Long, List<Long>> rankMap0=rank(0);
		for (double t=-0.05;t<=1.001;t+=0.05){
			System.out.print("thredsholds="+df2.format(t));
			System.out.print(" Prec="+df4.format(prec(rankMap0, t)));
			System.out.print(" FPR="+df4.format(falsePositiveRate(rankMap0, t)));
			System.out.println(" TPR="+df4.format(truePositiveRate(rankMap0, t)));
		}
		System.out.println("==========");
		for (double w=0.0;w<=1.001;w+=0.05){
			Map<Long, List<Long>> rankMap=rank(w);
			System.out.print("cWeight="+df2.format(w));
			System.out.print(" NDCG="+df4.format(ndcg(rankMap)));
			System.out.println(" MRR="+df4.format(mrr(rankMap)));
		}
	}
	
	double ndcg(Map<Long, List<Long>> rankMap){
		double r=0;
		for (long sample:rankMap.keySet()){
			List<Long> list=rankMap.get(sample);
			double maxDCG=0,nDCG=0;
			for (int i=0;i<10;i++){
				if (i>=list.size())
					break;
				int yes=features.standards.contains(new ImmutablePair<Long, Long>(sample, list.get(i)))?1:0;
				double discount=1.0/Math.log(2+i);
				maxDCG+=discount;
				nDCG+=discount*yes;
			}
			if (maxDCG>0)
				r+=nDCG/maxDCG;
		}
		r/=rankMap.size();
		return r;
	}
	
	double mrr(Map<Long, List<Long>> rankMap){
		double r=0;
		for (long sample:rankMap.keySet()){
			List<Long> list=rankMap.get(sample);
			for (int i=0;i<list.size();i++){
				boolean yes=features.standards.contains(new ImmutablePair<Long, Long>(sample, list.get(i)));
				if (yes){
					r+=1.0/(1.0+i);
					break;
				}
			}
		}
		r/=rankMap.size();
		return r;
	}
	
	double prec(Map<Long, List<Long>> rankMap0, double thresholds){
		double c=0,d=0;
		for (long sample:rankMap0.keySet()){
			List<Long> list0=rankMap0.get(sample);
			for (int i=0;i<K;i++){
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, list0.get(i));
				boolean yes=features.standards.contains(pair);
				if (!(features.code2vecFeature.containsKey(pair)&&features.code2vecFeature.get(pair)>thresholds))
					continue;
				if (yes)
					c++;
				d++;
			}
		}
		if (d==0)
			return 0;
		return c/d;
	}
	
	double falsePositiveRate(Map<Long, List<Long>> rankMap0, double thresholds){
		double r=0,c=0;
		for (long sample:rankMap0.keySet()){
			List<Long> list0=rankMap0.get(sample);
			for (int i=0;i<list0.size();i++){
				if (i>=K)
					break;
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, list0.get(i));
				boolean yes=features.standards.contains(pair);
				if (yes){
					c++;
					if (!(features.code2vecFeature.containsKey(pair)&&features.code2vecFeature.get(pair)>thresholds))
						r++;
				}
			}
		}
		if (c>0)
			r/=c;
		return r;
	}
	
	double truePositiveRate(Map<Long, List<Long>> rankMap0, double thresholds){
		double r=0,c=0;
		for (long sample:rankMap0.keySet()){
			List<Long> list0=rankMap0.get(sample);
			for (int i=0;i<list0.size();i++){
				if (i>=K)
					break;
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, list0.get(i));
				boolean yes=features.standards.contains(pair);
				if (!yes){
					c++;
					if (!(features.code2vecFeature.containsKey(pair)&&features.code2vecFeature.get(pair)>thresholds))
						r++;
				}
			}
		}
		if (c>0)
			r/=c;
		return r;
	}
	
	private Map<Long, List<Long>> rank(double cWeight){
		Map<Long, List<Long>> r=new HashMap<Long, List<Long>>();
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
			List<Long> rList=new ArrayList<Long>();
			for (Pair<Long,Double> pair:list)
				rList.add(pair.getKey());
			r.put(sample, rList);
		}
		return r;
	}
	
}
