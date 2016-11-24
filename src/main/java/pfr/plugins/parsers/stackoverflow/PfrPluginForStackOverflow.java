package pfr.plugins.parsers.stackoverflow;

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
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pfr.PFR;
import pfr.annotations.ConceptDeclaration;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;
import pfr.plugins.parsers.stackoverflow.entity.AnswerInfo;
import pfr.plugins.parsers.stackoverflow.entity.QaCommentInfo;
import pfr.plugins.parsers.stackoverflow.entity.QaUserInfo;
import pfr.plugins.parsers.stackoverflow.entity.QuestionInfo;

/**
 * 从StackOverflow的XML文件数据中生成图数据
 * @author Zeqi Lin
 *
 */

public class PfrPluginForStackOverflow implements PFR
{
	
	@ConceptDeclaration public static final String QUESTION="StackOverflowQuestion";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_ID="questionId";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_CREATION_DATE="creationDate";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_SCORE="score";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_VIEW_COUNT="viewCount";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_BODY="body";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_OWNER_USER_ID="ownerUserId";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_TITLE="title";
	@PropertyDeclaration(parent=QUESTION)public static final String QUESTION_TAGS="tags";
	
	@ConceptDeclaration public static final String ANSWER="StackOverflowAnswer";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_ACCEPTED="accepted";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_ID="answerId";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_PARENT_QUESTION_ID="parentQuestionId";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_CREATION_DATE="creationDate";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_SCORE="score";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_BODY="body";
	@PropertyDeclaration(parent=ANSWER)public static final String ANSWER_OWNER_USER_ID="ownerUserId";
	
	@ConceptDeclaration public static final String COMMENT="StackOverflowComment";
	@PropertyDeclaration(parent=COMMENT)public static final String COMMENT_ID="commentId";
	@PropertyDeclaration(parent=COMMENT)public static final String COMMENT_PARENT_ID="parentId";
	@PropertyDeclaration(parent=COMMENT)public static final String COMMENT_SCORE="score";
	@PropertyDeclaration(parent=COMMENT)public static final String COMMENT_TEXT="text";
	@PropertyDeclaration(parent=COMMENT)public static final String COMMENT_CREATION_DATE="creationDate";
	@PropertyDeclaration(parent=COMMENT)public static final String COMMENT_USER_ID="userId";
	
	@ConceptDeclaration public static final String USER="StackOverflowUser";
	@PropertyDeclaration(parent=USER)public static final String USER_ID = "user_id";
	@PropertyDeclaration(parent=USER)public static final String USER_REPUTATION = "reputation";
	@PropertyDeclaration(parent=USER)public static final String USER_CREATION_DATE = "creationDate";
	@PropertyDeclaration(parent=USER)public static final String USER_DISPLAY_NAME = "displayName";
	@PropertyDeclaration(parent=USER)public static final String USER_LAST_ACCESS_dATE = "lastAccessDate";
	@PropertyDeclaration(parent=USER)public static final String USER_VIEWS = "views";
	@PropertyDeclaration(parent=USER)public static final String USER_UP_VOTES = "upVotes";
	@PropertyDeclaration(parent=USER)public static final String USER_DOWN_VOTES = "downVotes";
	
	@RelationDeclaration public static final String HAVE_ANSWER="haveSoAnswer";
	@RelationDeclaration public static final String HAVE_COMMENT="haveSoComment";
	@RelationDeclaration public static final String AUTHOR="soAuthor";
	@RelationDeclaration public static final String DUPLICATE="soDuplicate";

	String folderPath=null;
	private Map<Integer, QuestionInfo> questionMap = new HashMap<Integer, QuestionInfo>();
	private Map<Integer, AnswerInfo> answerMap = new HashMap<Integer, AnswerInfo>();
	private Map<Integer, QaCommentInfo> commentMap = new HashMap<Integer, QaCommentInfo>();
	private Map<Integer, QaUserInfo> userMap = new HashMap<Integer,QaUserInfo>();
	private List<Pair<Integer,Integer>> duplicateLinkList = new ArrayList<>();
	
	String questionXmlPath=null,answerXmlPath=null,commentXmlPath=null,userXmlPath=null,postLinkXmlPath=null;
	
	public void setFolderPath(String path){
		this.folderPath=path;
		this.questionXmlPath=folderPath+"/Questions.xml";
		this.answerXmlPath=folderPath+"/Answers.xml";
		this.commentXmlPath=folderPath+"/Comments.xml";
		this.userXmlPath=folderPath+"/Users.xml";
		this.postLinkXmlPath=folderPath+"/PostLinks.xml";
	}

