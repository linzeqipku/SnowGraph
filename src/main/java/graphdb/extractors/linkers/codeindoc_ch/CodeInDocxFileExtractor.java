package graphdb.extractors.linkers.codeindoc_ch;

import graphdb.extractors.linkers.apimention.CodeIndexes;
import graphdb.extractors.linkers.designtorequire_ch.DesignToRequireExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.word.WordKnowledgeExtractor;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.RelationshipDeclaration;
import org.neo4j.graphdb.*;

import java.util.Map;
import java.util.Set;

/**
 * Created by maxkibble on 2017/5/26.
 */
public class CodeInDocxFileExtractor implements Extractor {

    @RelationshipDeclaration
    public static final String API_EXPLAINED_BY = "api_explained_by";

    private GraphDatabaseService db = null;

    @Override
    public void config(String[] args) {

    }

    public int countCommonWords(String s, String t) {
        String[] tokenS = s.split("\\.");
        String[] tokenT = t.split("\\.");
        int ret = 0;
        for(String t1: tokenS) {
            for(String t2: tokenT) {
                if(t1.equals(t2)) {
                    ret++;
                }
            }
        }
        return ret;
    }

    public void run(GraphDatabaseService db) {
        this.db = db;
        try (Transaction tx=db.beginTx()){
            db.getAllRelationships().forEach(rel->{
                if (rel.getType().equals(RelationshipType.withName(API_EXPLAINED_BY)))
                    rel.delete();
            });
            tx.success();
        }
        CodeIndexes codeIndexes = new CodeIndexes(db);
        Map<String, Set<Long>> methodShortNameMap = codeIndexes.methodShortNameMap;
        try(Transaction tx = db.beginTx()) {
            for(Node node : db.getAllNodes()) {
                if(!node.hasLabel(Label.label(WordKnowledgeExtractor.DOCX_SECTION)))
                    continue;
                if(!node.getProperty(WordKnowledgeExtractor.SECTION_USAGE_TYPE).equals("design"))
                    continue;
                if(!DesignToRequireExtractor.isLeafSection(node))
                    continue;
                String sectionPackageName = (String) node.getProperty(WordKnowledgeExtractor.SECTION_PACKAGE);
                String sectionApiList = (String) node.getProperty(WordKnowledgeExtractor.SECTION_APIS);
                String[] sectionApis = sectionApiList.split("\n");
                for(String api : sectionApis) {
                    // create relationship by the method short name
                    // check whether the package name is identical as well
                    if(!methodShortNameMap.containsKey(api)) continue;
                    Set<Long> nodeIdList = methodShortNameMap.get(api);
                    int maxTokenNum = 0;
                    Node matchedMethodNode = null;
                    for(Long id : nodeIdList) {
                        Node methodNode = db.getNodeById(id);
                        String codePackageName = (String) methodNode.getProperty(JavaCodeExtractor.METHOD_BELONGTO);
                        //System.out.println("Find the same-name method");
                        int tokenNum = countCommonWords(codePackageName, sectionPackageName);
                        if(tokenNum > maxTokenNum) {
                            maxTokenNum = tokenNum;
                            matchedMethodNode = methodNode;
                        }
                        if (!api.equals(api.toLowerCase()))
                            methodNode.createRelationshipTo(node, RelationshipType.withName(API_EXPLAINED_BY));
                    }

                    //System.out.println("Create link between code and section");
                    if(api.equals(api.toLowerCase())&&matchedMethodNode != null) {
                        matchedMethodNode.createRelationshipTo(node, RelationshipType.withName(API_EXPLAINED_BY));
                    }
                    // TO-DO: create relationship by the class/interface name
                    // TO-DO: define relationship accuracy by take method type and parameter into consideration
                }
            }
            tx.success();
            tx.close();
        }
    }
}
