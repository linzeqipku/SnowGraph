package graphmodel.entity.qa;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;

import org.neo4j.graphdb.Node;

public class QaSchema extends Schema
{

	public static String getContent(Node node){
		if (node.hasLabel(ManageElements.Labels.QUESTION))
			return (String)node.getProperty(QuestionSchema.TITLE)+" ===== "+(String)node.getProperty(QuestionSchema.BODY);
		if (node.hasLabel(ManageElements.Labels.ANSWER))
			return (String)node.getProperty(AnswerSchema.BODY);
		if (node.hasLabel(ManageElements.Labels.QA_COMMENT))
			return (String)node.getProperty(QaCommentSchema.TEXT);
		return "";
	}
	
}
