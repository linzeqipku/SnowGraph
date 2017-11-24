package graphdb.extractors.linkers.apimention;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

public class CodeIndexes {

    public Map<String, Long> typeMap = new HashMap<>();
    private Map<Long, String> idToTypeNameMap = new HashMap<>();
    private Map<String, Set<Long>> methodMap = new HashMap<>();
    private Map<Long, String> idToMethodNameMap = new HashMap<>();
    public Map<String, Set<Long>> typeShortNameMap = new HashMap<>();
    private Map<String, Set<Long>> methodMidNameMap = new HashMap<>();
    public Map<String, Set<Long>> methodShortNameMap = new HashMap<>();

    public CodeIndexes(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodes = db.getAllNodes().iterator();
            Set<Node> codeNodes = new HashSet<>();
            while (nodes.hasNext()) {
                Node node = nodes.next();
                if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS)) || node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)) || node.hasLabel(Label.label(JavaCodeExtractor.METHOD))) {
                    codeNodes.add(node);
                }
            }

            for (Node codeNode : codeNodes) {
                String name = "";
                boolean type = true;
                if (codeNode.hasLabel(Label.label(JavaCodeExtractor.CLASS)))
                    name = (String) codeNode.getProperty(JavaCodeExtractor.CLASS_FULLNAME);
                if (codeNode.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
                    name = (String) codeNode.getProperty(JavaCodeExtractor.INTERFACE_FULLNAME);
                if (codeNode.hasLabel(Label.label(JavaCodeExtractor.METHOD))) {
                    name = codeNode.getProperty(JavaCodeExtractor.METHOD_BELONGTO) + "." + codeNode.getProperty(JavaCodeExtractor.METHOD_NAME);
                    type = false;
                }
                if (name.contains("$"))
                    continue;
                if (type) {
                    typeMap.put(name, codeNode.getId());
                    idToTypeNameMap.put(codeNode.getId(), name);
                    String shortName = name;
                    int p = shortName.lastIndexOf('.');
                    if (p > 0)
                        shortName = shortName.substring(p + 1, shortName.length());
                    if (!typeShortNameMap.containsKey(shortName))
                        typeShortNameMap.put(shortName, new HashSet<>());
                    typeShortNameMap.get(shortName).add(codeNode.getId());
                } else {
                    if (!methodMap.containsKey(name))
                        methodMap.put(name, new HashSet<>());
                    methodMap.get(name).add(codeNode.getId());
                    idToMethodNameMap.put(codeNode.getId(), name);
                    int p1 = name.lastIndexOf('.');
                    int p2 = name.lastIndexOf('.', p1 - 1);
                    String midName, shortName;
                    if (p2 > 0) {
                        midName = name.substring(p2 + 1);
                        shortName = name.substring(p1 + 1);
                    } else {
                        midName = name;
                        shortName = name.substring(p1 + 1);
                    }
                    if (!methodMidNameMap.containsKey(midName))
                        methodMidNameMap.put(midName, new HashSet<>());
                    methodMidNameMap.get(midName).add(codeNode.getId());
                    if (!methodShortNameMap.containsKey(shortName))
                        methodShortNameMap.put(shortName, new HashSet<>());
                    methodShortNameMap.get(shortName).add(codeNode.getId());
                }
            }

            tx.success();
        }
    }

}
