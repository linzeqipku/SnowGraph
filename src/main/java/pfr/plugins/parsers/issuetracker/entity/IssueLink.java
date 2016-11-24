package pfr.plugins.parsers.issuetracker.entity;

public class IssueLink {
	private String issueLinkName;//issue link name
	private String linkIssueId;//link issue id
	
	public IssueLink(){ }
	
	public IssueLink(String issueLinkName,String linkIssueId){
		this.issueLinkName = issueLinkName;
		this.linkIssueId = linkIssueId;
	}
	
	public String getIssueLinkName() {
		return issueLinkName;
	}
	public void setIssueLinkName(String issueLinkName) {
		this.issueLinkName = issueLinkName;
	}
	public String getLinkIssueId() {
		return linkIssueId;
	}
	public void setLinkIssueId(String linkIssueId) {
		this.linkIssueId = linkIssueId;
	}
}
