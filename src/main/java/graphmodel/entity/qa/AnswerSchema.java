package graphmodel.entity.qa;

import org.neo4j.graphdb.Node;

import graphmodel.ManageElements;

public class AnswerSchema extends QaSchema
{

	int answerId = 0;
	int parentQuestionId = 0;
	int ownerUserId = -1;
	
	public static final String ACCEPTED="accepted";
	public static final String ANSWER_ID="answerId";
	public static final String PARENT_QUESTION_ID="parentQuestionId";
	public static final String CREATION_DATE="creationDate";
	public static final String SCORE="score";
	public static final String BODY="body";
	public static final String OWNER_USER_ID="ownerUserId";

	public AnswerSchema(Node node, int id, int parentId, String creationDate, int score, String body, int ownerUserId)
	{
		this.node=node;
		this.answerId=id;
		this.parentQuestionId=parentId;
		this.ownerUserId=ownerUserId;
		
		node.addLabel(ManageElements.Labels.ANSWER);
		
		node.setProperty(ANSWER_ID, id);
		node.setProperty(PARENT_QUESTION_ID, parentId);
		node.setProperty(CREATION_DATE, creationDate);
		node.setProperty(SCORE, score);
		node.setProperty(BODY, body);
		node.setProperty(OWNER_USER_ID, ownerUserId);
		node.setProperty(ACCEPTED, false);
		
	}
	
	public int getAnswerId(){
		return answerId;
	}
	
	public int getParentQuestionId(){
		return parentQuestionId;
	}
	
	public void setAccepted(boolean accepted){
		node.setProperty(ACCEPTED, accepted);
	}

	public int getOwnerUserId() {
		return ownerUserId;
	}
}
