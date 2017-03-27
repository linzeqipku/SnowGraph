package statistics;

import framework.KnowledgeExtractor;
import framework.KnowledgeGraphBuilder;
import org.neo4j.graphdb.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.*;

public class Runner implements KnowledgeExtractor {

    GraphDatabaseService db = null;

    static Set<Node> visited = new HashSet<>();

    public void run(GraphDatabaseService db) {
        this.db = db;

        countFrequency(db);
        countConnected(db);
    }

    public static void countConnected(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            int count = 0;
            ResourceIterable<Node> ite = db.getAllNodes();
            for (Node node : ite) {
                if (visited.contains(node)) continue;
                count++;
                bfs(node);
            }
            System.out.println(count);
            tx.success();
        }
    }

    public static void bfs(Node node) {
        Queue<Node> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            Node front = q.poll();
            visited.add(front);
            for (Relationship relationship : front.getRelationships()) {
                Node startNode = relationship.getStartNode();
                Node endNode = relationship.getEndNode();
                if (!visited.contains(startNode)) visited.add(startNode);
                if (!visited.contains(endNode)) visited.add(endNode);
            }
        }
    }

    public static void countFrequency(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            int total = 0;
            int sum = 0;
            ResourceIterable<Node> ite = db.getAllNodes();
            Map<Integer, Integer> map = new HashMap<>();
            for (Node node : ite) {
                total++;
                int d = node.getDegree();
                sum += d;
                Integer f = map.get(d);
                if (f == null) f = 0;
                map.put(d, f + 1);
            }
            map.forEach((d, f) -> System.out.println(d + ": " + f));
            System.out.println(total);
            System.out.println((double)sum / total);
            tx.success();
        }
    }

    public static void main(String[] args) {
        run("resources/configs/config.xml");
    }

    public static void run(String configPath) {
        @SuppressWarnings("resource")
        ApplicationContext context = new FileSystemXmlApplicationContext(configPath);
        KnowledgeGraphBuilder graphBuilder = (KnowledgeGraphBuilder) context.getBean("graph");
        graphBuilder.buildGraph();
    }
}
