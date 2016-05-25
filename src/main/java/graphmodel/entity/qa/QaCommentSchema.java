package graphmodel.entity.qa;

import org.neo4j.graphdb.Node;

import graphmodel.ManageElements;

public class QaCommentSchema extends QaSchema
{

	int commentId = 0;
	int parentId = 0;
	int userId=-1;
	
	public static final String COMMENT_ID="commentId";
	public static final String PARENT_ID="parentId";
	public static final String SCORE="score";
	public static final String TEXT="text";
	public static final String CREATION_DATE="creationDate";
	public static final String USER_ID="userId";

	public QaCommentSchema(Node node, int id, int parentId, int score, String text, String creationDate, int userId)
	{
		this.node=node;
		this.commentId=id;
		this.parentId=parentId;
		this.userId=userId;
		
		node.addLabel(ManageElements.Labels.QA_COMMENT);
		
		node.setProperty(COMMENT_ID, id);
		node.setProperty(PARENT_ID, parentId);
		node.setProperty(SCORE, score);
		node.setProperty(TEXT, text);
		node.setProperty(CREATION_DATE, creationDate);
		node.setProperty(USER_ID, userId);
		
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
}
