package discretgraphs.qa;

import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;
import graphmodel.entity.qa.QaCommentSchema;
import graphmodel.entity.qa.QaUserSchema;
import graphmodel.entity.qa.QuestionSchema;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import discretgraphs.GraphBuilder;

/**
 * 从StackOverflow的XML文件数据中生成图数据
 * @author Zeqi Lin
 *
 */

public class QaGraphDbBuilder extends GraphBuilder
{

	private GraphDatabaseService db = null;
	private Map<Integer, QuestionSchema> questionMap = new HashMap<Integer, QuestionSchema>();
	private Map<Integer, AnswerSchema> answerMap = new HashMap<Integer, AnswerSchema>();
	private Map<Integer, QaCommentSchema> commentMap = new HashMap<Integer, QaCommentSchema>();
	private Map<Integer, QaUserSchema> userMap = new HashMap<Integer,QaUserSchema>();
	private List<Pair<Integer,Integer>> duplicateLinkList = new ArrayList<>();
	
	String questionXmlPath=null,answerXmlPath=null,commentXmlPath=null,userXmlPath=null,postLinkXmlPath=null;

	public QaGraphDbBuilder(String dbPath, String questionXmlPath, String answerXmlPath, String commentXmlPath, 
			                               String userXmlPath, String postLinkXmlPath){
		super(dbPath);
		this.questionXmlPath=questionXmlPath;
		this.answerXmlPath=answerXmlPath;
		this.commentXmlPath=commentXmlPath;
		this.userXmlPath=userXmlPath;
		this.postLinkXmlPath=postLinkXmlPath;
		
		name="QaGraphBuilder";
	}
	
	public static void main(String[] args) {
		String projectName = "lucene";
		String dbPath = "data/" + projectName + "/graphdb/qa";
		String folderPath = "data/" + projectName + "/source_data/qa";
		String questionXmlPath = folderPath + "/Questions.xml";
		String answerXmlPath = folderPath + "/Answers.xml";
		String commentXmlPath = folderPath + "/Comments.xml";
		String userXmlPath = folderPath + "/Users.xml";
		String postLinkXmlPath = folderPath + "/PostLinks.xml";
		
		QaGraphDbBuilder builder = new QaGraphDbBuilder(dbPath,questionXmlPath,answerXmlPath,commentXmlPath,userXmlPath,postLinkXmlPath);
		builder.run();
	}
	
