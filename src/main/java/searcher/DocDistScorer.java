package searcher;

import java.util.Set;

import searcher.graph.GraphSearcher;

class DocDistScorer {
	
	private GraphSearcher graphSearcher=null;
	
	public DocDistScorer(GraphSearcher graphSearcher){
		this.graphSearcher=graphSearcher;
	}
	
	public double score(Set<Long> nodeSet1, Set<Long> nodeSet2){
		double r=0;
		double c=0;
		for (long id1:nodeSet1){
			if (!graphSearcher.id2Vec.containsKey(id1))
				continue;
			c++;
			double minDist=Double.MAX_VALUE;
			for (long id2:nodeSet2){
				if (!graphSearcher.id2Vec.containsKey(id2))
					continue;
				double dist=graphSearcher.dist(id1, id2);
				if (dist<minDist)
					minDist=dist;
			}
			if (minDist!=Double.MAX_VALUE)
				r+=minDist;
			else
				return Double.MAX_VALUE;
		}
		if (c==0)
			return Double.MAX_VALUE;
		return r/c;
	}
	
}
