package pfr.plugins.parsers.issuetracker.entity;

import java.util.UUID;

public class IssueCommentInfo {
	private String uuid = UUID.randomUUID().toString();
	
	private String commentId;
	private String body;
	private String creatorName;
	private String updaterName;
	private String createdDate;
	private String updatedDate;

	public IssueCommentInfo(){ }
	
	public IssueCommentInfo(String id, String body, String creatorName,String updaterName,String createdDate, String updatedDate){ 
		this.commentId = id;
		this.body = body;
		this.creatorName = creatorName;
		this.updaterName = updaterName;
		this.createdDate = createdDate;
		this.updatedDate = updatedDate;
	}
	
	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String id) {
		this.commentId = id;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public String getUpdaterName() {
		return updaterName;
	}

	public void setUpdaterName(String updaterName) {
		this.updaterName = updaterName;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUuid() {
		return uuid;
	}
	
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("*****************************************\n")
			  .append("A comment:\n")
			  .append("id:").append(commentId).append("\n")
			  .append("body:").append(body).append("\n")
			  .append("creator:").append(creatorName).append("\n")
			  .append("updater:").append(updaterName).append("\n")
			  .append("created:").append(createdDate).append("\n")
			  .append("updated:").append(updatedDate).append("\n")
			  .append("*****************************************\n");
		return result.toString();
	}
}
