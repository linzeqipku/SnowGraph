package extractors.miners.mailqa.graphdb;

import extractors.miners.mailqa.answer.AnswerSelector;
import extractors.miners.mailqa.content.MessageProcess;
import extractors.miners.mailqa.entity.*;
import extractors.miners.mailqa.question.QuestionClassifier;
import extractors.miners.mailqa.tag.Keywords;
import extractors.parsers.mail.MailListKnowledgeExtractor;
import org.neo4j.graphdb.*;

import java.util.*;

/**
 * Created by maxkibble on 2017/1/14.
 */
public class SessionMaker {
    private HashMap<String, Email> emailList = new HashMap<>();
    private ArrayList<Session> sessionList = new ArrayList<>();
    private ArrayList<SessionContent> sessionContentList = new ArrayList<>();
    private ArrayList<Document> documentList = new ArrayList<>();
    public static final String			SPLIT_CONTENT		= "#####SPLIT_CONTENT#####";
    public static final String			QUESTION_MAIL		= "#####QUESTION_MAIL#####";
    public static final String			REPLY_MAIL			= "#####REPLY_MAIL#####";
    public MessageProcess messageProcess		= new MessageProcess();
    public QuestionClassifier questionClassifier	= new QuestionClassifier();
    public AnswerSelector answerSelector		= new AnswerSelector();

    public HashMap<String, Email> getEmails() {
        return emailList;
    }

    public ArrayList<Session> getSessions() {
        return sessionList;
    }

    public ArrayList<SessionContent> getSessionContents() {
        return sessionContentList;
    }

    public ArrayList<Document> getDocumentList() {
        return documentList;
    }

    public static Date getDate(String s) {
        String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        String[] content = s.split(" ");
        int d = Integer.parseInt(content[1]);
        int m = 0;
        for(int i = 0; i < 12; i++) {
            if(content[2].equals(month[i])) {
                m = i;
                break;
            }
        }
        int y = Integer.parseInt(content[3]) - 1900;
        String t = content[4];
        String[] tContent = t.split(":");
        int h = Integer.parseInt(tContent[0]);
        int min = Integer.parseInt(tContent[1]);
        int sec = Integer.parseInt(tContent[2]);
        return new Date(y, m, d, h, min, sec);
    }

