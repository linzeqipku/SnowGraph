package utils.mywork;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.framework.Extractor;
import org.neo4j.graphdb.*;

import java.io.*;
import java.util.*;

/**
 * Created by laurence on 17-7-8.
 */
public class GraphToTextUtil implements Extractor {
    GraphDatabaseService db;
    HashMap<Integer, double[]> embedding;
    public void run(GraphDatabaseService graphDB){
        this.db = graphDB;
        loadEmbedding();
        show();
    }
    public void show(){
        Scanner scanner = new Scanner(System.in);
        String name;
        while(scanner.hasNext()){
            name = scanner.next();
            long id = getIdByName(name);
            System.out.println("the id is " + id);
            findNearest((int)id);
        }
    }
    public void writeGraph(){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream("/home/laurence/Desktop/working/graph"));
        } catch (IOException e){
            e.printStackTrace();
        }
        int count = 0;
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while (nodeIter.hasNext()) {
                Node start = nodeIter.next();
                count++;
                for (Relationship relationship : start.getRelationships(Direction.OUTGOING)) {
                    Node end = relationship.getEndNode();
                    writer.write(start.getId() + " " + end.getId() + " " + 1 + "\n");
                }
            }
            tx.success();
        }
        writer.close();
        System.out.println(count);
    }
    public void loadEmbedding(){
        embedding = new HashMap<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream("/home/laurence/Desktop/graph_embedding"));
        }catch (IOException e){
            e.printStackTrace();
        }
        int m = scanner.nextInt();
        int n = scanner.nextInt();
        for (int i = 0; i < m; ++i){
            int key = scanner.nextInt();
            double[] vector = new double[n];
            for (int j = 0; j < n; ++j)
                vector[j] = scanner.nextDouble();
            embedding.put(key, vector);
        }
        scanner.close();
    }
    public double calDist(int id1, int id2){
        double[] vec1 = embedding.get(id1);
        double[] vec2 = embedding.get(id2);
        double dist = 0;
        for (int i = 0; i < vec1.length; ++i){
            dist += (vec1[i] - vec2[i])*(vec1[i] - vec2[i]);
        }
        dist = Math.sqrt(dist);
        return dist;
    }
    public void findNearest(int id){
        ArrayList<Pair> distPair = new ArrayList<>(embedding.size()/2);
        for (int key : embedding.keySet()){
            //System.out.println(key + " " + calDist(id, key));
            distPair.add(new Pair(key, calDist(id, key)));
        }
        Collections.sort(distPair);
        for (int k = 1; k <= 20; ++k){
            int key = distPair.get(k).key;
            String name = getNameById(key);
            System.out.println( name + "(" + key + ") " + distPair.get(k).dist);
        }
    }
    public String getNameById(int id){
        try (Transaction tx = db.beginTx()) {
            Node node = db.getNodeById(id);
            if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))){
                return (String)node.getProperty(JavaCodeExtractor.CLASS_FULLNAME);
            }
            else if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                return (String)node.getProperty(JavaCodeExtractor.INTERFACE_FULLNAME);
            }
            else if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD))){
                return (String)node.getProperty(JavaCodeExtractor.METHOD_NAME);
            }
            else if (node.hasLabel(Label.label(JavaCodeExtractor.FIELD))){
                return (String)node.getProperty(JavaCodeExtractor.FIELD_NAME);
            }
            tx.success();
            return null;
        }
    }
    public long getIdByName(String name){
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while(nodeIter.hasNext()){
                Node node = nodeIter.next();
                if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))){
                    if (((String)node.getProperty(JavaCodeExtractor.CLASS_NAME)).equals(name)){
                        return node.getId();
                    }
                } else if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
                    if (name.equals((String)node.getProperty(JavaCodeExtractor.INTERFACE_NAME))){
                        return node.getId();
                    }
                }
            }
            tx.success();
            return 0;
        }
    }
}

class Pair implements Comparable<Pair>{
    public int key;
    public double dist;
    public Pair(int k, double d){
        key = k;
        dist = d;
    }
    public int compareTo(Pair p){
        return Double.compare(this.dist, p.dist);
    }
}
