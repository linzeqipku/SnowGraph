package extractors.miners.mailqa.entity;

public class Tag {

	public int		session_id		= 0;

	public String	session_uuid	= "";

	public int		message_id		= 0;

	public String	message_uuid	= "";

	public String	raw_text		= "";

	public String	tagged_text		= "";

	public String	subject			= "";

	public String	content			= "";

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getSession_id() {
		return session_id;
	}

	public void setSession_id(int session_id) {
		this.session_id = session_id;
	}

	public String getSession_uuid() {
		return session_uuid;
	}

	public void setSession_uuid(String session_uuid) {
		this.session_uuid = session_uuid;
	}

	public int getMessage_id() {
		return message_id;
	}

	public void setMessage_id(int message_id) {
		this.message_id = message_id;
	}

	public String getMessage_uuid() {
		return message_uuid;
	}

	public void setMessage_uuid(String message_uuid) {
		this.message_uuid = message_uuid;
	}

	public String getRaw_text() {
		return raw_text;
	}

	public void setRaw_text(String raw_text) {
		this.raw_text = raw_text;
	}

	public String getTagged_text() {
		return tagged_text;
	}

	public void setTagged_text(String tagged_text) {
		this.tagged_text = tagged_text;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#########################" + "\n");
		sb.append("tagged item :" + "\n");
		sb.append("session_id :" + this.getSession_id() + "\n");
		sb.append("session_uuid:" + this.getSession_uuid() + "\n");
		sb.append("message_id:" + this.getMessage_id() + "\n");
		sb.append("message_uuid:" + this.getMessage_uuid() + "\n");
		sb.append("subject :" + this.getSubject() + "\n");
		sb.append("#########################" + "\n");
		return sb.toString();
	}
}
