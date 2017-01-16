package extractors.miners.mailqa.entity;

public class Document {

	private int		id;

	private String	qmail_uuid		= "";

	private String	amail_uuid		= "";

	private String	question		= "";

	private int		question_seg_no	= -1;

	private int	session_uuid	;
	
	private String keywords = "";

	
	public String getKeywords() {
		return keywords;
	}

	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQmail_uuid() {
		return qmail_uuid;
	}

	public void setQmail_uuid(String qmail_uuid) {
		this.qmail_uuid = qmail_uuid;
	}

	public String getAmail_uuid() {
		return amail_uuid;
	}

	public void setAmail_uuid(String amail_uuid) {
		this.amail_uuid = amail_uuid;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getQuestion_seg_no() {
		return question_seg_no;
	}

	public void setQuestion_seg_no(int question_seg_no) {
		this.question_seg_no = question_seg_no;
	}

	public int getSession_uuid() {
		return session_uuid;
	}

	public void setSession_uuid(int session_uuid) {
		this.session_uuid = session_uuid;
	}

	public String toString() {
		String ret = "";
		ret += "------------------------------\n";
		ret += "question mail id: " + this.getQmail_uuid() + "\n";
		ret += "question :" + this.question + "\n";
		ret += "answer mail id :" + this.amail_uuid + "\n";
		ret += "------------------------------\n";
		return ret;
	}

}
