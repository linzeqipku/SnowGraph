package exps.codepattern.utils;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;
import exps.codepattern.code.cfg.CFG;
import exps.codepattern.code.mining.MiningNode;

import java.io.PrintStream;
import java.util.Iterator;

public class CFGUtil {
    public static void printCFG(CFG cfg) {
        printCFG(cfg, System.out);
    }

    public static void printCFG(CFG cfg, PrintStream ps) {
        cfg.getBlocks().forEach(b ->
            b.getNexts().forEach(n -> ps.println(b + " -> " + n))
        );

        ps.println("-----");

        cfg.getBlocks().forEach(b -> {
            ps.println(b);
            b.getStatements().forEach(ps::println);
            ps.println();
        });
    }

    public static void printGraph(Graph<MiningNode, Integer> g, PrintStream ps) {
        ps.println("-----");
        Iterator<Node<MiningNode, Integer>> nodeIte = g.nodeIterator();
        while (nodeIte.hasNext()) {
            Node<MiningNode, Integer> node = nodeIte.next();
            ps.println(String.format("Node %d: %s", node.getIndex(), node.getLabel()));
        }
        Iterator<Edge<MiningNode, Integer>> ite = g.edgeIterator();
        while (ite.hasNext()) {
            Edge<MiningNode, Integer> edge = ite.next();
            if (edge.getDirection() == Edge.INCOMING) {
                ps.println(edge.getNodeB().getIndex() + " -> " + edge.getNodeA().getIndex());
            } else {
                ps.println(edge.getNodeA().getIndex() + " -> " + edge.getNodeB().getIndex());
            }
        }
    }

}
