package extractors.miners.mailqa;

import extractors.miners.mailqa.entity.Document;
import extractors.miners.mailqa.entity.Session;
import extractors.miners.mailqa.graphdb.SessionMaker;
import extractors.parsers.mail.MailListKnowledgeExtractor;
import framework.KnowledgeExtractor;
import framework.annotations.EntityDeclaration;
import framework.annotations.PropertyDeclaration;
import framework.annotations.RelationshipDeclaration;
import org.neo4j.graphdb.*;

import java.util.*;


/**
 * Created by maxkibble on 2016/12/4.
 */
public class MailQAKnowledgeExtractor implements KnowledgeExtractor {
    @EntityDeclaration
    public static final String SESSION = "Session";
    @PropertyDeclaration(parent = SESSION)
    public static final String SESSION_ID = "sessionID";

    @EntityDeclaration
    public static final String SESSION_QUESTION = "SessionQuestion";
    @PropertyDeclaration(parent = SESSION_QUESTION)
    public static final String SESSION_QUESTION_CONTENT = "sessionQuestionContent";

    @RelationshipDeclaration
    public static final String MAIL_OF_SESSION = "mailOfSession";
    @RelationshipDeclaration
    public static final String QUESTION_MAIL_OF_SESSION = "questionMailOfSession";
    @RelationshipDeclaration
    public static final String QUESTION_OF_SESSION = "questionOfSession";
    @RelationshipDeclaration
    public static final String ANSWER_MAIL_OF_SESSION = "answerMailOfSession";

    ArrayList<Session> sessions = new ArrayList<>();
    ArrayList<Document> documents = new ArrayList<>();
    GraphDatabaseService db = null;

    public void run(GraphDatabaseService db) {
        System.out.println("begin run qa");
        this.db = db;
        SessionMaker sessionMaker = new SessionMaker(db);
        sessions = sessionMaker.getSessions();
        for(Session session : sessions) {
            sessionMaker.processOneSession(session);
        }
        documents = sessionMaker.getDocumentList();
        System.out.println("document num : " + documents.size());
        createQANode();
    }

    private void createQANode() {
        //create session node, a session contains several mails, and has relationship: SESSION--->MAIL_OF_SESSION--->MAIL
        for(int i = 0; i < sessions.size(); i++) {
            Session s = sessions.get(i);
            int sID = s.getSessionID();
            String[] msgList = s.getMsgList().split(",");
            try(Transaction tx = db.beginTx()) {
                Node sessionNode = db.createNode();
                sessionNode.addLabel(Label.label(SESSION));
                sessionNode.setProperty(SESSION_ID, sID);
                for(Node node : db.getAllNodes()) {
                    if(!node.hasLabel(Label.label(MailListKnowledgeExtractor.MAIL))) continue;
                    boolean belongToThisSession = false;
                    for(String str : msgList) {
                        if(node.getProperty(MailListKnowledgeExtractor.MAIL_ID).equals(str)) {
                            belongToThisSession = true;
                            break;
                        }
                    }
                    if(belongToThisSession) {
                        sessionNode.createRelationshipTo(node, RelationshipType.withName(MailQAKnowledgeExtractor.MAIL_OF_SESSION));
                    }
                }
                tx.success();
                tx.close();
            }
        }

        // for those sessions which has Q&A information, finish three tasks below:
        // 1.create node SESSION_QUESTION, also relationship QUESTION_OF_SESSION from SESSION to SESSION_QUESTION
        // 2.create relationship QUESTION_MAIL_OF_SESSION from SESSION to MAIL
        // 3.create relationship ANSWER_MAIL_OF_SESSION from SESSION to MAIL
        for(int i = 0; i < documents.size(); i++) {
            Document d = documents.get(i);
            int sessionID = d.getSession_uuid();
            String qMailId = d.getQmail_uuid();
            String question = d.getQuestion();
            String aMailId = d.getAmail_uuid();
            try (Transaction tx = db.beginTx()) {
                for (Node node : db.getAllNodes()) {
                    if (!node.hasLabel(Label.label(MailQAKnowledgeExtractor.SESSION))) continue;
                    if (!node.getProperty(MailQAKnowledgeExtractor.SESSION_ID).equals(sessionID)) continue;
                    // 1.create node SESSION_QUESTION, also relationship QUESTION_OF_SESSION from SESSION to SESSION_QUESTION
                    Node questionNode = db.createNode();
                    questionNode.addLabel(Label.label(MailQAKnowledgeExtractor.SESSION_QUESTION));
                    questionNode.setProperty(MailQAKnowledgeExtractor.SESSION_QUESTION_CONTENT, question);
                    node.createRelationshipTo(questionNode, RelationshipType.withName(MailQAKnowledgeExtractor.QUESTION_OF_SESSION));

                    try (Transaction tx2 = db.beginTx()) {
                        for (Node node2 : db.getAllNodes()) {
                            if (!node2.hasLabel(Label.label(MailListKnowledgeExtractor.MAIL))) continue;
                            // 2.create relationship QUESTION_MAIL_OF_SESSION from SESSION to MAIL
                            if (node2.getProperty(MailListKnowledgeExtractor.MAIL_ID).equals(qMailId)) {
                                node.createRelationshipTo(node2, RelationshipType.withName(MailQAKnowledgeExtractor.QUESTION_MAIL_OF_SESSION));
                            }
                            // 3.create relationship ANSWER_MAIL_OF_SESSION from SESSION to MAIL
                            if (node2.getProperty(MailListKnowledgeExtractor.MAIL_ID).equals(aMailId)) {
                                node.createRelationshipTo(node2, RelationshipType.withName(MailQAKnowledgeExtractor.ANSWER_MAIL_OF_SESSION));
                            }
                        }
                        tx2.success();
                        tx2.close();
                    }
                }
                tx.success();
                tx.close();
            }
        }
    }
}
