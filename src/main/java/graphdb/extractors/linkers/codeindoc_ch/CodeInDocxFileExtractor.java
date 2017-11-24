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
    private static final String API_EXPLAINED_BY = "api_explained_by";

    private GraphDatabaseService db = null;

    public void run(GraphDatabaseService db) {
        this.db = db;
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
                    for(Long id : nodeIdList) {
                        Node methodNode = db.getNodeById(id);
                        String codePackageName = (String) methodNode.getProperty(JavaCodeExtractor.METHOD_BELONGTO);
                        //System.out.println("Find the same-name method");
                        if(!codePackageName.equals(sectionPackageName)) continue;
                        //System.out.println("Create link between code and section");
                        methodNode.createRelationshipTo(node, RelationshipType.withName(API_EXPLAINED_BY));
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
