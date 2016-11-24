package pfr.plugins.parsers.mail.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.mail.PfrPluginForMailList;
import pfr.plugins.parsers.mail.entity.MailInfo;

public class MboxHandler extends AbstractContentHandler
{

	private GraphDatabaseService db = null;
	Node mailNode = null;
	MailInfo mailInfo=new MailInfo();

	public void setDb(GraphDatabaseService db)
	{
		this.db = db;
	}

	public Node getMailNode()
	{
		return mailNode;
	}
	
	public MailInfo getMailInfo(){
		return mailInfo;
	}

	@Override
	public void field(Field fieldData) throws MimeException
	{
		if (fieldData.toString().startsWith("Message-ID:") || fieldData.toString().startsWith("Message-Id:"))
		{
			mailInfo.id = fieldData.toString().substring(11).trim();
		}
		if (fieldData.toString().startsWith("Subject:"))
		{
			mailInfo.subject = fieldData.toString().substring(8).trim();
		}
		if (fieldData.toString().startsWith("In-Reply-To:"))
		{
			mailInfo.replyTo = fieldData.toString().substring(12).trim();
		}
		if (fieldData.toString().startsWith("From:"))
		{
			mailInfo.from = fieldData.toString().substring(5).trim();
			Pair<String, String> senderPair = MailUtil.extractMailNameAndAddress(mailInfo.from);
			if (senderPair != null)
			{
				mailInfo.senderName = senderPair.getLeft();
				mailInfo.senderMail = senderPair.getRight();
			}
			else
			{// has no mail address, e.g., from="undisclosed-recipients:;"
				mailInfo.senderName = mailInfo.senderMail = mailInfo.from;
			}
		}
		if (fieldData.toString().startsWith("To:"))
		{
			mailInfo.to = fieldData.toString().substring(3).trim();

			List<Pair<String, String>> mailPairs = MailUtil.extractMultiMailNameAndAddress(mailInfo.to);
			int mailNum = mailPairs.size();
			if (mailNum > 0)
			{
				mailInfo.receiverNames = new String[mailNum];
				mailInfo.receiverMails = new String[mailNum];

				for (int i = 0; i < mailNum; i++)
				{
					Pair<String, String> mailPair = mailPairs.get(i);
					mailInfo.receiverNames[i] = mailPair.getLeft();
					mailInfo.receiverMails[i] = mailPair.getRight();
				}
			}
			else
			{// has no mail address, e.g., to="undisclosed-recipients:;"
				mailInfo.receiverNames = new String[] { mailInfo.to };
				mailInfo.receiverMails = new String[] { mailInfo.to };
			}
		}
		if (fieldData.toString().startsWith("Date:"))
		{
			mailInfo.date = fieldData.toString().substring(5).trim();
		}
	}

	@Override
	public void body(BodyDescriptor bd, InputStream is) throws MimeException,
			IOException
	{
		String r = "";
		byte[] buffer = new byte[200];
		String s = null;
		int len;
		try
		{
			while ((len = is.read(buffer)) != -1)
			{
				if (len != 200)
				{
					byte buffer2[] = new byte[len];
					for (int i = 0; i < len; i++)
					{
						buffer2[i] = buffer[i];
					}
					s = new String(buffer2);
				}
				else
				{
					s = new String(buffer);
				}
				if (s != null)
					r += s;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mailInfo.body += r;
	}

	@Override
	public void startMultipart(BodyDescriptor bd) throws MimeException
	{
	}

	@Override
	public void endMultipart() throws MimeException
	{
	}

	@Override
	public void epilogue(InputStream is) throws MimeException
	{
	}

	@Override
	public void preamble(InputStream is) throws MimeException
	{
	}

	@Override
	public void startHeader() throws MimeException
	{
	}

	@Override
	public void endHeader() throws MimeException
	{
	}

	@Override
	public void startBodyPart() throws MimeException
	{
	}

	@Override
	public void endBodyPart() throws MimeException
	{
	}

	@Override
	public void startMessage() throws MimeException
	{
	}

	@Override
	public void endMessage() throws MimeException
	{
		Node node = db.createNode();
		mailNode=node;
		createMailNode(node, mailInfo.subject, mailInfo.id, mailInfo.senderName, mailInfo.senderMail, mailInfo.receiverNames, mailInfo.receiverMails, mailInfo.replyTo, mailInfo.date, mailInfo.body);
	}

	@Override
	public void raw(InputStream is) throws MimeException
	{
	}

	public static void createMailNode(Node node, String subject, String id, String senderName, String senderMail,
			String[] receiverNames, String[] receiverMails, String replyTo, String date, String body)
	{
		node.addLabel(Label.label(PfrPluginForMailList.MAIL));
		node.setProperty(PfrPluginForMailList.MAIL_SUBJECT, subject);
		node.setProperty(PfrPluginForMailList.MAIL_ID, id);
		node.setProperty(PfrPluginForMailList.MAIL_SENDER_NAME, senderName);
		node.setProperty(PfrPluginForMailList.MAIL_SENDER_MAIL, senderMail);
		node.setProperty(PfrPluginForMailList.MAIL_RECEIVER_NAMES, String.join(", ", receiverNames));
		node.setProperty(PfrPluginForMailList.MAIL_RECEIVER_MAILS, String.join(", ", receiverMails));
		node.setProperty(PfrPluginForMailList.MAIL_DATE, date);
		node.setProperty(PfrPluginForMailList.MAIL_BODY, body);
	}

}