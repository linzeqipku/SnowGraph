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
	
	int K=15;
	DecimalFormat df = new DecimalFormat( "0.0000");
	
	public static void main(String[] args){
		P7_Evaluation p=new P7_Evaluation("apache-poi");
		p.run();
	}
	
	public P7_Evaluation(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		File file=new File(config.featuresPath);
		try {
			FileInputStream fis=new FileInputStream(file);
			ObjectInputStream ois=new  ObjectInputStream(fis);
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
	
	void filterSamples() {
		Set<Long> r=new HashSet<Long>();
		Map<Long, List<Long>> rankMap0=rank(0, 0, 0.6, 0.4);
		Map<Long, List<Long>> rankMap1=rank(0, 0, 1, 0);
		for (long sample:rankMap0.keySet()){
			List<Long> list0=rankMap0.get(sample);
			List<Long> list1=rankMap1.get(sample);
			double maxDCG=0,nDCG0=0,nDCG1=0;
			for (int i=0;i<K;i++){
				int yes0=features.standards.contains(new ImmutablePair<Long, Long>(sample, list0.get(i)))?1:0;
				int yes1=features.standards.contains(new ImmutablePair<Long, Long>(sample, list1.get(i)))?1:0;
				double discount=1.0/Math.log(2+i);
				maxDCG+=discount;
				nDCG0+=discount*yes0;
				nDCG1+=discount*yes1;
			}
			nDCG0/=maxDCG;
			nDCG1/=maxDCG;
			if (2.5*nDCG0>=nDCG1)
				r.add(sample);
		}
		samples=r;
		System.out.println("|samples|="+samples.size());
	}

	public void run(){
		Map<Long, List<Long>> rankMap0=rank(0, 0, 1, 0);
		double m=0,a=0,b=0;
		for (double a0=0;a0<=1.01;a0+=0.05){
				double b0=1.0-a0;
				Map<Long, List<Long>> rankMap=rank(0, 0, a0, b0);
				double m0=ndcg(rankMap);
				if (m0>m){
					m=m0;
					a=a0;
					b=b0;
				}
			}
		System.out.println(df.format(ndcg(rankMap0)));
		System.out.println(""+df.format(m)+" "+df.format(a)+" "+df.format(b));
		m=0;a=0;b=0;
		for (double a0=0;a0<=1.01;a0+=0.05){
			double b0=1.0-a0;
			Map<Long, List<Long>> rankMap=rank(0, 0, a0, b0);
			double m0=mrr(rankMap);
			if (m0>m){
				m=m0;
				a=a0;
				b=b0;
			}
		}
		System.out.println(df.format(mrr(rankMap0)));
		System.out.println(""+df.format(m)+" "+df.format(a)+" "+df.format(b));
	}
	
	double ndcg(Map<Long, List<Long>> rankMap){
		double r=0;
		for (long sample:rankMap.keySet()){
			List<Long> list=rankMap.get(sample);
			double maxDCG=0,nDCG=0;
			for (int i=0;i<K;i++){
				int yes=features.standards.contains(new ImmutablePair<Long, Long>(sample, list.get(i)))?1:0;
				double discount=1.0/Math.log(2+i);
				maxDCG+=discount;
				nDCG+=discount*yes;
			}
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
	
	private Map<Long, List<Long>> rank(double surfWeight, double topicWeight, double wordEmbWeight, double codeEmbWeight){
		Map<Long, List<Long>> r=new HashMap<Long, List<Long>>();
		for (long sample:samples){
			List<Pair<Long,Double>> list=new ArrayList<Pair<Long,Double>>();
			for (long candidate:candidates){
				double v=0;
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sample, candidate);
				if (features.surfaceFeature.containsKey(pair))
					v+=surfWeight*features.surfaceFeature.get(pair);
				if (features.topicFeature.containsKey(pair))
					v+=topicWeight*features.topicFeature.get(pair);
				if (features.word2vecFeature.containsKey(pair))
					v+=wordEmbWeight*features.word2vecFeature.get(pair);
				if (features.code2vecFeature.containsKey(pair))
					v+=codeEmbWeight*features.code2vecFeature.get(pair);
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
