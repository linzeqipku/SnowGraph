package graphmodel.entity.mail;

import org.neo4j.graphdb.Node;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;

public class MailSchema extends Schema {
	public static String MAIL_ID = "mailId";
	public static String SUBJECT = "subject";
	public static String SENDER_NAME = "senderName";
	public static String SENDER_MAIL = "senderMail";
	public static String RECEIVER_NAMES = "receiverNames";
	public static String RECEIVER_MAILS = "receiverMails";
	public static String DATE = "date";
	public static String BODY = "body";
	
	private String id="";
	private String replyTo="";
	private String senderName="";
	private String senderMail="";
	private String[] receiverNames = new String[]{};
	private String[] receiverMails = new String[]{};
	
	public MailSchema(Node node, String subject, String id, String senderName, String senderMail, 
			                     String[] receiverNames, String[] receiverMails, String replyTo, String date, String body){
		this.node=node;
		this.id=id;
		this.replyTo=replyTo;
		this.senderName = senderName;
		this.senderMail = senderMail;
		this.receiverNames = receiverNames;
		this.receiverMails = receiverMails;
		
		node.addLabel(ManageElements.Labels.MAIL);

		node.setProperty(SUBJECT, subject);
		node.setProperty(MAIL_ID, id);
		node.setProperty(SENDER_NAME, senderName);
		node.setProperty(SENDER_MAIL, senderMail);
		node.setProperty(RECEIVER_NAMES, receiverNames);
		node.setProperty(RECEIVER_MAILS, receiverMails);
		node.setProperty(DATE, date);
		node.setProperty(BODY, body);
	}
	
	public String getId(){
		return id;
	}
	
	public String getReplyTo(){
		return replyTo;
	}

	public String getSenderName() {
		return senderName;
	}

	public String getSenderMail() {
		return senderMail;
	}

	public String[] getReceiverNames() {
		return receiverNames;
	}

	public String[] getReceiverMails() {
		return receiverMails;
	}
	
	@Override 
	public String toString(){
		StringBuilder schemaBuilder = new StringBuilder();
		schemaBuilder.append(SUBJECT).append(":").append(node.getProperty(SUBJECT)).append("\n")
					.append(MAIL_ID).append(":").append(node.getProperty(MAIL_ID)).append("\n")
					.append(SENDER_NAME).append(":").append(node.getProperty(SENDER_NAME)).append("\n")
					.append(SENDER_MAIL).append(":").append(node.getProperty(SENDER_MAIL)).append("\n")
					.append(RECEIVER_NAMES).append(":").append(node.getProperty(RECEIVER_NAMES)).append("\n")
					.append(RECEIVER_MAILS).append(":").append(node.getProperty(RECEIVER_MAILS)).append("\n")
					.append(DATE).append(":").append(node.getProperty(DATE)).append("\n")
					.append(BODY).append(":").append(node.getProperty(BODY).hashCode());
		return schemaBuilder.toString();
	}
}
