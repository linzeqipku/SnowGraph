package extractors.miners.mailqa.entity;

import java.util.ArrayList;

public class Content {

	private ArrayList<Segment>	segments;

	public ArrayList<Segment> getSegments() {
		return segments;
	}

	public void setSegments(ArrayList<Segment> segments) {
		this.segments = segments;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("content segments:" + "\n");
		for (Segment s : segments) {
			sb.append(s.toString() + "\n");
		}
		return sb.toString();
	}
}
