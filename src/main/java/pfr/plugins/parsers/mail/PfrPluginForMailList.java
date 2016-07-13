package pfr.plugins.parsers.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.MimeConfig;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import pfr.PFR;
import pfr.annotations.RelationDeclaration;
import pfr.plugins.parsers.mail.entity.MailSchema;
import pfr.plugins.parsers.mail.entity.MailUserSchema;
import pfr.plugins.parsers.mail.utils.CharBufferWrapper;
import pfr.plugins.parsers.mail.utils.MboxHandler;
import pfr.plugins.parsers.mail.utils.MboxIterator;

public class PfrPluginForMailList implements PFR {
	
	@RelationDeclaration public static final String MAIL_IN_REPLY_TO="mailInReplyTo";
	@RelationDeclaration public static final String MAIL_SENDER="mailSender";
	@RelationDeclaration public static final String MAIL_RECEIVER="mailReceiver";

	private Map<String,MailSchema> mailMap = new HashMap<String,MailSchema>();
	private Map<String,MailUserSchema> mailUserMap = new HashMap<>();
	private Map<String,String> mailAddrToMailNameMap = new HashMap<>();
	
	public String mboxPath=null;
	private static Charset charset = Charset.forName("UTF-8");
	private final static CharsetDecoder DECODER = charset.newDecoder();
	private int toSystemMailCount = 0;
	
	public void setMboxPath(String path) {
		this.mboxPath=path;
	}
	
	public void run(GraphDatabaseService db){
		try (Transaction tx = db.beginTx()) {
			parse(db, new File(mboxPath));
			for(MailSchema mailSchema: mailMap.values()){
				String senderName = mailSchema.getSenderName();
				String senderMail = mailSchema.getSenderMail();
				
				if(!mailAddrToMailNameMap.containsKey(senderMail)){
					mailAddrToMailNameMap.put(senderMail, senderName);
				}
				
				String[] receiverNames = mailSchema.getReceiverNames();
				String[] receiverMails = mailSchema.getReceiverMails();
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
				MailUserSchema mailUserSchema = new MailUserSchema(node,mailName,mailAddr);
				mailUserMap.put(mailAddr, mailUserSchema);
			}
			
			buildRelationships();
			tx.success();
		}
		db.shutdown();
		
		System.out.println("Total Mail Count:" + mailMap.size());
		System.out.println("toSystemMailCount:" + toSystemMailCount);
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
			if (myHandler.getMailSchema().getId().length()>0)
				mailMap.put(myHandler.getMailSchema().getId(),myHandler.getMailSchema());
		}
		catch (MimeException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public void buildRelationships() {
		for (String id:mailMap.keySet()){
			MailSchema mailSchema=mailMap.get(id);
			//建立邮件之间的回复关系（MAIL_IN_REPLY_TO）
			if (!mailSchema.getReplyTo().equals("")){
				if (mailMap.containsKey(mailSchema.getReplyTo())){
					mailSchema.getNode().createRelationshipTo(mailMap.get(mailSchema.getReplyTo()).getNode(),ManageElements.RelTypes.MAIL_IN_REPLY_TO);
				}
			}
			
			//建立邮件与发送者之间的关联关系（ 发送者 --MAIL_SENDER--> 邮件）
			String senderMail = mailSchema.getSenderMail();
			if(mailUserMap.containsKey(senderMail)){
				MailUserSchema mailUserSchema = mailUserMap.get(senderMail);
				mailUserSchema.getNode().createRelationshipTo(mailSchema.getNode(), ManageElements.RelTypes.MAIL_SENDER);
//				System.out.printf("%s --MAIL_SENDER--> %s\n", mailUserSchema.getMail(),mailSchema.getId());
			}
			
			//建立邮件与接收者之间的关联关系（邮件 --MAIL_RECEIVER-->接收者）
			String[] receiverMails = mailSchema.getReceiverMails();
			for(String receiverMail: receiverMails){
				if(!mailUserMap.containsKey(receiverMail)){
					continue;
				}
				
				if(receiverMail.equals("lucene-user@jakarta.apache.org") || receiverMail.equals("java-user@lucene.apache.org")){
					toSystemMailCount++;
					
//					//不建立邮件与小组接收者之间的关联关系（MAIL_RECEIVER），原因是几乎全是发给小组接收者的。
//					continue;
				}
				
				MailUserSchema mailUserSchema = mailUserMap.get(receiverMail);
				mailSchema.getNode().createRelationshipTo(mailUserSchema.getNode(), ManageElements.RelTypes.MAIL_RECEIVER);
//				System.out.printf("%s--MAIL_RECEIVER-->%s\n",mailSchema.getId(),mailUserSchema.getMail());
			}
		}
	}
	
}
