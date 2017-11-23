package graphdb.extractors.linkers.ref;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graphdb.extractors.linkers.apimention.CodeIndexes;
import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.git.GitExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.jira.JiraExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.RelationshipDeclaration;

public class ReferenceExtractor implements Extractor {

    @RelationshipDeclaration
    public static final String REFERENCE = "reference";

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
        		
        		String text="";
        		if (node.hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
        			text="<h2>"+node.getProperty(StackOverflowExtractor.QUESTION_TITLE)+"</h2>";
        			text+=(String) node.getProperty(StackOverflowExtractor.QUESTION_BODY);
        		}
        		else if (node.hasLabel(Label.label(StackOverflowExtractor.ANSWER)))
        			text=(String) node.getProperty(StackOverflowExtractor.ANSWER_BODY);
        		else if (node.hasLabel(Label.label(StackOverflowExtractor.COMMENT)))
        			text=(String) node.getProperty(StackOverflowExtractor.COMMENT_TEXT);
        		else {
        			text=(String) node.getProperty(TextExtractor.TITLE);
            		text+=" "+node.getProperty(TextExtractor.TEXT);
        		}
        		
        		nodeToTextMap.put(node, text);
        	}
        	fromHtmlToCodeElement();
        	fromTextToJiraOrCommit();
        	fromPatchToCodeElement();
        	tx.success();
        }
    }

    void fromHtmlToCodeElement() {
        try (Transaction tx = db.beginTx()) {
            for (Node srcNode : nodeToTextMap.keySet()) {
                String content = nodeToTextMap.get(srcNode);
                Set<String> tokens = new HashSet<>();
                for (String token : content.split("\\W+"))
                    if (token.length() > 0)
                        tokens.add(token);
                
                //链接的类和方法
                Set<Long> linkTypeSet = new HashSet<>();
                Set<Long> linkMethodSet = new HashSet<>();
                //所有的链接地址
                String ahrefs = "";
                Elements links = Jsoup.parse("<html>"+content+"</html>").select("a[href]");
                
                if (links.size()==0)
                	continue;
                
                for (Element link : links)
                    ahrefs += link.attr("href") + "||||||||||";
                try {
                    ahrefs = URLDecoder.decode(ahrefs.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //找到所有名字在文本中出现过的类
                Map<String, Set<Long>> occTypeMap = new HashMap<>();
                for (String typeShortName : codeIndexes.typeShortNameMap.keySet())
                    if (tokens.contains(typeShortName))
                        occTypeMap.put(typeShortName, codeIndexes.typeShortNameMap.get(typeShortName));
                //找到所有名字在文本中出现过的方法
                Map<String, Set<Long>> occMethodMap = new HashMap<>();
                for (String methodShortName : codeIndexes.methodShortNameMap.keySet())
                    if (tokens.contains(methodShortName))
                        occMethodMap.put(methodShortName, codeIndexes.methodShortNameMap.get(methodShortName));

                //精确链接匹配到的方法
                for (String methodShortName : occMethodMap.keySet())
                    for (long id : occMethodMap.get(methodShortName)) {
                        String str = ((String) db.getNodeById(id).getProperty(JavaCodeExtractor.METHOD_BELONGTO)).replace(".", "/");
                        str += ".html#" + methodShortName + "(" + db.getNodeById(id).getProperty(JavaCodeExtractor.METHOD_PARAMS) + ")";
                        if (ahrefs.contains(str))
                            linkMethodSet.add(id);
                    }
                //精确链接匹配到的类
                for (String typeShortName : occTypeMap.keySet())
                    for (long id : occTypeMap.get(typeShortName)) {
                        String str = ((String) db.getNodeById(id).getProperty(JavaCodeExtractor.CLASS_FULLNAME)).replace(".", "/");
                        str += ".html|";
                        if (ahrefs.contains(str))
                            linkTypeSet.add(id);
                    }
                //建立链接关联
                for (long id : linkMethodSet)
                    srcNode.createRelationshipTo(db.getNodeById(id), RelationshipType.withName(REFERENCE));
                for (long id : linkTypeSet)
                    srcNode.createRelationshipTo(db.getNodeById(id), RelationshipType.withName(REFERENCE));

            }
            tx.success();
        }
    }
    
    void fromTextToJiraOrCommit(){
    	Map<String, Node> jiraMap=new HashMap<>();
    	Map<String, Node> commitMap=new HashMap<>();
    	try (Transaction tx = db.beginTx()) {
    		for (Node node:db.getAllNodes()){
    			if (node.hasLabel(Label.label(JiraExtractor.ISSUE))){
    				String name=(String) node.getProperty(JiraExtractor.ISSUE_NAME);
    				jiraMap.put(name, node);
    			}
    			else if (node.hasLabel(Label.label(GitExtractor.COMMIT))){
    				String name=(String) node.getProperty(GitExtractor.COMMIT_VERSION);
    				commitMap.put(name, node);
    			}
    		}
    		tx.success();
    	}
    	try (Transaction tx = db.beginTx()) {
            for (Node srcNode : nodeToTextMap.keySet()) {
                String content = nodeToTextMap.get(srcNode);
                Set<String> tokenSet=new HashSet<>();
                for (String e:content.split("[^A-Za-z0-9\\-_]+"))
                	tokenSet.add(e);
                for (String jiraName:jiraMap.keySet()){
                	if (tokenSet.contains(jiraName))
                		srcNode.createRelationshipTo(jiraMap.get(jiraName), RelationshipType.withName(REFERENCE));
                }
                for (String version:commitMap.keySet()){
                	if (tokenSet.contains(version)||tokenSet.contains("r"+version))
                		srcNode.createRelationshipTo(commitMap.get(version), RelationshipType.withName(REFERENCE));
                }
            }
            tx.success();
    	}
    }

    void fromPatchToCodeElement(){
    	
    	HashMap<String, Node> patchMap = new HashMap<>();
    	try (Transaction tx = db.beginTx()) {
    		for (Node node:db.getAllNodes()){
    			if (node.hasLabel(Label.label(JiraExtractor.PATCH))){
    				String name=(String) node.getProperty(JiraExtractor.PATCH_NAME);
    				patchMap.put(name, node);
    			}
    		}
    		for (Node patchNode:patchMap.values()){
    			String content=(String) patchNode.getProperty(JiraExtractor.PATCH_CONTENT);
    			for (String typeName:codeIndexes.typeMap.keySet()){
    				String str=typeName.replace(".", "/")+".java";
    				if (content.contains(str))
    					patchNode.createRelationshipTo(db.getNodeById(codeIndexes.typeMap.get(typeName)), RelationshipType.withName(REFERENCE));
    			}
    		}
    		tx.success();
    	}
    }
    
}
