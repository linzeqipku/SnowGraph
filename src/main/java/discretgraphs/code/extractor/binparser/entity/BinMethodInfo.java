package discretgraphs.code.extractor.binparser.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BinMethodInfo {

	private String name;	//方法名，如getUses
	private String fullName;	//方法的全名，例如"javassist.CtMethod.setBody(java.lang.String)"
	private int modifiers;	//方法的访问控制符
	
	private String belongTo;	//方法所属的类/接口
	private List<String> params=new ArrayList<String>();	//方法中的所有参数
	private String rt;	//方法的返回类型
	private List<String> exceptions=new ArrayList<String>();	//方法抛出的异常类型
	private HashSet<String> methodCalls=new HashSet<String>();	//方法体中调用的方法
	private HashSet<String> uses=new HashSet<String>();	//方法体中用到的域.
	
	public BinMethodInfo(String name, String fullName, int modifiers, String belongTo, List<String> params, String rt,List<String> exceptions, HashSet<String> methodCalls, HashSet<String> uses){
		this.name=name;
		this.fullName=fullName;
		this.modifiers=modifiers;
		this.belongTo=belongTo;
		for (String i:params)
			this.params.add(i);
		this.rt=rt;
		for (String i:exceptions)
			this.exceptions.add(i);
		for (String i:methodCalls)
			this.methodCalls.add(i);
		this.uses.addAll(uses);
	}
	
	public HashSet<String> getUses(){
		return uses;
	}
	
	public String getName(){
		return name;
	}
	
	public String getFullName(){
		return fullName;
	}
	
	public int getModifiers(){
		return modifiers;
	}
	
	public String getBelongTo(){
		return belongTo;
	}
	
	public List<String> getParams(){
		return params;
	}
	
	public String getRt(){
		return rt;
	}
	
	public List<String> getExceptions(){
		return exceptions;
	}
	
	public HashSet<String> getMethodCalls(){
		return methodCalls;
	}
	
}
