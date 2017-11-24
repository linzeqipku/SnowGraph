package searcher.ir;

import java.util.HashSet;
import java.util.Set;

public class LuceneSearchResult {

	public LuceneSearchResult(long id,String type,String title,String content,double score, String nodeSet){
		this.id=id;
		this.type=type;
		this.title=title;
		this.content=content;
		this.score=score;
		this.nodeSet=new HashSet<>();
		for (String node:nodeSet.trim().split("\\s+"))
			if (node.length()>0)
				this.nodeSet.add(Long.parseLong(node));
	}
	
	public long id;
	public String type;
	public String title;
	public String content;
	private double score;
	public Set<Long> nodeSet;
	
}
