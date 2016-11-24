package pfr.plugins.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow;

public class AnswerInfo
{

	Node node=null;
	int answerId = 0;
	int parentQuestionId = 0;
	int ownerUserId = -1;

	public AnswerInfo(Node node, int id, int parentId, String creationDate, int score, String body, int ownerUserId)
	{
		this.node=node;
		this.answerId=id;
		this.parentQuestionId=parentId;
		this.ownerUserId=ownerUserId;
		
		node.addLabel(Label.label(PfrPluginForStackOverflow.ANSWER));
		
		node.setProperty(PfrPluginForStackOverflow.ANSWER_ID, id);
		node.setProperty(PfrPluginForStackOverflow.ANSWER_PARENT_QUESTION_ID, parentId);
		node.setProperty(PfrPluginForStackOverflow.ANSWER_CREATION_DATE, creationDate);
		node.setProperty(PfrPluginForStackOverflow.ANSWER_SCORE, score);
		node.setProperty(PfrPluginForStackOverflow.ANSWER_BODY, body);
		node.setProperty(PfrPluginForStackOverflow.ANSWER_OWNER_USER_ID, ownerUserId);
		node.setProperty(PfrPluginForStackOverflow.ANSWER_ACCEPTED, false);
		
	}
	
	public int getAnswerId(){
		return answerId;
	}
	
	public int getParentQuestionId(){
		return parentQuestionId;
	}
	
	public void setAccepted(boolean accepted){
		node.setProperty(PfrPluginForStackOverflow.ANSWER_ACCEPTED, accepted);
	}

	public int getOwnerUserId() {
		return ownerUserId;
	}
	
	public Node getNode(){
		return node;
	}
	
}
