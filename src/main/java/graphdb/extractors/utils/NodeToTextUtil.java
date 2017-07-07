package graphdb.extractors.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import graphdb.framework.annotations.PropertyDeclaration;

public class NodeToTextUtil {

    public static Map<Node, String> prepareNodeToTextMap(GraphDatabaseService db, Set<String> focusSet) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Map<Node, String> nodeToTextMap = new HashMap<Node, String>();
        Map<String, Set<String>> propMap = new HashMap<String, Set<String>>();
        for (String str : focusSet) {
            int p = str.lastIndexOf('.');
            String className = str.substring(0, p);
            String fieldName = str.substring(p + 1, str.length());
            Class pluginClass = Class.forName(className);
            Field field = pluginClass.getField(fieldName);
            String concept = field.getAnnotation(PropertyDeclaration.class).parent();
            if (!propMap.containsKey(concept))
                propMap.put(concept, new HashSet<String>());
            propMap.get(concept).add((String) field.get(null));
        }
        try (Transaction tx = db.beginTx()) {
            for (Node node : db.getAllNodes()) {
                if (!node.getLabels().iterator().hasNext())
                    continue;
                String label = node.getLabels().iterator().next().name();
                String content = "";
                boolean flag = false;
                if (propMap.containsKey(label))
                    for (String property : propMap.get(label)) {
                        if (node.hasProperty(property)) {
                            content += node.getProperty(property) + " ";
                            flag = true;
                        }
                    }
                if (propMap.containsKey(""))
                    for (String property : propMap.get("")) {
                        if (node.hasProperty(property)) {
                            content += node.getProperty(property) + " ";
                            flag = true;
                        }
                    }
                if (flag)
                    nodeToTextMap.put(node, content);
            }
            tx.success();
        }
        return nodeToTextMap;
    }

}
