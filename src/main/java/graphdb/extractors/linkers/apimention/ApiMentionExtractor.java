package graphdb.extractors.linkers.apimention;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.jsoup.Jsoup;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.RelationshipDeclaration;

public class ApiMentionExtractor implements Extractor {
    @RelationshipDeclaration
    public static final String API_NAME_MENTION = "apiNameMention";

    GraphDatabaseService db = null;
    CodeIndexes codeIndexes = null;

    Map<Node, String> nodeToTextMap = new HashMap<>();

    public void run(GraphDatabaseService db) {
        this.db = db;
        codeIndexes = new CodeIndexes(db);
        try (Transaction tx=db.beginTx()){
        	for (Node node:db.getAllNodes()){
        		if (!node.hasProperty(TextExtractor.TEXT))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS)))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.FIELD)))
        			continue;
        		String text=(String) node.getProperty(TextExtractor.TITLE);
        		text+=" "+node.getProperty(TextExtractor.TEXT);
        		nodeToTextMap.put(node, text);
        	}
        	tx.success();
        }
        find();
    }

    void find() {
        try (Transaction tx = db.beginTx()) {

            for (Node srcNode : nodeToTextMap.keySet()) {
                String content = Jsoup.parse(nodeToTextMap.get(srcNode)).text();
                Set<String> lexes = new HashSet<>();
                Collections.addAll(lexes, content.split("\\W+"));
                Set<Node> resultNodes = new HashSet<>();

                //类/接口
                for (String typeShortName : codeIndexes.typeShortNameMap.keySet())
                    if (lexes.contains(typeShortName))
                        for (long id : codeIndexes.typeShortNameMap.get(typeShortName))
                            resultNodes.add(db.getNodeById(id));

                for (String methodShortName : codeIndexes.methodShortNameMap.keySet()) {
                    //后接小括号，不要构造函数
                    if (methodShortName.charAt(0) < 'a' || methodShortName.charAt(0) > 'z' || !(lexes.contains(methodShortName) && content.contains(methodShortName + "(")))
                        continue;
                    boolean flag = false;
                    //无歧义
                    if (codeIndexes.methodShortNameMap.get(methodShortName).size() == 1) {
                        for (long id : codeIndexes.methodShortNameMap.get(methodShortName))
                            resultNodes.add(db.getNodeById(id));
                        flag = true;
                    }
                    //主类在
                    for (long methodNodeId : codeIndexes.methodShortNameMap.get(methodShortName)) {
                        Node methodNode = db.getNodeById(methodNodeId);
                        if (resultNodes.contains(methodNode.getRelationships(RelationshipType.withName(JavaCodeExtractor.HAVE_METHOD), Direction.INCOMING).iterator().next().getStartNode())) {
                            resultNodes.add(methodNode);
                            flag = true;
                        }
                    }
                    //歧义不多
                    if (!flag && codeIndexes.methodShortNameMap.get(methodShortName).size() <= 5)
                        for (long id : codeIndexes.methodShortNameMap.get(methodShortName))
                            resultNodes.add(db.getNodeById(id));
                }

                for (Node rNode : resultNodes)
                    srcNode.createRelationshipTo(rNode, RelationshipType.withName(API_NAME_MENTION));

            }

            tx.success();
        }
    }

}
