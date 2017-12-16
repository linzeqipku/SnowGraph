package graphdb.extractors.parsers.git;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.EntityDeclaration;
import graphdb.framework.annotations.PropertyDeclaration;

import graphdb.framework.annotations.RelationshipDeclaration;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.*;

import java.io.File;
import java.util.*;

/**
 * Created by oliver on 2017/5/22.
 */
public class GitExtractor implements Extractor {

    @EntityDeclaration
    public static final String COMMIT = "GitCommit";
    @PropertyDeclaration
    public static final String COMMIT_ID = "commitId";
    @PropertyDeclaration
    public static final String COMMIT_DATE = "createDate";
    @PropertyDeclaration
    public static final String COMMIT_LOGMESSAGE = "logMessage";
    @PropertyDeclaration
    public static final String COMMIT_CONTENT = "content";


    @EntityDeclaration
    public static final String COMMITAUTHOR = "GitCommitAuthor";
    @PropertyDeclaration
    public static final String COMMITAUTHOR_NAME = "name";

    @RelationshipDeclaration
    private static final String AUTHOR_OF_COMMIT = "author_of_commit";
    @RelationshipDeclaration
    private static final String PARENT_OF_COMMIT = "parent_of_commit";

    private String gitFolderPath = null;

    @Override
    public void config(String[] args) {
        gitFolderPath=args[0];
    }

    @Override
    public void run(GraphDatabaseService graphDB) {

        Map<String, List<String>> parentsMap=new HashMap<>();
        Map<String, Node> nodeMap=new HashMap<>();
        Map<String, String> authorMap=new HashMap<>();
        Map<String, Node> authorNodeMap=new HashMap<>();

        Collection<File> files=FileUtils.listFiles(new File(gitFolderPath),null,true);
        for (File file:files){
            if (!file.getName().startsWith("commit")||file.length()==0)
                continue;
            try (Transaction tx=graphDB.beginTx()) {
                GitCommit gitCommit = new GitCommit(file);
                parentsMap.put(gitCommit.getCommitId(), gitCommit.getParents());
                nodeMap.put(gitCommit.getCommitId(), gitCommit.createNode(graphDB));
                authorMap.put(gitCommit.getCommitId(), gitCommit.getAuthor());
                tx.success();
            }
        }
        try (Transaction tx=graphDB.beginTx()) {
            for (String commitId : parentsMap.keySet()) {
                for (String parentId : parentsMap.get(commitId))
                    if (nodeMap.containsKey(parentId))
                        nodeMap.get(parentId).createRelationshipTo(nodeMap.get(commitId), RelationshipType.withName(PARENT_OF_COMMIT));
            }
            tx.success();
        }

        try (Transaction tx=graphDB.beginTx()){
            for (String commitId : nodeMap.keySet()) {
                String authorName = authorMap.get(commitId);
                if (!authorNodeMap.containsKey(authorName)) {
                    Node node = graphDB.createNode();
                    node.addLabel(Label.label(COMMITAUTHOR));
                    node.setProperty(COMMITAUTHOR_NAME, authorName);
                    authorNodeMap.put(authorName,node);
                }
                authorNodeMap.get(authorName).createRelationshipTo(nodeMap.get(commitId), RelationshipType.withName(AUTHOR_OF_COMMIT));
            }
            tx.success();
        }

    }
}