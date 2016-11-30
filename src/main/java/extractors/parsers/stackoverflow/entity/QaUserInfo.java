package extractors.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor;

public class QaUserInfo {

    Node node = null;
    private int userId;
    private String displayName;

    public QaUserInfo(Node node, int id, int reputation, String creationDate, String displayName, String lastAccessDate, int views, int upVotes, int downVotes) {
        this.node = node;
        this.userId = id;
        this.displayName = displayName;

        node.addLabel(Label.label(StackOverflowKnowledgeExtractor.USER));

        node.setProperty(StackOverflowKnowledgeExtractor.USER_ID, id);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_REPUTATION, reputation);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_DISPLAY_NAME, displayName);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_LAST_ACCESS_dATE, lastAccessDate);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_VIEWS, views);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_UP_VOTES, upVotes);
        node.setProperty(StackOverflowKnowledgeExtractor.USER_DOWN_VOTES, downVotes);
    }

    public int getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Node getNode() {
        return node;
    }
}