	public void run(GraphDatabaseService db){
		
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
			
			for (AnswerInfo answerInfo:answerMap.values()){
				Node answerNode=answerInfo.getNode();
				QuestionInfo questionInfo=questionMap.get(answerInfo.getParentQuestionId());
				if (questionInfo!=null)
					questionInfo.getNode().createRelationshipTo(answerNode, RelationshipType.withName(HAVE_ANSWER));
			}
			for (QaCommentInfo commentInfo:commentMap.values()){
				Node commentNode=commentInfo.getNode();
				QuestionInfo questionInfo=questionMap.get(commentInfo.getParentId());
				if (questionInfo!=null)
					questionInfo.getNode().createRelationshipTo(commentNode, RelationshipType.withName(HAVE_COMMENT));
				AnswerInfo answerInfo=answerMap.get(commentInfo.getParentId());
				if (answerInfo!=null)
					answerInfo.getNode().createRelationshipTo(commentNode, RelationshipType.withName(HAVE_COMMENT));
			}
			
			//建立Question/Answer/Comment到User结点之间的关联关系
			for(QuestionInfo questionInfo: questionMap.values()){
				int userId = questionInfo.getOwnerUserId();
				QaUserInfo userInfo = userMap.get(userId);
				if(userInfo != null){
					userInfo.getNode().createRelationshipTo(questionInfo.getNode(),RelationshipType.withName(AUTHOR));
//					System.out.printf("Create Author relationship from User(%d) --> Question(%d)\n",userInfo.getUserId(),questionInfo.getQuestionId());
				}
			}
			
			for(AnswerInfo answerInfo: answerMap.values()){
				int userId = answerInfo.getOwnerUserId();
				QaUserInfo userInfo = userMap.get(userId);
				if(userInfo != null){
					userInfo.getNode().createRelationshipTo(answerInfo.getNode(), RelationshipType.withName(AUTHOR));
//					System.out.printf("Create Author relationship from User(%d) --> Answer(%d)\n",userInfo.getUserId(),answerInfo.getAnswerId());
				}
			}
			
			for(QaCommentInfo commentInfo: commentMap.values()){
				int userId = commentInfo.getUserId();
				QaUserInfo userInfo = userMap.get(userId);
				if(userInfo != null){
					userInfo.getNode().createRelationshipTo(commentInfo.getNode(), RelationshipType.withName(AUTHOR));
//					System.out.printf("Create Author relationship from User(%d) --> Comment(%d)\n",userInfo.getUserId(),commentInfo.getCommentId());
				}
			}
			
			//建立Question之间的Duplicate关系, postId -[:DUPLICATE]-> relatedPostId
			for(Pair<Integer,Integer> dupLink: duplicateLinkList){
				int postId = dupLink.getLeft();
				int relatedPostId = dupLink.getRight();
				
				QuestionInfo post = questionMap.get(postId);
				QuestionInfo relatedPost = questionMap.get(relatedPostId);
				if(post != null && relatedPost != null){
					post.getNode().createRelationshipTo(relatedPost.getNode(),RelationshipType.withName(DUPLICATE));
//					System.out.printf("Create Duplicate relationship from Question(%d) --> Question(%d)\n",post.getQuestionId(),relatedPost.getQuestionId());
				}
			}
			
			tx.success();
		}
		
		try (Transaction tx = db.beginTx())
		{
			
			//标记Answer结点是否是被接受的结点
			
			for (QuestionInfo questionInfo:questionMap.values()){
				int acceptedAnswerId=questionInfo.getAcceptedAnswerId();
				AnswerInfo answerInfo=answerMap.get(acceptedAnswerId);
				if (answerInfo!=null)
					answerInfo.setAccepted(true);
			}
			tx.success();
		}
	}

}

class QuestionHandler extends DefaultHandler
{

	private GraphDatabaseService db = null;
	private Map<Integer, QuestionInfo> questionMap = null;

	public QuestionHandler(GraphDatabaseService db, Map<Integer, QuestionInfo> questionMap)
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
		QuestionInfo questionInfo = new QuestionInfo(node, id, creationDate, score, viewCount, body, ownerUserId, title, tags, acceptedAnswerId);
		questionMap.put(id, questionInfo);
	}

}

class AnswerHandler extends DefaultHandler
{

	private GraphDatabaseService db = null;
	private Map<Integer, AnswerInfo> answerMap = null;

	public AnswerHandler(GraphDatabaseService db, Map<Integer, AnswerInfo> answerMap)
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
		AnswerInfo answerInfo = new AnswerInfo(node, id, parentId, creationDate, score, body, ownerUserId);
		answerMap.put(id, answerInfo);
	}

}

class QaCommentHandler extends DefaultHandler
{

	private GraphDatabaseService db = null;
	private Map<Integer, QaCommentInfo> commentMap = null;

	public QaCommentHandler(GraphDatabaseService db, Map<Integer, QaCommentInfo> commentMap)
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
		QaCommentInfo commentInfo = new QaCommentInfo(node, id, postId, score, text, creationDate, userId);
		commentMap.put(id, commentInfo);
	}

}


class QaUserHandler extends DefaultHandler{
	private GraphDatabaseService db = null;
	private Map<Integer,QaUserInfo> userMap = null;
	
	public QaUserHandler(GraphDatabaseService db, Map<Integer,QaUserInfo> userMap){
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
		QaUserInfo userInfo = new QaUserInfo(node,id,reputation,creationDate,displayName,lastAccessDate,views,upVotes,downVotes);
		userMap.put(id, userInfo);
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