    public SessionMaker(GraphDatabaseService db) {
        // read all the mails
        try (Transaction tx = db.beginTx()) {
            for(Node node : db.getAllNodes()) {
                if(node.hasLabel(Label.label(MailListKnowledgeExtractor.MAIL))) {
                    Email email = new Email(node);
                    emailList.put(email.getMessageID(), email);
                }
            }
            tx.success();
        }

        //build session, has 2 steps
        //step1: record all the reply relationship between mails
        try (Transaction tx = db.beginTx()) {
            for(Relationship r : db.getAllRelationships()) {
                if(!r.getType().name().equals(MailListKnowledgeExtractor.MAIL_IN_REPLY_TO)) {
                    continue;
                }
                Node qMailNode = r.getEndNode();
                Node aMailNode = r.getStartNode();
                String aMailId = (String)aMailNode.getProperty(MailListKnowledgeExtractor.MAIL_ID);
                emailList.get(aMailId).setInReplyTo((String)qMailNode.getProperty(MailListKnowledgeExtractor.MAIL_ID));
            }
        }
        //step2: build session based on three rules:
        //rule1: if a mail's subject doesn't start with 're' or 'aw', mark it as question mail of a new session
        //rule2: if a mail's subject contains current question mail's subject, add it into the session and mark as an answer mail
        //rule3: if a mail is reply to any mail in current session, add it into current session and mark as answer mail
        int sessionCounter = 0;
        HashSet<String> isVisited = new HashSet<>();
        ArrayList<Email> newEmailList = new ArrayList<Email>();
        System.out.println("email num : " + emailList.size());

        Iterator iter = emailList.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String emailID = (String)entry.getKey();
            Email email = (Email)entry.getValue();
            if (isVisited.contains(emailID))
                continue;

            if (email.getInReplyTo() == null || email.getInReplyTo().trim().length() == 0) {
                // in reply to is empty, this email maybe a start of a session
                String subject = email.getSubject();
                if (subject == null || subject.trim().length() == 0
                        || subject.toLowerCase().startsWith("re")
                        || subject.toLowerCase().startsWith("aw")) {
                    // a email with no subject or its subject start with "re" is
                    // not a session start
                    continue;
                }

                newEmailList.add(email);
                isVisited.add(emailID);
                Session oneSession = new Session();
                oneSession.setSessionID(sessionCounter); sessionCounter = sessionCounter + 1;
                oneSession.setStartTime(email.getSendDate());
                oneSession.setPromoterName(email.getFromName());
                oneSession.setPromoterEmail(email.getFromEmail());
                oneSession.setPromoterEmailID(email.getMessageID());
                oneSession.setSubject(email.getSubject());
                String participants = email.getFromEmail();

                String endTime = email.getSendDate();
                if (endTime == null)
                   endTime = "";
                HashSet<String> replySet = new HashSet<String>();
                replySet.add(email.getMessageID());

                SessionContent oneSessionContent = new SessionContent();
                oneSessionContent.setSessionID(oneSession.getSessionID());
                oneSessionContent.setSubject(subject);

                StringBuilder sessionContentSB = new StringBuilder();
                sessionContentSB.append(email.getSubject() + "\r\n");
                sessionContentSB.append(QUESTION_MAIL + "\r\n");
                sessionContentSB.append(email.getMessageID() + "\r\n");
                sessionContentSB.append("" + "\r\n");
                sessionContentSB.append(email.getContent() + "\r\n");
                sessionContentSB.append(SPLIT_CONTENT + "\r\n");

                Iterator iter2 = emailList.entrySet().iterator();
                while(iter2.hasNext()) {
                    Map.Entry entry2 = (Map.Entry) iter2.next();
                    String emailID2 = (String)entry2.getKey();
                    Email e = (Email)entry2.getValue();
                    if (isVisited.contains(emailID2))
                        continue;

                    if (e.getSubject() != null
                            && e.getSubject().toLowerCase().contains(subject.toLowerCase())) {
                        isVisited.add(emailID2);
                        newEmailList.add(e);
                        if (e.getInReplyTo() == null || e.getInReplyTo().trim().length() == 0) {
                            e.setInReplyTo(email.getMessageID());
                        }
                        participants += "," + e.getFromEmail();
                        if (!replySet.contains(e.getMessageID())) {
                            replySet.add(e.getMessageID());
                        }
                        if (e.getSendDate() != null
                                && getDate(endTime).getTime() < getDate(e.getSendDate()).getTime()) {
                            endTime = e.getSendDate();
                        }
                        if (e.getFromEmail().equals(email.getFromEmail())) {
                            sessionContentSB.append(QUESTION_MAIL + "\r\n");
                        }
                        else
                            sessionContentSB.append(REPLY_MAIL + "\r\n");
                        sessionContentSB.append(e.getMessageID() + "\r\n");
                        sessionContentSB.append(e.getInReplyTo() + "\r\n");
                        sessionContentSB.append(e.getContent() + "\r\n");
                        sessionContentSB.append(SPLIT_CONTENT + "\r\n");
                    }
                    else if (e.getSubject() != null && e.getInReplyTo() != null
                            && replySet.contains(e.getInReplyTo())) {
                        isVisited.add(emailID2);
                        newEmailList.add(e);
                        if (e.getInReplyTo() == null || e.getInReplyTo().trim().length() == 0) {
                            e.setInReplyTo(email.getMessageID());
                        }
                        participants += "," + e.getFromEmail();
                        if (!replySet.contains(e.getMessageID())) {
                            replySet.add(e.getMessageID());
                        }
                        if (e.getSendDate() != null
                                && getDate(endTime).getTime() < getDate(e.getSendDate()).getTime()) {
                            endTime = e.getSendDate();
                        }
                        if (e.getFromEmail().equals(email.getFromEmail())) {
                            sessionContentSB.append(QUESTION_MAIL + "\r\n");
                        }
                        else
                            sessionContentSB.append(REPLY_MAIL + "\r\n");
                        sessionContentSB.append(e.getMessageID() + "\r\n");
                        sessionContentSB.append(e.getInReplyTo() + "\r\n");
                        sessionContentSB.append(e.getContent() + "\r\n");
                        sessionContentSB.append(SPLIT_CONTENT + "\r\n");
                    }
                }

                oneSession.setEndTime(endTime);
                oneSession.setParticipants(participants);
                String msgList = "";
                for (String str : replySet) {
                    if (msgList.length() == 0) {
                        msgList = str;
                    }
                    else {
                        msgList += "," + str;
                    }
                }
                oneSession.setMsgList(msgList);
                oneSessionContent.setMsgList(msgList);
                oneSessionContent.setParticipants(participants);
                oneSessionContent.setContent(sessionContentSB.toString());
                sessionContentList.add(oneSessionContent);
                sessionList.add(oneSession);
            }
        }
        System.out.println("session num :" + sessionList.size());
    }

    public void processOneSession(Session session) {

        String qMailID = session.getPromoterEmailID();
        Email qMail = emailList.get(qMailID);

        if (qMail == null || qMail.getMessageID() == null)
            return;

        String participants[] = session.getParticipants().split(",");
        if(participants.length <= 1) return ;

        ArrayList<Email> rMailList = new ArrayList<Email>();
        ArrayList<Email> allMailList = new ArrayList<>();
        String[] msgIDList = session.getMsgList().split(",");
        for(String msgID : msgIDList) {
            Email e = emailList.get(msgID);
            allMailList.add(e);
        }

        if(allMailList.size() <= 1 || allMailList.size() >= 15) return;

        for (Email e : allMailList) {
            if (e.getFromEmail() != null && !e.getFromEmail().equals(qMail.getFromEmail())) {
                rMailList.add(e);
            }
        }
        if(rMailList.size() == 0) return;

        Document doc = new Document();
        doc.setQmail_uuid(qMail.getMessageID());
        doc.setSession_uuid(session.getSessionID());

        messageProcess.process(qMail);

        String question = questionClassifier.getQuestionSentence(qMail);
        doc.setQuestion(question);
        String keywords = getKeywords(qMail,question);
        doc.setKeywords(keywords);

        ArrayList<Segment> segmentList = qMail.getEmailContent().getSegments();
        for (int i = 0; i < segmentList.size(); i++) {
            Segment segment = segmentList.get(i);
            Seg seg = new Seg();
            seg.setContent(segment.getContentText());
            seg.setMessage_uuid(qMail.getMessageID());
            seg.setSession_uuid(session.getSessionID());
            seg.setSegment_type(segment.getContentType());
            seg.setSegment_no(i + 1);

            if (seg.getSegment_type() == Segment.NORMAL_CONTENT
                    && seg.getContent().contains(question)) {
                doc.setQuestion_seg_no(i + 1);
            }
        }

        for (Email e : rMailList) {
            messageProcess.process(e);
            ArrayList<Segment> segList = e.getEmailContent().getSegments();
            for(int i = 0; i < segList.size(); i++) {
                Segment segment = segList.get(i);
                Seg seg = new Seg();
                seg.setContent(segment.getContentText());
                seg.setMessage_uuid(e.getMessageID());
                seg.setSession_uuid(session.getSessionID());
                seg.setSegment_type(segment.getContentType());
                seg.setSegment_no(i + 1);
            }
        }

        Email aMail = answerSelector.getAnswerMail(qMail, question, rMailList);
        doc.setAmail_uuid(aMail.getMessageID());
        documentList.add(doc);
    }

    private String getKeywords(Email qMail,String question) {
        Keywords kw = new Keywords();
        String subject = qMail.getSubject();
        String str = subject;
        if(!subject.equals(question)) str += " " + question;
        String words[] = str.split(" ");

        String result = "";

        for(String word : words) {

            word = word.toLowerCase();
            if(!kw.isFunctionWords(word) && !kw.isStopWords(word)) {
                //System.out.println(word+"###");
                result += word + " ";
                continue;
            }
            if(kw.isProjectWords(word)) {
                result += word + " ";
                continue;
            }

        }
        return result;
    }
}
