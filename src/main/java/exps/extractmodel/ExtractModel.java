package exps.extractmodel;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.tartarus.snowball.ext.EnglishStemmer;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;

public class ExtractModel {
	
	GraphDatabaseService db=null;
	
	public static void main(String[] args){
		ExtractModel extractModel=new ExtractModel("E://SnowGraphData//lucene//graphdb-copy");
		Graph graph=extractModel.pipeline();
		for (Vertex vertex:graph.getAllVertexes())
			System.out.println(vertex.name);
		graph.writeToNeo4j("E://SnowGraphData//lucene//model");
	}
	
	public ExtractModel(String srcPath){
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(srcPath));
	}
	
	public Graph pipeline(){
		Graph graph=extractProgramAbstract();
		graph=longNameFilter(graph);
		graph=omitMethodsAndFields(graph);
		graph=dealWithInheritance(graph);
		graph=mergeConcepts(graph);
		return graph;
	}
	
	Graph extractProgramAbstract(){
		
		Graph graph=new Graph();
		
		try (Transaction tx=db.beginTx()){
			for (Node node:db.getAllNodes()){
				if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))){
					String name=(String) node.getProperty(JavaCodeExtractor.CLASS_NAME);
					String longName=(String) node.getProperty(JavaCodeExtractor.CLASS_FULLNAME);
					Vertex vertex=new Vertex(node.getId(), name, longName, JavaCodeExtractor.CLASS);
					graph.add(vertex);
				}
				if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
					String name=(String) node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
					String longName=(String) node.getProperty(JavaCodeExtractor.INTERFACE_FULLNAME);
					Vertex vertex=new Vertex(node.getId(), name, longName, JavaCodeExtractor.INTERFACE);
					graph.add(vertex);
				}
				if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD))){
					String name=(String) node.getProperty(JavaCodeExtractor.METHOD_NAME);
					String longName=(String) node.getProperty(JavaCodeExtractor.METHOD_BELONGTO)+"."+name;
					Vertex vertex=new Vertex(node.getId(), name, longName, JavaCodeExtractor.METHOD);
					graph.add(vertex);
				}
				if (node.hasLabel(Label.label(JavaCodeExtractor.FIELD))){
					String name=(String) node.getProperty(JavaCodeExtractor.FIELD_NAME);
					String longName=(String) node.getProperty(JavaCodeExtractor.FIELD_BELONGTO)+"."+name;
					Vertex vertex=new Vertex(node.getId(), name, longName, JavaCodeExtractor.FIELD);
					graph.add(vertex);
				}
			}
			for (Relationship rel:db.getAllRelationships()){
				long srcId=rel.getStartNodeId();
				long dstId=rel.getEndNodeId();
				String type=rel.getType().name();
				if (graph.contains(srcId)&&graph.contains(dstId)){
					graph.addEdge(graph.get(srcId), graph.get(dstId), type);
				}
			}
			tx.success();
		}
		
		return graph;
		
	}
	
	Graph longNameFilter(Graph graph){
		for (Vertex vertex:graph.getAllVertexes()){
			String longName=vertex.longName.toLowerCase();
			if (longName.contains("codec")||longName.contains("util")||longName.contains("test")||longName.contains("exception")
					||longName.contains("comparator")||longName.contains("attribute"))
				graph.remove(vertex);
		}
		return graph;
	}
	
	Graph omitMethodsAndFields(Graph graph){
		for (Vertex vertex:graph.getAllVertexes()){
			if (vertex.labels.contains(JavaCodeExtractor.CLASS)||vertex.labels.contains(JavaCodeExtractor.INTERFACE))
				continue;
			Set<Vertex> calledMethodsAndFields=new HashSet<>();
			if (vertex.outgoingEdges.containsKey(JavaCodeExtractor.CALL_METHOD))
				calledMethodsAndFields.addAll(vertex.outgoingEdges.get(JavaCodeExtractor.CALL_METHOD));
			if (vertex.outgoingEdges.containsKey(JavaCodeExtractor.CALL_FIELD))
				calledMethodsAndFields.addAll(vertex.outgoingEdges.get(JavaCodeExtractor.CALL_FIELD));
			Set<Vertex> calledTypes=new HashSet<>();
			for (Vertex vertex2:calledMethodsAndFields){
				if (vertex2.incomingEdges.containsKey(JavaCodeExtractor.HAVE_METHOD))
					calledTypes.addAll(vertex2.incomingEdges.get(JavaCodeExtractor.HAVE_METHOD));
				if (vertex2.incomingEdges.containsKey(JavaCodeExtractor.HAVE_FIELD))
					calledTypes.addAll(vertex2.incomingEdges.get(JavaCodeExtractor.HAVE_FIELD));
			}
			for (Vertex vertex2:calledTypes)
				graph.addEdge(vertex, vertex2, "rely");
		}
		for (Vertex vertex:graph.getAllVertexes()){
			if (vertex.labels.contains(JavaCodeExtractor.METHOD)||vertex.labels.contains(JavaCodeExtractor.FIELD))
				continue;
			Set<Vertex> fields=new HashSet<>();
			if (vertex.outgoingEdges.containsKey(JavaCodeExtractor.HAVE_FIELD))
				fields.addAll(vertex.outgoingEdges.get(JavaCodeExtractor.HAVE_FIELD));
			Set<Vertex> components=new HashSet<>();
			for (Vertex field:fields)
				if (field.outgoingEdges.containsKey(JavaCodeExtractor.TYPE))
					components.addAll(field.outgoingEdges.get(JavaCodeExtractor.TYPE));
			for (Vertex component:components)
				graph.addEdge(vertex, component, "have");
			Set<Vertex> methods=new HashSet<>();
			if (vertex.outgoingEdges.containsKey(JavaCodeExtractor.HAVE_METHOD))
				methods.addAll(vertex.outgoingEdges.get(JavaCodeExtractor.HAVE_METHOD));
			Set<Vertex> relies=new HashSet<>();
			for (Vertex method:methods){
				if (method.outgoingEdges.containsKey("rely"))
					relies.addAll(method.outgoingEdges.get("rely"));
				if (method.outgoingEdges.containsKey(JavaCodeExtractor.PARAM))
					relies.addAll(method.outgoingEdges.get(JavaCodeExtractor.PARAM));
				if (method.outgoingEdges.containsKey(JavaCodeExtractor.VARIABLE))
					relies.addAll(method.outgoingEdges.get(JavaCodeExtractor.VARIABLE));
			}
			for (Vertex rely:relies)
				if (!vertex.equals(rely)&&!components.contains(rely))
					graph.addEdge(vertex, rely, "rely");
		}
		for (Vertex vertex:graph.getAllVertexes())
			if (vertex.labels.contains(JavaCodeExtractor.METHOD)||vertex.labels.contains(JavaCodeExtractor.FIELD))
				graph.remove(vertex);
		graph.rename(JavaCodeExtractor.EXTEND, "isA");
		graph.rename(JavaCodeExtractor.IMPLEMENT, "isA");
		return graph;
	}
	
	Graph dealWithInheritance(Graph graph){
		//TODO
		return graph;
	}
	
	Graph mergeConcepts(Graph graph){
		Map<String, Set<Vertex>> map=new HashMap<>();
		for (Vertex vertex:graph.getAllVertexes()){
			String name=vertex.name;
			name=name.replaceAll("^\\w+\\.", "");
			name=name.replaceAll("\\d+", "");
			name=name.replaceAll("Iterat[a-z0-9]*", "");
			name=name.replaceAll("Factory((?=[A-Z])|$)", "");
			name=name.replaceAll("All((?=[A-Z])|$)", "");
			name=name.replaceAll("Buffered((?=[A-Z])|$)", "");
			name=name.replaceAll("Config[a-z0-9]*", "");
			name=name.replaceAll("Wrapper((?=[A-Z])|$)", "");
			name=name.replaceAll("Impl((?=[A-Z])|$)", "");
			name=name.replaceAll("Base((?=[A-Z])|$)", "");
			name=name.replaceAll("Set((?=[A-Z])|$)", "");
			name=name.replaceAll("Map((?=[A-Z])|$)", "");
			name=name.replaceAll("Array((?=[A-Z])|$)", "");
			name=name.replaceAll("InputStream((?=[A-Z])|$)", "");
			name=name.replaceAll("OutputStream((?=[A-Z])|$)", "");
			name=name.replaceAll("Simple((?=[A-Z])|$)", "");
			name=name.replaceAll("Default((?=[A-Z])|$)", "");
			name=name.replaceAll("Mock((?=[A-Z])|$)", "");
			name=name.replaceAll("Standard((?=[A-Z])|$)", "");
			name=name.replaceAll("Common((?=[A-Z])|$)", "");
			name=name.replaceAll("Context((?=[A-Z])|$)", "");
			name=name.replaceAll("IO((?=[A-Z])|$)", "");
			name=name.replaceAll("Holder((?=[A-Z])|$)", "");
			EnglishStemmer stemmer=new EnglishStemmer();
			stemmer.setCurrent(name);
			name=stemmer.getCurrent();
			if (name.trim().length()>0){
				if (!map.containsKey(name))
					map.put(name, new HashSet<>());
				map.get(name).add(vertex);
			}
			else
				graph.remove(vertex);
		}
		for (String name:map.keySet()){
			graph.merge(map.get(name));
		}
		return graph;
	}

}
