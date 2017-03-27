package extractors.miners.mailqa.entity;

public class Seg {

	private int		id;

	private String	message_uuid	= "";

	private int	session_uuid;

	private int		segment_no;

	private int		segment_type	= 0;

	private String	content			= "";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMessage_uuid() {
		return message_uuid;
	}

	public void setMessage_uuid(String message_uuid) {
		this.message_uuid = message_uuid;
	}

	public int getSession_uuid() {
		return session_uuid;
	}

	public void setSession_uuid(int session_uuid) {
		this.session_uuid = session_uuid;
	}

	public int getSegment_no() {
		return segment_no;
	}

	public void setSegment_no(int segment_no) {
		this.segment_no = segment_no;
	}

	public int getSegment_type() {
		return segment_type;
	}

	public void setSegment_type(int segment_type) {
		this.segment_type = segment_type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
