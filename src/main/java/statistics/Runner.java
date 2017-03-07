package statistics;

import framework.KnowledgeExtractor;
import framework.KnowledgeGraphBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class Runner implements KnowledgeExtractor {

    GraphDatabaseService db = null;

    public void run(GraphDatabaseService db) {
        this.db = db;

        try (Transaction tx = db.beginTx()) {
            ResourceIterable<Node> ite = db.getAllNodes();
            Map<Integer, Integer> map = new HashMap<>();
            for (Node node : ite) {
                int d = node.getDegree();
                Integer times = map.get(d);
                if (times == null) times = 0;
                map.put(d, times + 1);
            }
            map.forEach((d, t) -> System.out.println(d + ": " + t));
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
