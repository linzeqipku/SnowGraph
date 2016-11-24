package pfr.plugins.parsers.stackoverflow.entity;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow;

public class QaUserInfo{
	
	Node node=null;
	private int userId;
	private String displayName;
	
	public QaUserInfo(Node node, int id, int reputation, String creationDate,String displayName,String lastAccessDate, int views,int upVotes,int downVotes){
		this.node = node;
		this.userId = id;
		this.displayName = displayName;
		
		node.addLabel(Label.label(PfrPluginForStackOverflow.USER));
		
		node.setProperty(PfrPluginForStackOverflow.USER_ID, id);
		node.setProperty(PfrPluginForStackOverflow.USER_REPUTATION,reputation);
		node.setProperty(PfrPluginForStackOverflow.USER_CREATION_DATE,creationDate);
		node.setProperty(PfrPluginForStackOverflow.USER_DISPLAY_NAME,displayName);
		node.setProperty(PfrPluginForStackOverflow.USER_LAST_ACCESS_dATE,lastAccessDate);
		node.setProperty(PfrPluginForStackOverflow.USER_VIEWS,views);
		node.setProperty(PfrPluginForStackOverflow.USER_UP_VOTES,upVotes);
		node.setProperty(PfrPluginForStackOverflow.USER_DOWN_VOTES,downVotes);
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getDisplayName(){
		return displayName;
	}
	
	public Node getNode(){
		return node;
	}
}
