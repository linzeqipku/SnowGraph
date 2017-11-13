package exps.extractmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vertex {
	
	long id;
	String name;
	String longName;
	Set<String> labels=new HashSet<>();
	Map<String, Set<Vertex>> incomingEdges=new HashMap<>();
	Map<String, Set<Vertex>> outgoingEdges=new HashMap<>();
	
	public Vertex(long id, String name, String longName, String label){
		this.id=id;
		this.name=name;
		this.longName=longName;
		this.labels.add(label);
	}
	
	@Override
	public boolean equals(Object v){
		if (v instanceof Vertex)
			return id==((Vertex)v).id;
		return false;
	}
	
}
