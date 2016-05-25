package graphmodel.entity.mail;

import org.neo4j.graphdb.Node;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;

/*
 * Email 用户（邮件发送者，邮件接收者）的信息：
 * 		用户昵称
 *  	邮件地址
 *  如："From: Scott Ganyo <scott.ganyo@eTapestry.com>"中用户昵称为"Scott Ganyo"，
 *  												        邮件地址为"scott.ganyo@eTapestry.com"
 */
public class MailUserSchema extends Schema{
	private static final String NAME = "name";
	private static final String MAIL = "mail";
	
	private String name;
	private String mail;
	
	public MailUserSchema(Node node, String name, String mail){
		this.node = node;
		this.name = name;
		this.mail = mail;
		
		node.addLabel(ManageElements.Labels.MAIL_USER);
		node.setProperty(NAME, name);
		node.setProperty(MAIL, mail);
	}
	
	public MailUserSchema(Node node){
		this.node = node;
		
		this.name = node.getProperty(NAME).toString();
		this.mail = node.getProperty(MAIL).toString();
	}

	public String getName() {
		return name;
	}

	public String getMail() {
		return mail;
	}
}
