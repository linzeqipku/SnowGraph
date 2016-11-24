package pfr.plugins.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow;

public class QaCommentInfo
{

	Node node=null;
	int commentId = 0;
	int parentId = 0;
	int userId=-1;

	public QaCommentInfo(Node node, int id, int parentId, int score, String text, String creationDate, int userId)
	{
		this.node=node;
		this.commentId=id;
		this.parentId=parentId;
		this.userId=userId;
		
		node.addLabel(Label.label(PfrPluginForStackOverflow.COMMENT));
		
		node.setProperty(PfrPluginForStackOverflow.COMMENT_ID, id);
		node.setProperty(PfrPluginForStackOverflow.COMMENT_PARENT_ID, parentId);
		node.setProperty(PfrPluginForStackOverflow.COMMENT_SCORE, score);
		node.setProperty(PfrPluginForStackOverflow.COMMENT_TEXT, text);
		node.setProperty(PfrPluginForStackOverflow.COMMENT_CREATION_DATE, creationDate);
		node.setProperty(PfrPluginForStackOverflow.COMMENT_USER_ID, userId);
		
	}
	
	public int getCommentId(){
		return commentId;
	}
	
	public int getParentId(){
		return parentId;
	}

	public int getUserId() {
		return userId;
	}
	
	public Node getNode(){
		return node;
	}
}