	@Override
	public void run(){
		
		//delete existed database before creating a new one
		File dbFile = new File(dbPath);
		if(dbFile.exists()){
			dbFile.delete();
		}
		db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		SAXParser qParser = null;
		SAXParser aParser = null;
		SAXParser cParser = null;
		SAXParser uParser = null;
		SAXParser plParser = null;
		
		try
		{
			qParser = SAXParserFactory.newInstance().newSAXParser();
			aParser = SAXParserFactory.newInstance().newSAXParser();
			cParser = SAXParserFactory.newInstance().newSAXParser();
			uParser = SAXParserFactory.newInstance().newSAXParser();
			plParser = SAXParserFactory.newInstance().newSAXParser();
		}
		catch (ParserConfigurationException | SAXException e)
		{
			e.printStackTrace();
		}
		
		try (Transaction tx = db.beginTx())
		{
			
			//生成图数据结点，并记录在questionMap, answerMap和commentMap中.
			
			QuestionHandler qHandler = new QuestionHandler(db, questionMap);
			AnswerHandler aHandler = new AnswerHandler(db, answerMap);
			QaCommentHandler cHandler = new QaCommentHandler(db, commentMap);
			QaUserHandler uHandler = new QaUserHandler(db, userMap);
			PostLinkHandler plHandler = new PostLinkHandler(duplicateLinkList);
			try
			{
				qParser.parse(new File(questionXmlPath), qHandler);
				aParser.parse(new File(answerXmlPath), aHandler);
				cParser.parse(new File(commentXmlPath), cHandler);
				uParser.parse(new File(userXmlPath), uHandler);
				plParser.parse(new File(postLinkXmlPath), plHandler);
			}
			catch (SAXException | IOException e)
			{
				e.printStackTrace();
			}
			tx.success();
		}
		
		try (Transaction tx = db.beginTx())
		{
			
			//建立QA结点之间的关联关系
			
			for (AnswerSchema answerSchema:answerMap.values()){
				Node answerNode=answerSchema.getNode();
				QuestionSchema questionSchema=questionMap.get(answerSchema.getParentQuestionId());
				if (questionSchema!=null)
					questionSchema.getNode().createRelationshipTo(answerNode, ManageElements.RelTypes.HAVE_ANSWER);
			}
			for (QaCommentSchema commentSchema:commentMap.values()){
				Node commentNode=commentSchema.getNode();
				QuestionSchema questionSchema=questionMap.get(commentSchema.getParentId());
				if (questionSchema!=null)
					questionSchema.getNode().createRelationshipTo(commentNode, ManageElements.RelTypes.HAVE_QA_COMMENT);
				AnswerSchema answerSchema=answerMap.get(commentSchema.getParentId());
				if (answerSchema!=null)
					answerSchema.getNode().createRelationshipTo(commentNode, ManageElements.RelTypes.HAVE_QA_COMMENT);
			}
			
			//建立Question/Answer/Comment到User结点之间的关联关系
			for(QuestionSchema questionSchema: questionMap.values()){
				int userId = questionSchema.getOwnerUserId();
				QaUserSchema userSchema = userMap.get(userId);
				if(userSchema != null){
					userSchema.getNode().createRelationshipTo(questionSchema.getNode(),ManageElements.RelTypes.AUTHOR);
//					System.out.printf("Create Author relationship from User(%d) --> Question(%d)\n",userSchema.getUserId(),questionSchema.getQuestionId());
				}
			}
			
			for(AnswerSchema answerSchema: answerMap.values()){
				int userId = answerSchema.getOwnerUserId();
				QaUserSchema userSchema = userMap.get(userId);
				if(userSchema != null){
					userSchema.getNode().createRelationshipTo(answerSchema.getNode(), ManageElements.RelTypes.AUTHOR);
//					System.out.printf("Create Author relationship from User(%d) --> Answer(%d)\n",userSchema.getUserId(),answerSchema.getAnswerId());
				}
			}
			
			for(QaCommentSchema commentSchema: commentMap.values()){
				int userId = commentSchema.getUserId();
				QaUserSchema userSchema = userMap.get(userId);
				if(userSchema != null){
					userSchema.getNode().createRelationshipTo(commentSchema.getNode(), ManageElements.RelTypes.AUTHOR);
//					System.out.printf("Create Author relationship from User(%d) --> Comment(%d)\n",userSchema.getUserId(),commentSchema.getCommentId());
				}
			}
			
			//建立Question之间的Duplicate关系, postId -[:DUPLICATE]-> relatedPostId
			for(Pair<Integer,Integer> dupLink: duplicateLinkList){
				int postId = dupLink.getLeft();
				int relatedPostId = dupLink.getRight();
				
				QuestionSchema post = questionMap.get(postId);
				QuestionSchema relatedPost = questionMap.get(relatedPostId);
				if(post != null && relatedPost != null){
					post.getNode().createRelationshipTo(relatedPost.getNode(), ManageElements.RelTypes.DUPLICATE);
//					System.out.printf("Create Duplicate relationship from Question(%d) --> Question(%d)\n",post.getQuestionId(),relatedPost.getQuestionId());
				}
			}
			
			tx.success();
		}
		
		try (Transaction tx = db.beginTx())
		{
			
			//标记Answer结点是否是被接受的结点
			
			for (QuestionSchema questionSchema:questionMap.values()){
				int acceptedAnswerId=questionSchema.getAcceptedAnswerId();
				AnswerSchema answerSchema=answerMap.get(acceptedAnswerId);
				if (answerSchema!=null)
					answerSchema.setAccepted(true);
			}
			tx.success();
		}
		
		db.shutdown();
	}

}

class QuestionHandler extends DefaultHandler
{

	private GraphDatabaseService db = null;
	private Map<Integer, QuestionSchema> questionMap = null;

	public QuestionHandler(GraphDatabaseService db, Map<Integer, QuestionSchema> questionMap)
	{
		super();
		this.db = db;
		this.questionMap = questionMap;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (!qName.equals("row"))
			return;
		int id = Integer.parseInt(attributes.getValue("Id"));
		String creationDate = attributes.getValue("CreationDate");
		int score = Integer.parseInt(attributes.getValue("Score"));
		int viewCount = Integer.parseInt(attributes.getValue("ViewCount"));
		String body = attributes.getValue("Body");
		String ownerUserIdString=attributes.getValue("OwnerUserId");
		if (ownerUserIdString==null)
			ownerUserIdString="-1";
		int ownerUserId = Integer.parseInt(ownerUserIdString);
		String title = attributes.getValue("Title");
		String tags = attributes.getValue("Tags");
		String acceptedAnswerIdString=attributes.getValue("AcceptedAnswerId");
		if (acceptedAnswerIdString==null)
			acceptedAnswerIdString="-1";
		int acceptedAnswerId = Integer.parseInt(acceptedAnswerIdString);

		Node node = db.createNode();
		QuestionSchema questionSchema = new QuestionSchema(node, id, creationDate, score, viewCount, body, ownerUserId, title, tags, acceptedAnswerId);
		questionMap.put(id, questionSchema);
	}

}

class AnswerHandler extends DefaultHandler
{

	private GraphDatabaseService db = null;
	private Map<Integer, AnswerSchema> answerMap = null;

