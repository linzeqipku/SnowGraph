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
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.mail.entity.MailSchema;

public class MboxHandler extends AbstractContentHandler
{

	private GraphDatabaseService db = null;
	MailSchema mailSchema = null;

	public void setDb(GraphDatabaseService db)
	{
		this.db = db;
	}

	public MailSchema getMailSchema()
	{
		return mailSchema;
	}

	String subject = "";
	String id = "";
	String from = "";
	String to = "";
	String date = "";
	String replyTo = "";
	String body = "";
	String senderName = "";
	String senderMail = "";
	String receiverNames[] = new String[]{};
	String receiverMails[] = new String[]{};
	
	@Override
	public void field(Field fieldData) throws MimeException
	{
		if (fieldData.toString().startsWith("Message-ID:") || fieldData.toString().startsWith("Message-Id:"))
		{
			id=fieldData.toString().substring(11).trim();
		}
		if (fieldData.toString().startsWith("Subject:"))
		{
			subject=fieldData.toString().substring(8).trim();
		}
		if (fieldData.toString().startsWith("In-Reply-To:"))
		{
			replyTo=fieldData.toString().substring(12).trim();
		}
		if (fieldData.toString().startsWith("From:"))
		{
			from=fieldData.toString().substring(5).trim();
			Pair<String,String> senderPair = MailUtil.extractMailNameAndAddress(from);
			if(senderPair != null){
				senderName = senderPair.getLeft();
				senderMail = senderPair.getRight();
			}else{//has no mail address, e.g., from="undisclosed-recipients:;"
				senderName = senderMail = from;
			}
		}
		if (fieldData.toString().startsWith("To:"))
		{
			to=fieldData.toString().substring(3).trim();
			
			List<Pair<String,String>> mailPairs = MailUtil.extractMultiMailNameAndAddress(to); 
			int mailNum = mailPairs.size();
			if(mailNum > 0){
				receiverNames = new String[mailNum];
				receiverMails = new String[mailNum];
				
				for(int i=0;i<mailNum;i++){
					Pair<String,String> mailPair = mailPairs.get(i);
					receiverNames[i] = mailPair.getLeft();
					receiverMails[i] = mailPair.getRight();
				}
			}else{//has no mail address, e.g., to="undisclosed-recipients:;"
				receiverNames = new String[]{to};
				receiverMails = new String[]{to}; 
			}
		}
		if (fieldData.toString().startsWith("Date:"))
		{
			date=fieldData.toString().substring(5).trim();
		}
	}

	@Override
	public void body(BodyDescriptor bd, InputStream is) throws MimeException,
			IOException
	{
		String r="";
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
				if (s!=null)
					r+=s;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		body+=r;
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
		Node node=db.createNode();		
		mailSchema=new MailSchema(node, subject, id, senderName, senderMail, receiverNames, receiverMails, replyTo, date, body);
	}

	@Override
	public void raw(InputStream is) throws MimeException
	{
	}

}