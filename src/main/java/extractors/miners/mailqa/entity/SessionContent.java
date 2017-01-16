package extractors.miners.mailqa.entity;

public class SessionContent {

	private int	sessionID;

	private String	participants;

	private String	subject;

	private String	content;

	private String	msgList;

	private int		projectID;

	public String getMsgList() {
		return msgList;
	}

	public void setMsgList(String msgList) {
		this.msgList = msgList;
	}

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public String getParticipants() {
		return participants;
	}

	public void setParticipants(String participants) {
		this.participants = participants;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String toString() {
		String ret = "";
		ret += "------------------------------------\n";
		ret += "sessionID :" + this.sessionID + "\n";
		ret += "subject :" + this.subject + "\n";
		ret += "participants :" + this.participants + "\n";
		ret += "content length :" + this.content.length() + "\n";
		ret += "content : " + this.content + "\n";
		ret += "------------------------------------\n";
		return ret;
	}
}
