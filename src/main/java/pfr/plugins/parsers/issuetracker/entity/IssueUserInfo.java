package pfr.plugins.parsers.issuetracker.entity;

import java.util.UUID;

public class IssueUserInfo {
	private String uuid = UUID.randomUUID().toString();
	
	private String name;// user identifier
	private String emailAddress;
	private String displayName;
	private boolean active;
	
	public IssueUserInfo(String name,String emailAddress, String displayName,boolean active){
		this.name = name;
		this.emailAddress = emailAddress;
		this.displayName = displayName;
		this.active = active;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getUuid() {
		return uuid;
	}
	
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("*****************************\n")
		  	  .append("An Issue User:\n")
		  	  .append("name:").append(name).append("\n")
			  .append("displayName:").append(displayName).append("\n")
			  .append("email:").append(emailAddress).append("\n")
			  .append("active:").append(active).append("\n")
			  .append("*****************************\n");
		return result.toString();
	}
}
