package graphdb.extractors.linkers.designtorequire_ch;

import graphdb.extractors.linkers.codeindoc_ch.CodeInDocxFileExtractor;
import graphdb.extractors.parsers.word.WordKnowledgeExtractor;
import graphdb.extractors.parsers.word.utils.Config;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.RelationshipDeclaration;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * Created by maxkibble on 2017/6/18.
 */
public class DesignToRequireExtractor implements Extractor {

    @RelationshipDeclaration
    public static final String DESIGNED_BY = "function_designed_by";

    GraphDatabaseService db;
    HashSet<Node> designSectionList = new HashSet<>();
    HashSet<Node> requireSectionList = new HashSet<>();
    HashSet<Node> designLeafSectionList = new HashSet<>();
    HashSet<Node> requireLeafSectionList = new HashSet<>();
    static HashSet<String> sectionNameStopWords = new HashSet<>();
    static HashSet<String> subSectionStopWords = new HashSet<>();

    public boolean relatedToCode(Node node) {
        for(Relationship relationship : node.getRelationships()) {
            if(relationship.isType(RelationshipType.withName(CodeInDocxFileExtractor.API_EXPLAINED_BY)))
                return true;
        }
        return false;
    }

    public static boolean isLeafSection(Node node) {
        if(!node.hasLabel(Label.label(WordKnowledgeExtractor.DOCX_SECTION)))
            return false;
        for(Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
            if(!relationship.isType(RelationshipType.withName(WordKnowledgeExtractor.HAVE_SUB_ELEMENT)))
                continue;
            Node nextNode = relationship.getEndNode();
            if(nextNode.hasLabel(Label.label(WordKnowledgeExtractor.DOCX_SECTION))) return false;
        }
        return true;
    }

    public void initSectionList() {
        try(Transaction tx = db.beginTx()) {
            for(Node node : db.getAllNodes()) {
                if(!node.hasLabel(Label.label(WordKnowledgeExtractor.DOCX_SECTION)))
                    continue;
                if(node.getProperty(WordKnowledgeExtractor.SECTION_USAGE_TYPE).equals("design")) {
                    //if(!relatedToCode(node)) continue;
                    designSectionList.add(node);
                    if(isLeafSection(node)) designLeafSectionList.add(node);
                }
                else if(node.getProperty(WordKnowledgeExtractor.SECTION_USAGE_TYPE).equals("requirement")) {
                    requireSectionList.add(node);
                    if(isLeafSection(node)) requireLeafSectionList.add(node);
                }
            }
            tx.success();
            tx.close();
        }
    }

    public static void loadStopWords() throws IOException {
        List<String> lines= FileUtils.readLines(new File(Config.getSectionTitleStopWordPath()));
        for (String line : lines)
            sectionNameStopWords.add(line);
        lines = FileUtils.readLines(new File(Config.getSubSectionStopWordPath()));
        for (String line : lines)
            subSectionStopWords.add(line);
    }

    public static boolean similarTitle(String s, String t) {
        try {
            loadStopWords();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for(String stopWord : sectionNameStopWords) {
            if(s.equals(stopWord) || t.equals(stopWord)) return false;
        }
        if(s.length() > t.length()) {
            String tmp = s;
            s = t;
            t = tmp;
        }
        int[][] dp = new int[s.length() + 1][t.length() + 1];
        dp[0][0] = 0;
        int maxSubStrLen = 0;
        int maxSubStrIdx = -1;
        for(int i = 1; i <= s.length(); i++) {
            for(int j = 1; j <= t.length(); j++) {
                if(s.charAt(i - 1) == t.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if(dp[i][j] > maxSubStrLen) {
                        maxSubStrLen = dp[i][j];
                        maxSubStrIdx = i;
                    }
                }
                else dp[i][j] = 0;
            }
        }
        int minLen = s.length();
        if(maxSubStrLen < 2) return false;
        if(maxSubStrLen < minLen * 0.5) return false;
        String commonWord = s.substring(maxSubStrIdx - maxSubStrLen, maxSubStrIdx);
        //System.out.println(commonWord);
        for(String stopWord : subSectionStopWords) {
            if(stopWord.equals(commonWord)) return false;
        }
        return true;
    }

    public void buildRelationship(Node dNode, Node rNode) {
        if(!dNode.hasLabel(Label.label(WordKnowledgeExtractor.DOCX_SECTION))) return;
        try(Transaction tx = db.beginTx()) {
            dNode.createRelationshipTo(rNode, RelationshipType.withName(DesignToRequireExtractor.DESIGNED_BY));
            System.out.println(rNode.getProperty(WordKnowledgeExtractor.SECTION_TITLE) +  " " + dNode.getProperty(WordKnowledgeExtractor.SECTION_TITLE));
//            for(Relationship relationship : dNode.getRelationships(Direction.OUTGOING)) {
//                if(!relationship.isType(RelationshipType.withName(WordKnowledgeExtractor.HAVE_SUB_ELEMENT)))
//                    continue;
//                Node nextNode = relationship.getEndNode();
//                buildRelationship(nextNode, rNode);
//            }
            tx.success();
            tx.close();
        }
    }

    public void run(GraphDatabaseService db) {
        this.db = db;
        initSectionList();
        try(Transaction tx = db.beginTx()) {
            for (Node dNode : designLeafSectionList) {
                String dNodeName = (String) dNode.getProperty(WordKnowledgeExtractor.SECTION_TITLE);
                String dNodeProjectName = (String) dNode.getProperty(WordKnowledgeExtractor.SECTION_PROJECT_NAME);
                for (Node rNode : requireSectionList) {
                    String rNodeName = (String) rNode.getProperty(WordKnowledgeExtractor.SECTION_TITLE);
                    String rNodeProjectName = (String) rNode.getProperty(WordKnowledgeExtractor.SECTION_PROJECT_NAME);
                    if (!similarTitle(dNodeName, rNodeName)) continue;
                    if (!dNodeProjectName.equals(rNodeProjectName)) continue;
                    //System.out.println("Find relationship between design and requirement");
                    buildRelationship(dNode, rNode);
                }
            }
            tx.success();
            tx.close();
        }
    }

}
