package pfr.plugins.parsers.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.MimeConfig;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import pfr.PFR;
import pfr.annotations.ConceptDeclaration;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;
import pfr.plugins.parsers.mail.entity.MailInfo;
import pfr.plugins.parsers.mail.entity.MailUserInfo;
import pfr.plugins.parsers.mail.utils.CharBufferWrapper;
import pfr.plugins.parsers.mail.utils.MboxHandler;
import pfr.plugins.parsers.mail.utils.MboxIterator;

public class PfrPluginForMailList implements PFR {
	
	@ConceptDeclaration public static final String MAIL = "Mail";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_ID = "mailId";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_SUBJECT = "subject";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_SENDER_NAME = "senderName";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_SENDER_MAIL = "senderMail";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_RECEIVER_NAMES = "receiverNames";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_RECEIVER_MAILS = "receiverMails";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_DATE = "date";
	@PropertyDeclaration(parent=MAIL)public static final String MAIL_BODY = "body";
	
	@ConceptDeclaration public static final String MAILUSER = "MailUser";
	@PropertyDeclaration(parent=MAILUSER)public static final String MAILUSER_NAME = "name";
	@PropertyDeclaration(parent=MAILUSER)public static final String MAILUSER_MAIL = "mail";
	
	@RelationDeclaration public static final String MAIL_IN_REPLY_TO="mailInReplyTo";
	@RelationDeclaration public static final String MAIL_SENDER="mailSender";
	@RelationDeclaration public static final String MAIL_RECEIVER="mailReceiver";

	private Map<String,Pair<MailInfo,Node>> mailMap = new HashMap<String,Pair<MailInfo,Node>>();
	private Map<String,Pair<MailUserInfo,Node>> mailUserMap = new HashMap<>();
	private Map<String,String> mailAddrToMailNameMap = new HashMap<>();
	
	public String mboxPath=null;
	private static Charset charset = Charset.forName("UTF-8");
	private final static CharsetDecoder DECODER = charset.newDecoder();
	
	public void setMboxPath(String path) {
		this.mboxPath=path;
	}
	
	public void run(GraphDatabaseService db){
		try (Transaction tx = db.beginTx()) {
			parse(db, new File(mboxPath));
			for(Pair<MailInfo,Node> mailSchema: mailMap.values()){
				String senderName = mailSchema.getLeft().senderName;
				String senderMail = mailSchema.getLeft().senderMail;
				
				if(!mailAddrToMailNameMap.containsKey(senderMail)){
					mailAddrToMailNameMap.put(senderMail, senderName);
				}
				
				String[] receiverNames = mailSchema.getLeft().receiverNames;
				String[] receiverMails = mailSchema.getLeft().receiverMails;
				int len = receiverNames.length;
				for(int i=0;i<len;i++){
					if(!mailAddrToMailNameMap.containsKey(receiverMails[i])){
						mailAddrToMailNameMap.put(receiverMails[i],receiverNames[i]);
					}
				}
			}
			
			for(String mailAddr: mailAddrToMailNameMap.keySet()){
				String mailName = mailAddrToMailNameMap.get(mailAddr);
				Node node = db.createNode();
				MailUserInfo mailUserInfo=new MailUserInfo();
				mailUserInfo.mail=mailAddr;
				mailUserInfo.name=mailName;
				node.addLabel(Label.label(MAILUSER));
				node.setProperty(MAILUSER_NAME, mailName);
				node.setProperty(MAILUSER_MAIL, mailAddr);
				Pair<MailUserInfo, Node> mailUserSchema = new ImmutablePair<MailUserInfo, Node>(mailUserInfo, node);
				mailUserMap.put(mailAddr, mailUserSchema);
			}
			
			buildRelationships();
			tx.success();
		}
	}  

	public void parse(GraphDatabaseService db, File mboxFile){
		if (mboxFile.isDirectory()){
			for (File f:mboxFile.listFiles())
				parse(db,f);
			return;
		}
		try
		{
			for (CharBufferWrapper message : MboxIterator.fromFile(mboxFile).charset(DECODER.charset()).build()){
				parse(db, message);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void parse(GraphDatabaseService db, CharBufferWrapper message){
		MboxHandler myHandler = new MboxHandler();
		myHandler.setDb(db);
		ContentHandler handler = myHandler;
		MimeConfig config = new MimeConfig();
		MimeStreamParser parser = new MimeStreamParser(config);
		parser.setContentHandler(handler);
		try
		{
			parser.parse(new ByteArrayInputStream(message.toString().getBytes()));
			String id=myHandler.getMailInfo().id;
			if (id.length()>0)
				mailMap.put(id,new ImmutablePair<MailInfo, Node>(myHandler.getMailInfo(),myHandler.getMailNode()));
		}
		catch (MimeException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public void buildRelationships() {
		for (String id:mailMap.keySet()){
			Pair<MailInfo, Node> mailSchema=mailMap.get(id);
			//建立邮件之间的回复关系（MAIL_IN_REPLY_TO）
			if (!mailSchema.getLeft().replyTo.equals("")){
				if (mailMap.containsKey(mailSchema.getLeft().replyTo)){
					mailSchema.getRight().createRelationshipTo(mailMap.get(mailSchema.getLeft().replyTo).getRight(),RelationshipType.withName(MAIL_IN_REPLY_TO));
				}
			}
			
			//建立邮件与发送者之间的关联关系（ 发送者 --MAIL_SENDER--> 邮件）
			String senderMail = mailSchema.getLeft().senderMail;
			if(mailUserMap.containsKey(senderMail)){
				Pair<MailUserInfo, Node> mailUserSchema = mailUserMap.get(senderMail);
				mailUserSchema.getRight().createRelationshipTo(mailSchema.getRight(), RelationshipType.withName(MAIL_SENDER));
			}
			
			String[] receiverMails = mailSchema.getLeft().receiverMails;
			for(String receiverMail: receiverMails){
				if(!mailUserMap.containsKey(receiverMail)){
					continue;
				}
				
				Pair<MailUserInfo, Node> mailUserSchema = mailUserMap.get(receiverMail);
				mailSchema.getRight().createRelationshipTo(mailUserSchema.getRight(), RelationshipType.withName(MAIL_RECEIVER));
			}
		}
	}
	
}
