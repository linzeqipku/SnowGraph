package extractors.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor;

public class AnswerInfo {

    Node node = null;
    int answerId = 0;
    int parentQuestionId = 0;
    int ownerUserId = -1;

    public AnswerInfo(Node node, int id, int parentId, String creationDate, int score, String body, int ownerUserId) {
        this.node = node;
        this.answerId = id;
        this.parentQuestionId = parentId;
        this.ownerUserId = ownerUserId;

        node.addLabel(Label.label(StackOverflowKnowledgeExtractor.ANSWER));

        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_ID, id);
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_PARENT_QUESTION_ID, parentId);
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_CREATION_DATE, creationDate);
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_SCORE, score);
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_BODY, body);
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_OWNER_USER_ID, ownerUserId);
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_ACCEPTED, false);

    }

    public int getAnswerId() {
        return answerId;
    }

    public int getParentQuestionId() {
        return parentQuestionId;
    }

    public void setAccepted(boolean accepted) {
        node.setProperty(StackOverflowKnowledgeExtractor.ANSWER_ACCEPTED, accepted);
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public Node getNode() {
        return node;
    }

}
