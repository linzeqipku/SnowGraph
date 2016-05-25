package graphmodel.entity.code;

import java.util.Collection;
import java.util.Iterator;

import graphmodel.entity.Schema;
import javassist.Modifier;

public abstract class CodeSchema extends Schema{
	
	public static String SEPERATOR=",";
	
	public static String COMMENT="comment";
	
	public static String getAccess(int modifier){
		boolean p1=Modifier.isPrivate(modifier);
		boolean p2=Modifier.isProtected(modifier);
		boolean p3=Modifier.isPublic(modifier);
		if (p1&&p2)
			return "private protected";
		if (p1)
			return "private";
		if (p2)
			return "protected";
		if (p3)
			return "public";
		return "package";
	}
	
	static String line(Collection<String> v){
		String r="";
		Iterator<String> iter=v.iterator();
		String seperator="";
		while (iter.hasNext()){
			r+=seperator+iter.next();
			seperator=SEPERATOR;
		}
		return r;
	}
	
	public void setComment(String comment){
		node.setProperty(COMMENT, comment);
	}

}
