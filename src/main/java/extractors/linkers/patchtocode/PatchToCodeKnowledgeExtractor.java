package extractors.linkers.patchtocode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractors.parsers.jira.JiraKnowledgeExtractor;
import extractors.parsers.javacode.JavaCodeKnowledgeExtractor;
import framework.KnowledgeExtractor;
import framework.annotations.RelationshipDeclaration;
import org.neo4j.graphdb.*;
/**
/**
 * Created by laurence on 17-2-1.
 */
public class PatchToCodeKnowledgeExtractor implements KnowledgeExtractor {
    @RelationshipDeclaration
    public static final String PATCH_TO_CLASS = "patchToClass";

    GraphDatabaseService db = null;
    HashMap<String, Node> patchMap = new HashMap<>();
    HashMap<String, Node> nameForClassMap = new HashMap<>();
    HashMap<Node, ArrayList<Node>> patchToClassMap = new HashMap<>();

    @Override
    public void run(GraphDatabaseService graphDB) {
        this.db = graphDB;
        getPatchAndCodeNode();
        extractClassLink();
    }

    public void getPatchAndCodeNode(){
        try (Transaction tx = db.beginTx()){
            for (Node node : db.getAllNodes()) {
                if (!node.getLabels().iterator().hasNext())
                    continue;
                if (node.hasLabel(Label.label(JiraKnowledgeExtractor.PATCH))) {
                    String content = (String) node.getProperty(JiraKnowledgeExtractor.PATCH_CONTENT);
                    if (content != null) {
                        patchMap.put(content, node);
                    }
                } else if (node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS))) {
                    String name = (String) node.getProperty(JavaCodeKnowledgeExtractor.CLASS_NAME);
                    nameForClassMap.put(name, node);
                }
            }
            System.out.println("patch number " + patchMap.size());
            tx.success();
        }
    }

    public void extractClassLink(){
        for (String content : patchMap.keySet()){
            String patternForClass = "diff --git(.+?\\..+?) ";
            Pattern regex = Pattern.compile(patternForClass);
            Matcher classMatcher = regex.matcher(content);
            ArrayList<Node> classList = new ArrayList<>();
            while(classMatcher.find()){
                String fullPath = classMatcher.group(1);
                String className = fullPath.substring(fullPath.lastIndexOf('/') + 1,
                            fullPath.lastIndexOf('.'));
                Node node = nameForClassMap.get(className);
                if (node != null) {
                    classList.add(nameForClassMap.get(className));
                    //System.out.println(className);
                }
                //System.out.println(classMatcher.group(0));
            }
            patchToClassMap.put(patchMap.get(content), classList);
        }
        try (Transaction tx = db.beginTx()){
            for (Node patchNode : patchToClassMap.keySet()){
                for (Node classNode : patchToClassMap.get(patchNode)){
                    patchNode.createRelationshipTo(classNode, RelationshipType.withName(PATCH_TO_CLASS));
                }
            }
            tx.success();
        }
    }
}