	public AnswerHandler(GraphDatabaseService db, Map<Integer, AnswerSchema> answerMap)
	{
		super();
		this.db = db;
		this.answerMap = answerMap;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (!qName.equals("row"))
			return;
		int id = Integer.parseInt(attributes.getValue("Id"));
		int parentId = Integer.parseInt(attributes.getValue("ParentId"));
		String creationDate = attributes.getValue("CreationDate");
		int score = Integer.parseInt(attributes.getValue("Score"));
		String body = attributes.getValue("Body");
		String ownerUserIdString=attributes.getValue("OwnerUserId");
		if (ownerUserIdString==null)
			ownerUserIdString="-1";
		int ownerUserId = Integer.parseInt(ownerUserIdString);

		Node node = db.createNode();
		AnswerSchema answerSchema = new AnswerSchema(node, id, parentId, creationDate, score, body, ownerUserId);
		answerMap.put(id, answerSchema);
	}

}

class QaCommentHandler extends DefaultHandler
{

	private GraphDatabaseService db = null;
	private Map<Integer, QaCommentSchema> commentMap = null;

	public QaCommentHandler(GraphDatabaseService db, Map<Integer, QaCommentSchema> commentMap)
	{
		super();
		this.db = db;
		this.commentMap = commentMap;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (!qName.equals("row"))
			return;
		int id = Integer.parseInt(attributes.getValue("Id"));
		int postId = Integer.parseInt(attributes.getValue("PostId"));
		int score = Integer.parseInt(attributes.getValue("Score"));
		String text = attributes.getValue("Text");
		String creationDate = attributes.getValue("CreationDate");
		String userIdString=attributes.getValue("UserId");
		if (userIdString==null)
			userIdString="-1";
		int userId = Integer.parseInt(userIdString);

		Node node = db.createNode();
		QaCommentSchema commentSchema = new QaCommentSchema(node, id, postId, score, text, creationDate, userId);
		commentMap.put(id, commentSchema);
	}

}


class QaUserHandler extends DefaultHandler{
	private GraphDatabaseService db = null;
	private Map<Integer,QaUserSchema> userMap = null;
	
	public QaUserHandler(GraphDatabaseService db, Map<Integer,QaUserSchema> userMap){
		super();
		this.db = db;
		this.userMap = userMap;
	}
	
	/*
	 * 对于以"<row" 开头的记录，即User记录，创建User 结点，并将(userId,schema)放置userMap；
	 * 对于其它记录，忽略该记录。
	 * 
	 * 一条User记录样例如下：
	 * 	 <row Id="-1" Reputation="1" CreationDate="2015-10-26T21:36:24.767" DisplayName="Comunidad" 
	 *        LastAccessDate="2015-10-26T21:36:24.767" WebsiteUrl="" Location="en la granja de servidores" 
	 *        AboutMe="about me" Views="0" UpVotes="0" DownVotes="106" Age="16" AccountId="-1" />
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if (!qName.equals("row"))
			return;
		
		int id = Integer.parseInt(attributes.getValue("Id"));
		int reputation = Integer.parseInt(attributes.getValue("Reputation"));
		String creationDate = attributes.getValue("CreationDate");
		String displayName = attributes.getValue("DisplayName");
		String lastAccessDate = attributes.getValue("LastAccessDate");
		int views = Integer.parseInt(attributes.getValue("Views"));
		int upVotes = Integer.parseInt(attributes.getValue("UpVotes"));
		int downVotes = Integer.parseInt(attributes.getValue("DownVotes"));
		
		Node node = db.createNode();
		QaUserSchema userSchema = new QaUserSchema(node,id,reputation,creationDate,displayName,lastAccessDate,views,upVotes,downVotes);
		userMap.put(id, userSchema);
	}
}

class PostLinkHandler extends DefaultHandler{
	private List<Pair<Integer,Integer>> dupLinkList = null;
	
	public PostLinkHandler(List<Pair<Integer,Integer>> dupLinkList){
		super();
		this.dupLinkList = dupLinkList;
	}
	
	/*
	 * 对于以"<row" 开头的记录，即PostLink记录，将(postId,relatedPostId)放置dupLinkList；
	 * 对于其它记录，忽略该记录。
	 * 
	 * 一条PostLink样例如下：
	 * 		<row Id="45" CreationDate="2015-11-23T20:08:24.160" PostId="68" RelatedPostId="8" LinkTypeId="3" />
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if (!qName.equals("row"))
			return;
		
		int postId = Integer.parseInt(attributes.getValue("PostId"));
		int relatedPostId = Integer.parseInt(attributes.getValue("RelatedPostId"));
		
		dupLinkList.add(Pair.of(postId, relatedPostId));
//		System.out.printf("Duplicate from %d-->%d.\n",postId,relatedPostId);
	}
}