package graphdb.extractors.miners.apiusage.codeslice;

import graphdb.extractors.miners.apiusage.entity.Slice;
import utils.CollectionUtils;

import java.io.Serializable;
import java.util.*;

public class Clusters implements Serializable {
	private static final long serialVersionUID = -2181906811187839349L;
	private final double THRESHOLD = 0.74;
	private List<Slice> slices;
	private List<List<Slice>> clusters;

	public Clusters(List<Slice> _slices) {
		slices = _slices;
		clustering();
	}

	private void clustering() {
		clusters = new ArrayList<>();
		if (slices == null || slices.isEmpty())
			return;

		List<Slice> tempSlices = slices;
		while (tempSlices.size() > 0) {
			// nextFloat在0-1之间
			// 产生一个随机种子
			Random rand = new Random(new Date().getTime());
			int index = (int) (rand.nextFloat() * tempSlices.size());
			if (index >= tempSlices.size())
				index = tempSlices.size() - 1;
			Slice seedSlice = tempSlices.get(index);

			List<Slice> tempCluster = new ArrayList<>();
			tempCluster.add(seedSlice);
			tempSlices.remove(seedSlice);

			// 以下开始比较相似度
			for (int i = 0; i < tempSlices.size(); i++) {
				Slice curSlice = tempSlices.get(i);

				double similarity = similarity(seedSlice, curSlice);

				if (similarity > THRESHOLD) {
					tempCluster.add(curSlice);
					tempSlices.remove(curSlice);
					i--;
				}
			}

			TreeMap<Integer, Double> rankedSimilarities = new TreeMap<>();
			for (int i = 0; i < tempCluster.size(); i++) {
				double sum_similarity = 0;
				for (int j = 0; j < tempCluster.size(); j++) {
					if (j != i)
						sum_similarity += similarity(tempCluster.get(i), tempCluster.get(j));
				}
				double avrg_similarity = sum_similarity / (tempCluster.size() - 1);
				rankedSimilarities.put(i, avrg_similarity);
			}

			List<Map.Entry<Integer, Double>> similarityEntries = new ArrayList<>(
                    rankedSimilarities.entrySet());
			Collections.sort(similarityEntries, new Comparator<Map.Entry<Integer, Double>>() {
				public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
					return (int) (10000 * (o1.getValue() - o2.getValue()));
				}
			});

			List<Slice> cluster = new ArrayList<>();
			for (int i = 0; i < rankedSimilarities.size(); i++) {
				cluster.add(tempCluster.get(i));
			}

			clusters.add(cluster);
		}
	}

	private double similarity(Slice s1, Slice s2) {
		List<String> s1Signature = s1.getInvocationSignatures();
		List<String> s2Signature = s2.getInvocationSignatures();

		// 计算LCS
		List<String> lcs = CollectionUtils.lcs(s1Signature, s2Signature);

		return ((double) 2 * lcs.size()) / (s1Signature.size() + s2Signature.size());
	}

	public List<Slice> getSlices() {
		return slices;
	}

	public void setSlices(List<Slice> slices) {
		this.slices = slices;
	}

	public List<List<Slice>> getClusters() {
		return clusters;
	}

}

