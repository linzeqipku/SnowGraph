package extractors.miners.mailqa.entity;

public class QuestionAnswer {

	public int		id;

	public int		session_id;

	public String	session_uuid;

	public int		message_id;

	public String	message_uuid;

	public String	question;

	public int		answer_message_id;

	public String	answer_message_uuid;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getAnswer_message_id() {
		return answer_message_id;
	}

	public void setAnswer_message_id(int answer_message_id) {
		this.answer_message_id = answer_message_id;
	}

	public String getAnswer_message_uuid() {
		return answer_message_uuid;
	}

	public void setAnswer_message_uuid(String answer_message_uuid) {
		this.answer_message_uuid = answer_message_uuid;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("####################################\n");
		sb.append("session_id: " + this.session_id + "\n");
		sb.append("session_uuid: " + this.session_uuid + "\n");
		sb.append("message_id: " + this.message_id + "\n");
		sb.append("message_uuid: " + this.message_uuid + "\n");
		sb.append("question sentence: " + this.question + "\n");
		sb.append("answer_message_id: " + this.answer_message_id + "\n");
		sb.append("answer_message_uuid: " + this.answer_message_uuid + "\n");
		sb.append("####################################\n");

		return sb.toString();
	}
}
