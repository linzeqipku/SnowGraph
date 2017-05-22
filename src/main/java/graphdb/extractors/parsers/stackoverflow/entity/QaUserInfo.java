package graphdb.extractors.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;

public class QaUserInfo {

    Node node = null;
    private int userId;
    private String displayName;

    public QaUserInfo(Node node, int id, int reputation, String creationDate, String displayName, String lastAccessDate, int views, int upVotes, int downVotes) {
        this.node = node;
        this.userId = id;
        this.displayName = displayName;

        node.addLabel(Label.label(StackOverflowExtractor.USER));

        node.setProperty(StackOverflowExtractor.USER_ID, id);
        node.setProperty(StackOverflowExtractor.USER_REPUTATION, reputation);
        node.setProperty(StackOverflowExtractor.USER_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowExtractor.USER_DISPLAY_NAME, displayName);
        node.setProperty(StackOverflowExtractor.USER_LAST_ACCESS_dATE, lastAccessDate);
        node.setProperty(StackOverflowExtractor.USER_VIEWS, views);
        node.setProperty(StackOverflowExtractor.USER_UP_VOTES, upVotes);
        node.setProperty(StackOverflowExtractor.USER_DOWN_VOTES, downVotes);
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
