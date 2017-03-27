package extractors.miners.mailqa.entity;

import java.util.Date;

//import extractors.miners.mailqa.dao.MessageDao;
//import extractors.miners.mailqa.dao.SessionDao;

public class Session {

	private int		projectID;
	/**
	 * @fieldName: sessionID ����Ψһ��ʶһ���Ự
	 * @fieldType: String
	 */
	private int	sessionID;

	/**
	 * @fieldName: promoterName �Ự�����ˣ����Ự�ĵ�һ���ʼ� ���ǳ�
	 * @fieldType: String
	 */
	private String	promoterName;

	/**
	 * @fieldName: promoterEmail �Ự�����˵��ʼ���ַ
	 * @fieldType: String
	 */
	private String	promoterEmail;

	private String promoterEmailID;

	/**
	 * @fieldName: startTime �Ự�ķ���ʱ�� ����һ���ʼ��ķ���ʱ��
	 * @fieldType: String
	 */
	private String	startTime;

	/**
	 * @fieldName: endTime �Ự�Ľ���ʱ�䣬�����һ���ʼ��ķ���ʱ��
	 * @fieldType: String
	 */
	private String	endTime;

	/**
	 * @fieldName: participant ����Ự�����г�Ա �˴���������ʼ��б����� �Զ��ŷָ���
	 *             <jinyonghorse@hotmail.com>,<jinyong1112@126.com>
	 * @fieldType: String
	 */
	private String	participants;

	/**
	 * @fieldName: subject ��ǰsession����ڵ����� TODO ��ʱδʵ�֣���ʵ��
	 * @fieldType: String
	 */
	private String	subject;

	/**
	 * @fieldName: msgList �Ự����email�б?�˴��洢����messageID����
	 *             ���message�Զ��ŷָ�
	 * @fieldType: String
	 */
	private String	msgList;

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public String getPromoterName() {
		return promoterName;
	}

	public void setPromoterName(String promoterName) {
		this.promoterName = promoterName;
	}

	public String getPromoterEmail() {
		return promoterEmail;
	}

	public void setPromoterEmail(String promoterEmail) {
		this.promoterEmail = promoterEmail;
	}

	public String getPromoterEmailID() {
		return promoterEmailID;
	}

	public void setPromoterEmailID(String promoterEmailID) {
		this.promoterEmailID = promoterEmailID;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
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

	public String toString() {
		String ret = "";
		ret += "------------------------------------\n";
		ret += "proejctID :" + this.projectID + "\n";
		ret += "sessionID :" + this.sessionID + "\n";
		ret += "promoterName :" + this.promoterName + "\n";
		ret += "promoterEmail :" + this.promoterEmail + "\n";
		ret += "subject :" + this.subject + "\n";
		ret += "startTime :" + this.startTime + "\n";
		ret += "endTime :" + this.endTime + "\n";
		ret += "msgList" + this.msgList + "\n";
		ret += "------------------------------------\n";
		return ret;
	}

	/*
	public static Email getPromoter(Session s) {
		Email e = new Email();

		MessageDao md = new MessageDao();
		String[] msgIds = s.getMsgList().split(",");
		for (String id : msgIds) {
			Email tmp = md.getEmailByMessageId(id);
			if (tmp != null && tmp.getSubject().trim().equals(s.getSubject().trim())) {
				if (tmp.getInReplyTo() == null || tmp.getInReplyTo().trim().length() == 0) {
					e = tmp;
					break;
				}
			}
		}

		return e;
	}

	public static void main(String args[]) {
		SessionDao sd = new SessionDao();
		Session s = sd.getSessionById(12);
		Email e = getPromoter(s);
		System.out.println(e);
	}*/
}
