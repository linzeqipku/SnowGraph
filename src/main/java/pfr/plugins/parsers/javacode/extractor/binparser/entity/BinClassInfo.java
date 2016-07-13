package pfr.plugins.parsers.javacode.extractor.binparser.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BinClassInfo
{

	private String name;	//类名，如BinClassInfo
	private String fullName;	//类的全名，如codegraph.extractor.binparser.entity.BinClassInfo
	private String template;	//类的模板，如为空则取"<>"
	private int modifiers;	//类的访问控制符

	private String superClass;	//父类（一定有，因为所有类都继承了java.lang.Object）
	private List<String> interfaces = new ArrayList<String>();	//实现的接口
	private HashSet<String> refClasses = new HashSet<String>();	//用到的类

	public BinClassInfo(String name, String fullName, String template, int modifiers, String superClass, List<String> interfaces, HashSet<String> refClasses)
	{
		this.name = name;
		this.fullName = fullName;
		this.modifiers = modifiers;
		for (String i : interfaces)
			this.interfaces.add(i);
		for (String i : refClasses)
			this.refClasses.add(i);
		this.template = template;
		this.superClass = superClass;
	}

	public String getName()
	{
		return name;
	}

	public String getFullName()
	{
		return fullName;
	}

	public int getModifiers()
	{
		return modifiers;
	}

	public String getSuperClass()
	{
		return superClass;
	}

	public List<String> getInterfaces()
	{
		return interfaces;
	}

	public HashSet<String> getRefClasses()
	{
		return refClasses;
	}

	public String getTemplate()
	{
		return template;
	}

}
