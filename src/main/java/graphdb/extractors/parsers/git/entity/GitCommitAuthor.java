package graphdb.extractors.parsers.git.entity;

import graphdb.extractors.parsers.git.GitExtractor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by oliver on 2017/5/23.
 */
public class GitCommitAuthor {
    private String name;

    public GitCommitAuthor(String name){
        this.name = name;
    }

    public void setAuthorName(String name){
        this.name = name;
    }

    public String getAuthorName(){
        return this.name;
    }

    public void createAuthorNode(Node node){
        node.addLabel(Label.label(GitExtractor.COMMITAUTHOR));
        node.setProperty(GitExtractor.COMMITAUTHOR_NAME , name );
    }

    public static boolean createRelationshipTo(Node authorNode , Node commitNode , String relationName){
        boolean isAuthorNode = false;
        boolean isCommitNode = false;

        Iterable<Label> labels = authorNode.getLabels();
        if(labels != null) {
            for (Label label : labels) {
                if(label.name().compareTo(GitExtractor.COMMITAUTHOR) == 0){
                    isAuthorNode = true;
                    break;
                }
            }
        }
        if(!isAuthorNode)
            return false;

        labels = commitNode.getLabels();
        if(labels != null){
            for(Label label : labels){
                if(label.name().compareTo(GitExtractor.COMMIT) ==0){
                    isCommitNode = true;
                    break;
                }
            }
        }
        if(!isCommitNode)
            return false;
        authorNode.createRelationshipTo(commitNode , RelationshipType.withName(relationName) );

        return true;
    }
}
