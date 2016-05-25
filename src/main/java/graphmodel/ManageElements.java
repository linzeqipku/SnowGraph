package graphmodel;


import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class ManageElements {
	
	public static enum RelTypes implements RelationshipType{
		EXTEND,//class-->class
		IMPLEMENT,//class-->interface
		THROW,//method-->class
		PARAM,//method-->class/interface
		RT,//method-->class/interface
		HAVE_METHOD,//class/interface-->method
		HAVE_FIELD,//class/interface-->field
		CALL_METHOD,//method-->method
		CALL_FIELD,//method-->field
		TYPE,//field-->class/interface
		
		HAVE_ANSWER,//QUESTION-->ANSWER
		HAVE_QA_COMMENT,//QUESTION/ANSWER-->QA_COMMENT
		
		DOC_LEVEL_REFER,
		LEX_LEVEL_REFER,
		
		MAIL_IN_REPLY_TO,
		MAIL_SENDER,
		MAIL_RECEIVER,
		
		HAVE_PATCH,//ISSUE --> PATCH
		HAVE_ISSUE_COMMENT,// ISSUE --> ISSUE_COMMENT
		
		DUPLICATE,
		AUTHOR,
		SAME_USER,
		
		DEFAULT
	}
	
	public static enum Labels implements Label{
		METHOD,
		CLASS,
		FIELD,
		INTERFACE,
		
		QUESTION,
		ANSWER,
		QA_COMMENT,
		QA_USER,
		
		MAIL,
		MAIL_USER,
		
		ISSUE,
		PATCH,
		ISSUE_COMMENT,
		ISSUE_USER
	}

}
