package graphmodel.entity.qa;

import org.neo4j.graphdb.Node;

import graphmodel.ManageElements;

public class QuestionSchema extends QaSchema
{

	int questionId = 0;
	int acceptedAnswerId = -1;
	int ownerUserId = -1;
	
	public static final String QUESTION_ID="questionId";
	public static final String CREATION_DATE="creationDate";
	public static final String SCORE="score";
	public static final String VIEW_COUNT="viewCount";
	public static final String BODY="body";
	public static final String OWNER_USER_ID="ownerUserId";
	public static final String TITLE="title";
	public static final String TAGS="tags";

	public QuestionSchema(Node node, int id, String creationDate, int score, int viewCount, String body, int ownerUserId, String title, String tags, int acceptedAnswerId)
	{
		this.node=node;
		this.questionId=id;
		this.acceptedAnswerId=acceptedAnswerId;
		this.ownerUserId = ownerUserId;
		
		node.addLabel(ManageElements.Labels.QUESTION);
		
		node.setProperty(QUESTION_ID, id);
		node.setProperty(CREATION_DATE, creationDate);
		node.setProperty(SCORE, score);
		node.setProperty(VIEW_COUNT, viewCount);
		node.setProperty(BODY, body);
		node.setProperty(OWNER_USER_ID, ownerUserId);
		node.setProperty(TITLE, title);
		node.setProperty(TAGS, tags);
		
	}
	
	public int getQuestionId(){
		return questionId;
	}
	
	public int getAcceptedAnswerId(){
		return acceptedAnswerId;
	}

	public int getOwnerUserId() {
		return ownerUserId;
	}
}
