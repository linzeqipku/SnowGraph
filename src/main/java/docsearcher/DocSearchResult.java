package docsearcher;

public class DocSearchResult {
	
	long id;
	int irRank,newRank;
	double dist;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getIrRank() {
		return irRank;
	}
	public void setIrRank(int irRank) {
		this.irRank = irRank;
	}
	public int getNewRank() {
		return newRank;
	}
	public void setNewRank(int newRank) {
		this.newRank = newRank;
	}
	public double getDist() {
		return dist;
	}
	public void setDist(double d) {
		this.dist = d;
	}

}
