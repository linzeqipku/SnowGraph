package graphmodel.entity;

import graphmodel.ManageElements;
import graphmodel.entity.code.ClassSchema;
import graphmodel.entity.code.InterfaceSchema;
import graphmodel.entity.code.MethodSchema;
import graphmodel.entity.issuetracker.IssueCommentSchema;
import graphmodel.entity.issuetracker.IssueSchema;
import graphmodel.entity.issuetracker.PatchSchema;
import graphmodel.entity.mail.MailSchema;
import graphmodel.entity.qa.QaSchema;

import org.neo4j.graphdb.Node;

public abstract class Schema {
	
	protected Node node;
	
	public Node getNode(){
		return node;
	}
	
	public static String getContent(Node node){
		String r="";
		
		if (node.hasLabel(ManageElements.Labels.CLASS)){
			r=(String)node.getProperty(ClassSchema.NAME)+" ";
			if (node.hasProperty(ClassSchema.COMMENT))
				r+=(String)node.getProperty(ClassSchema.COMMENT);
		}
		
		if (node.hasLabel(ManageElements.Labels.INTERFACE)){
			r=(String)node.getProperty(InterfaceSchema.NAME)+" ";
			if (node.hasProperty(InterfaceSchema.COMMENT))
				r+=(String)node.getProperty(InterfaceSchema.COMMENT);
		}
		
		if (node.hasLabel(ManageElements.Labels.METHOD)){
			r=(String)node.getProperty(MethodSchema.NAME)+" ";
			if (node.hasProperty(MethodSchema.COMMENT))
				r+=(String)node.getProperty(MethodSchema.COMMENT);
		}
		
		if (node.hasLabel(ManageElements.Labels.MAIL)){
			r=(String)node.getProperty(MailSchema.SUBJECT)+" "+(String)node.getProperty(MailSchema.BODY);
		}
		
		if (node.hasLabel(ManageElements.Labels.QUESTION)||node.hasLabel(ManageElements.Labels.ANSWER)||node.hasLabel(ManageElements.Labels.QA_COMMENT)){
			//System.out.println(node.getId());
			r=QaSchema.getContent(node);
		}
		
		/**************************添加对Issue相关资源（Issue, Patch, IssueComment）的Content支持*********************************/
		if(node.hasLabel(ManageElements.Labels.ISSUE)){
			String issueSummary = node.getProperty(IssueSchema.SUMMARY).toString();
			String issueDescription = node.getProperty(IssueSchema.DESCRIPTION).toString();
			r = issueSummary + " " + issueDescription;
		}else if(node.hasLabel(ManageElements.Labels.PATCH)){
			r = node.getProperty(PatchSchema.CONTENT).toString();
		}else if(node.hasLabel(ManageElements.Labels.ISSUE_COMMENT)){
			r = node.getProperty(IssueCommentSchema.BODY).toString();
		}
		return r;
	}
	
}
