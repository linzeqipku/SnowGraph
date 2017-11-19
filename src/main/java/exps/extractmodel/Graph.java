package exps.extractmodel;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Graph {

	private Map<Long, Vertex> vertexes=new HashMap<>();
	
	public void add(Vertex v){
		vertexes.put(v.id, v);
	}
	
	public Vertex get(long id){
		return vertexes.get(id);
	}
	
	public boolean contains(long id){
		return vertexes.containsKey(id);
	}
	
	public Set<Vertex> getAllVertexes(){
		return new HashSet<>(vertexes.values());
	}
	
	public void remove(Vertex vertex){
		vertexes.remove(vertex.id);
		for (String type:vertex.incomingEdges.keySet())
			for (Vertex vertex2:vertex.incomingEdges.get(type)){
				vertex2.outgoingEdges.get(type).remove(vertex);
				if (vertex2.outgoingEdges.get(type).size()==0)
					vertex2.outgoingEdges.remove(type);
			}
		for (String type:vertex.outgoingEdges.keySet())
			for (Vertex vertex2:vertex.outgoingEdges.get(type)){
				vertex2.incomingEdges.get(type).remove(vertex);
				if (vertex2.incomingEdges.get(type).size()==0)
					vertex2.incomingEdges.remove(type);
			}
		vertex.incomingEdges.clear();
		vertex.outgoingEdges.clear();
	}
	
	public void rename(String oType, String type){
		for (Vertex vertex:vertexes.values()){
			if (vertex.incomingEdges.containsKey(oType)){
				if (!vertex.incomingEdges.containsKey(type))
					vertex.incomingEdges.put(type, new HashSet<>());
				vertex.incomingEdges.get(type).addAll(vertex.incomingEdges.get(oType));
				vertex.incomingEdges.remove(oType);
			}
			if (vertex.outgoingEdges.containsKey(oType)){
				if (!vertex.outgoingEdges.containsKey(type))
					vertex.outgoingEdges.put(type, new HashSet<>());
				vertex.outgoingEdges.get(type).addAll(vertex.outgoingEdges.get(oType));
				vertex.outgoingEdges.remove(oType);
			}
		}
	}
	
	public void addEdge(Vertex v1, Vertex v2, String type){
		if (!v2.incomingEdges.containsKey(type))
			v2.incomingEdges.put(type, new HashSet<>());
		v2.incomingEdges.get(type).add(v1);
		if (!v1.outgoingEdges.containsKey(type))
			v1.outgoingEdges.put(type, new HashSet<>());
		v1.outgoingEdges.get(type).add(v2);
	}
	
	public void removeEdge(Vertex v1, Vertex v2, String type){
		if (v1.outgoingEdges.containsKey(type)){
			v1.outgoingEdges.get(type).remove(v2);
			v2.incomingEdges.get(type).remove(v1);
			if (v1.outgoingEdges.get(type).size()==0)
				v1.outgoingEdges.remove(type);
			if (v2.incomingEdges.get(type).size()==0)
				v2.incomingEdges.remove(type);
		}
	}
	
	public Vertex merge(Vertex v1, Vertex v2){
		Vertex vo=v1,vNew=v2;
		if (vo.name.length()>vNew.name.length()){
			vo=v2;
			vNew=v1;
		}
		for (String type:vNew.outgoingEdges.keySet())
			for (Vertex v3:vNew.outgoingEdges.get(type))
				if (!vo.equals(v3)&&!v3.equals(vNew))
					addEdge(vo, v3, type);
		for (String type:vNew.incomingEdges.keySet())
			for (Vertex v3:vNew.incomingEdges.get(type))
				if (!vo.equals(v3)&&!v3.equals(vNew))
					addEdge(v3, vo, type);
		remove(vNew);
		return vo;
	}
	
	public void merge(Set<Vertex> set){
		if (set.size()<=1)
			return;
		Iterator<Vertex> iter=set.iterator();
		Vertex v0=iter.next();
		while (iter.hasNext())
			v0=merge(v0, iter.next());
	}
	
	public void writeToNeo4j(String dbPath){
		GraphDatabaseService db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
		Map<Vertex, Node> map=new HashMap<>();
		try (Transaction tx=db.beginTx()){
			for (Vertex vertex:vertexes.values()){
				Node node=db.createNode();
				node.addLabel(Label.label("concept"));
				node.setProperty("name", vertex.name);
				map.put(vertex, node);
			}
			for (Vertex vertex:vertexes.values())
				for (String type:vertex.outgoingEdges.keySet())
					for (Vertex vertex2:vertex.outgoingEdges.get(type)){
						map.get(vertex).createRelationshipTo(map.get(vertex2), RelationshipType.withName(type));
					}
			tx.success();
		}
		db.shutdown();
	}
	
	public boolean check(){
		for (Vertex vertex:vertexes.values()){
			for (String key:vertex.outgoingEdges.keySet())
				for (Vertex vertex2:vertex.outgoingEdges.get(key)){
					if (!vertexes.containsValue(vertex2)){
						return false;
					}
					if (!vertex2.incomingEdges.containsKey(key)){
						return false;
					}
					if (!vertex2.incomingEdges.get(key).contains(vertex)){
						return false;
					}
				}
			for (String key:vertex.incomingEdges.keySet())
				for (Vertex vertex2:vertex.incomingEdges.get(key)){
					if (!vertexes.containsValue(vertex2)){
						return false;
					}
					if (!vertex2.outgoingEdges.containsKey(key)){
						return false;
					}
					if (!vertex2.outgoingEdges.get(key).contains(vertex)){
						return false;
					}
				}
		}
		return true;
	}
	
}
