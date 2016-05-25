package graphmodel.entity.qa;

import org.neo4j.graphdb.Node;

import graphmodel.ManageElements;

public class QaUserSchema extends QaSchema {
	public static final String USER_ID = "user_id";
	public static final String REPUTATION = "reputation";
	public static final String CREATION_DATE = "creationDate";
	public static final String DISPLAY_NAME = "displayName";
	public static final String LAST_ACCESS_dATE = "lastAccessDate";
	public static final String VIEWS = "views";
	public static final String UP_VOTES = "upVotes";
	public static final String DOWN_VOTES = "downVotes";
	
	private int userId;
	private String displayName;
	
	public QaUserSchema(Node node, int id, int reputation, String creationDate,String displayName,String lastAccessDate, int views,int upVotes,int downVotes){
		this.node = node;
		this.userId = id;
		this.displayName = displayName;
		
		node.addLabel(ManageElements.Labels.QA_USER);
		
		node.setProperty(USER_ID, id);
		node.setProperty(REPUTATION,reputation);
		node.setProperty(CREATION_DATE,creationDate);
		node.setProperty(DISPLAY_NAME,displayName);
		node.setProperty(LAST_ACCESS_dATE,lastAccessDate);
		node.setProperty(VIEWS,views);
		node.setProperty(UP_VOTES,upVotes);
		node.setProperty(DOWN_VOTES,downVotes);
	}

	public QaUserSchema(Node node){
		this.node = node;
		String strUserId = node.getProperty(USER_ID).toString();
		this.userId = Integer.parseInt(strUserId);
		this.displayName = node.getProperty(DISPLAY_NAME).toString();
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getDisplayName(){
		return displayName;
	}
}
