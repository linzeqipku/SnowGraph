package graphdb.extractors.miners.text;

import graphdb.extractors.parsers.git.GitExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.jira.JiraExtractor;
import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.*;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.PropertyDeclaration;

public class TextExtractor implements Extractor {

	@PropertyDeclaration
    public static final String TITLE = "uniformTitle";
    @PropertyDeclaration
    public static final String TEXT = "uniformText";
	@PropertyDeclaration
	public static final String IS_TEXT = "isText";

    @Override
    public void run(GraphDatabaseService db) {
		List<List<Node>> nodeSegs = new ArrayList<>();

		try (Transaction tx = db.beginTx()) {
			ResourceIterator<Node> nodes = db.getAllNodes().iterator();
			List<Node> list=new ArrayList<>();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (list.size()<1000)
					list.add(node);
				else {
					nodeSegs.add(list);
					list=new ArrayList<>();
				}
			}
			if (list.size()>0)
				nodeSegs.add(list);
			tx.success();
		}

		for (List<Node> list:nodeSegs)
			try (Transaction tx = db.beginTx()) {
				for (Node node:list)
					visit(node);
				tx.success();
			}
	}


    private void visit(Node node){

    	if (node.hasProperty(TITLE))
    		node.removeProperty(TITLE);
		if (node.hasProperty(TEXT))
			node.removeProperty(TEXT);
		if (node.hasProperty(IS_TEXT))
			node.removeProperty(IS_TEXT);
    	
    	if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
			node.setProperty(TITLE, node.getProperty(JavaCodeExtractor.CLASS_NAME));
			node.setProperty(TEXT, node.getProperty(JavaCodeExtractor.CLASS_CONTENT));
			node.setProperty(IS_TEXT, true);
		}
    	if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))) {
			node.setProperty(TITLE, node.getProperty(JavaCodeExtractor.INTERFACE_NAME));
			node.setProperty(TEXT, node.getProperty(JavaCodeExtractor.INTERFACE_CONTENT));
			node.setProperty(IS_TEXT, true);
		}
		if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD))) {
			node.setProperty(TITLE, node.getProperty(JavaCodeExtractor.METHOD_NAME));
			node.setProperty(TEXT, node.getProperty(JavaCodeExtractor.METHOD_CONTENT));
			node.setProperty(IS_TEXT, false);
		}
		if (node.hasLabel(Label.label(JavaCodeExtractor.FIELD))) {
			node.setProperty(TITLE, node.getProperty(JavaCodeExtractor.FIELD_NAME));
			node.setProperty(TEXT, node.getProperty(JavaCodeExtractor.FIELD_COMMENT));
			node.setProperty(IS_TEXT, false);
		}


    	if (node.hasLabel(Label.label(GitExtractor.COMMIT))){
    		node.setProperty(TITLE, "");
    		node.setProperty(TEXT, node.getProperty(GitExtractor.COMMIT_LOGMESSAGE));
			node.setProperty(IS_TEXT, true);
    	}
		if (node.hasLabel(Label.label(GitExtractor.COMMITAUTHOR))){
			node.setProperty(TITLE, node.getProperty(GitExtractor.COMMITAUTHOR_NAME));
			node.setProperty(TEXT, "");
			node.setProperty(IS_TEXT, false);
		}
		if (node.hasLabel(Label.label(GitExtractor.MUTATEDFILE))){
			node.setProperty(TITLE, node.getProperty(GitExtractor.MUTATEDFILE_API_NAME));
			node.setProperty(TEXT, "");
			node.setProperty(IS_TEXT, false);
		}
		if (node.hasLabel(Label.label(GitExtractor.MUTATEDCONTENT))){
			node.setProperty(TITLE, "");
			node.setProperty(TEXT, node.getProperty(GitExtractor.MUTATEDCONTENT_CONTENT));
			node.setProperty(IS_TEXT, false);
		}

    	if (node.hasLabel(Label.label(JiraExtractor.ISSUE))){
    		node.setProperty(TITLE, node.getProperty(JiraExtractor.ISSUE_SUMMARY));
    		node.setProperty(TEXT, node.getProperty(JiraExtractor.ISSUE_DESCRIPTION));
			node.setProperty(IS_TEXT, true);
    	}
		if (node.hasLabel(Label.label(JiraExtractor.ISSUEUSER))){
			node.setProperty(TITLE, node.getProperty(JiraExtractor.ISSUEUSER_NAME));
			node.setProperty(TEXT, "");
			node.setProperty(IS_TEXT, false);
		}
		if (node.hasLabel(Label.label(JiraExtractor.ISSUECOMMENT))){
			node.setProperty(TITLE, "");
			node.setProperty(TEXT, node.getProperty(JiraExtractor.ISSUECOMMENT_BODY));
			node.setProperty(IS_TEXT, true);
		}
		if (node.hasLabel(Label.label(JiraExtractor.PATCH))){
			node.setProperty(TITLE, node.getProperty(JiraExtractor.PATCH_NAME));
			node.setProperty(TEXT, node.getProperty(JiraExtractor.PATCH_CONTENT));
			node.setProperty(IS_TEXT, false);
		}
    	
    	if (node.hasLabel(Label.label(MailListExtractor.MAIL))){
    		node.setProperty(TITLE, node.getProperty(MailListExtractor.MAIL_SUBJECT));
    		node.setProperty(TEXT, node.getProperty(MailListExtractor.MAIL_BODY));
			node.setProperty(IS_TEXT, true);
    	}
		if (node.hasLabel(Label.label(MailListExtractor.MAILUSER))){
			node.setProperty(TITLE, node.getProperty(MailListExtractor.MAILUSER_MAIL));
			node.setProperty(TEXT, node.getProperty(MailListExtractor.MAILUSER_NAMES));
			node.setProperty(IS_TEXT, false);
		}
    	
    	if (node.hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
    		node.setProperty(TITLE, node.getProperty(StackOverflowExtractor.QUESTION_TITLE));
    		node.setProperty(TEXT, node.getProperty(StackOverflowExtractor.QUESTION_BODY));
			node.setProperty(IS_TEXT, true);
    	}
    	if (node.hasLabel(Label.label(StackOverflowExtractor.ANSWER))){
    		node.setProperty(TITLE, "");
    		node.setProperty(TEXT, node.getProperty(StackOverflowExtractor.ANSWER_BODY));
			node.setProperty(IS_TEXT, true);
    	}
		if (node.hasLabel(Label.label(StackOverflowExtractor.COMMENT))){
			node.setProperty(TITLE, "");
			node.setProperty(TEXT, node.getProperty(StackOverflowExtractor.COMMENT_TEXT));
			node.setProperty(IS_TEXT, true);
		}
		if (node.hasLabel(Label.label(StackOverflowExtractor.USER))){
			node.setProperty(TITLE, node.getProperty(StackOverflowExtractor.USER_DISPLAY_NAME));
			node.setProperty(TEXT, "");
			node.setProperty(IS_TEXT, false);
		}

    	
    }

}
