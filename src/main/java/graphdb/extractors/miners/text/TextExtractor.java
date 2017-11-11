package graphdb.extractors.miners.text;

import graphdb.extractors.parsers.git.GitExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.jira.JiraExtractor;
import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.PropertyDeclaration;

public class TextExtractor implements Extractor {

	@PropertyDeclaration
    public static final String TITLE = "uniformTitle";
    @PropertyDeclaration
    public static final String TEXT = "uniformText";

    GraphDatabaseService db = null;

    @Override
    public void run(GraphDatabaseService db) {
        this.db = db;
        try (Transaction tx=db.beginTx()){
        	for (Node node:db.getAllNodes())
        		visit(node);
        	tx.success();
        }
    }
    
    public void visit(Node node){
    	
    	if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))){
    		node.setProperty(TITLE, node.getProperty(JavaCodeExtractor.CLASS_FULLNAME));
    		node.setProperty(TEXT, node.getProperty(JavaCodeExtractor.CLASS_CONTENT));
    	}
    	
    	if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))){
    		node.setProperty(TITLE, node.getProperty(JavaCodeExtractor.INTERFACE_FULLNAME));
    		node.setProperty(TEXT, node.getProperty(JavaCodeExtractor.INTERFACE_CONTENT));
    	}
    	
    	if (node.hasLabel(Label.label(GitExtractor.COMMIT))){
    		node.setProperty(TITLE, node.getProperty(GitExtractor.COMMIT_UUID));
    		node.setProperty(TEXT, node.getProperty(GitExtractor.COMMIT_LOGMESSAGE));
    	}
    	
    	if (node.hasLabel(Label.label(JiraExtractor.ISSUE))){
    		node.setProperty(TITLE, node.getProperty(JiraExtractor.ISSUE_SUMMARY));
    		node.setProperty(TEXT, node.getProperty(JiraExtractor.ISSUE_SUMMARY)+" "+node.getProperty(JiraExtractor.ISSUE_DESCRIPTION));
    	}
    	
    	if (node.hasLabel(Label.label(MailListExtractor.MAIL))){
    		node.setProperty(TITLE, node.getProperty(MailListExtractor.MAIL_SUBJECT));
    		node.setProperty(TEXT, node.getProperty(MailListExtractor.MAIL_SUBJECT)+" "+node.getProperty(MailListExtractor.MAIL_MAIN_TEXT));
    	}
    	
    	if (node.hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
    		node.setProperty(TITLE, node.getProperty(StackOverflowExtractor.QUESTION_TITLE));
    		node.setProperty(TEXT, node.getProperty(StackOverflowExtractor.QUESTION_TITLE)+" "+node.getProperty(StackOverflowExtractor.QUESTION_BODY));
    	}
    	
    	if (node.hasLabel(Label.label(StackOverflowExtractor.ANSWER))){
    		Iterator<Relationship> iterator=node.getRelationships(RelationshipType.withName(StackOverflowExtractor.HAVE_ANSWER), Direction.INCOMING).iterator();
    		String title="";
    		if (!iterator.hasNext())
    			title="";
    		else
    			title=(String) iterator.next().getStartNode().getProperty(StackOverflowExtractor.QUESTION_TITLE);
    		node.setProperty(TITLE, title);
    		node.setProperty(TEXT, title+" "+node.getProperty(StackOverflowExtractor.ANSWER_BODY));
    	}
    	
    }

}
