package pfr.plugins.parsers.javacode.entity;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodInfo{

	public IMethodBinding methodBinding;
	
	public String name;
	public String returnString;
	public Set<String> returnTypes=new HashSet<String>();
	public String visibility;
	public boolean isConstruct;
	public boolean isAbstract;
	public boolean isFinal;
	public boolean isStatic;
	public boolean isSynchronized;
	public String content;
	public String comment="";
	public String belongTo;
	public String paramString;
	public Set<String> paramTypes=new HashSet<String>();
	public Set<String> variableTypes=new HashSet<String>();
	public Set<IMethodBinding> methodCalls=new HashSet<IMethodBinding>();
	public Set<String> fieldUsesSet=new HashSet<String>();
	public Set<String> throwSet=new HashSet<String>();
	
	public String hashName(){
		return belongTo+"."+name+"("+paramString+")";
	}
	
}
