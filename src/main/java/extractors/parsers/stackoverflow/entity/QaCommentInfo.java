package extractors.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor;

public class QaCommentInfo {

    Node node = null;
    int commentId = 0;
    int parentId = 0;
    int userId = -1;

    public QaCommentInfo(Node node, int id, int parentId, int score, String text, String creationDate, int userId) {
        this.node = node;
        this.commentId = id;
        this.parentId = parentId;
        this.userId = userId;

        node.addLabel(Label.label(StackOverflowKnowledgeExtractor.COMMENT));

        node.setProperty(StackOverflowKnowledgeExtractor.COMMENT_ID, id);
        node.setProperty(StackOverflowKnowledgeExtractor.COMMENT_PARENT_ID, parentId);
        node.setProperty(StackOverflowKnowledgeExtractor.COMMENT_SCORE, score);
        node.setProperty(StackOverflowKnowledgeExtractor.COMMENT_TEXT, text);
        node.setProperty(StackOverflowKnowledgeExtractor.COMMENT_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowKnowledgeExtractor.COMMENT_USER_ID, userId);

    }

    public int getCommentId() {
        return commentId;
    }

    public int getParentId() {
        return parentId;
    }

    public int getUserId() {
        return userId;
    }

    public Node getNode() {
        return node;
    }
}
