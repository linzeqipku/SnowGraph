package docsearcher;

import graphsearcher.GraphSearcher;
import graphsearcher.SearchResult;

public class DocDistScorer {
	
	GraphSearcher graphSearcher=null;
	
	public DocDistScorer(GraphSearcher graphSearcher){
		this.graphSearcher=graphSearcher;
	}
	
	public double score(SearchResult graph1, SearchResult graph2){
		double r=0;
		double c=0;
		for (long id1:graph1.nodes){
			if (!graphSearcher.id2Vec.containsKey(id1))
				continue;
			c++;
			double minDist=Double.MAX_VALUE;
			for (long id2:graph2.nodes){
				if (!graphSearcher.id2Vec.containsKey(id2))
					continue;
				double dist=graphSearcher.dist(id1, id2);
				if (dist<minDist)
					minDist=dist;
			}
		}
		return r/c;
	}
	
}
