package graphdb.extractors.linkers.apimention;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.jsoup.Jsoup;
import org.neo4j.graphdb.*;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.RelationshipDeclaration;

public class ApiMentionExtractor implements Extractor {
    @RelationshipDeclaration
    public static final String API_NAME_MENTION = "apiNameMention";

    private GraphDatabaseService db = null;
    private CodeIndexes codeIndexes = null;

    private Set<Node> textNodes = new HashSet<>();

    @Override
    public void config(String[] args) {

    }

    public void run(GraphDatabaseService db) {
        this.db = db;
        codeIndexes = new CodeIndexes(db);
        try (Transaction tx=db.beginTx()){
            for (Relationship rel:db.getAllRelationships()){
                if (rel.getType().equals(RelationshipType.withName(API_NAME_MENTION)))
                    rel.delete();
            }
        	for (Node node:db.getAllNodes()){
                if (!node.hasProperty(TextExtractor.IS_TEXT)||!(boolean)node.getProperty(TextExtractor.IS_TEXT))
                    continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS)))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
        			continue;
        		if (node.hasLabel(Label.label(JavaCodeExtractor.FIELD)))
        			continue;
        		textNodes.add(node);
        	}
        	tx.success();
        }
        find();
    }

    private void find() {
        try (Transaction tx = db.beginTx()) {

            for (Node srcNode : textNodes) {
                String text=(String) srcNode.getProperty(TextExtractor.TITLE);
                text+=" "+srcNode.getProperty(TextExtractor.TEXT);
                String content = Jsoup.parse(text).text();
                Set<String> lexes = new HashSet<>();
                Collections.addAll(lexes, content.toLowerCase().split("\\W+"));
                Set<Node> resultNodes = new HashSet<>();

                //类/接口
                for (String typeShortName : codeIndexes.typeShortNameMap.keySet())
                    if (lexes.contains(typeShortName.toLowerCase()))
                        for (long id : codeIndexes.typeShortNameMap.get(typeShortName))
                            resultNodes.add(db.getNodeById(id));

                for (String methodShortName : codeIndexes.methodShortNameMap.keySet()) {
                    //后接小括号，不要构造函数
                    if (methodShortName.charAt(0) < 'a' || methodShortName.charAt(0) > 'z' || !(lexes.contains(methodShortName.toLowerCase()) && content.contains(methodShortName + "(")))
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
