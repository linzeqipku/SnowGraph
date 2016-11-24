package pfr.plugins.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow;

public class QuestionInfo
{

	Node node=null;
	int questionId = 0;
	int acceptedAnswerId = -1;
	int ownerUserId = -1;
	
	

	public QuestionInfo(Node node, int id, String creationDate, int score, int viewCount, String body, int ownerUserId, String title, String tags, int acceptedAnswerId)
	{
		this.node=node;
		this.questionId=id;
		this.acceptedAnswerId=acceptedAnswerId;
		this.ownerUserId = ownerUserId;
		
		node.addLabel(Label.label(PfrPluginForStackOverflow.QUESTION));
		
		node.setProperty(PfrPluginForStackOverflow.QUESTION_ID, id);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_CREATION_DATE, creationDate);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_SCORE, score);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_VIEW_COUNT, viewCount);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_BODY, body);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_OWNER_USER_ID, ownerUserId);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_TITLE, title);
		node.setProperty(PfrPluginForStackOverflow.QUESTION_TAGS, tags);
		
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
	
	public Node getNode(){
		return node;
	}
}
