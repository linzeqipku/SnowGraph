package graphdb.extractors.miners.apiusage.entity;

abstract class MethodData
{
	String projectName;
	String packageName;
	String className;

	String name;
	String text;
	
	public abstract String getSignature();

	public String getProjectName()
	{
		return projectName;
	}

	void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}

	public String getPackageName()
	{
		return packageName;
	}

	void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getText()
	{
		return text;
	}

	void setText(String text)
	{
		this.text = text;
	}

	public String getClassName()
	{
		return className;
	}

	void setClassName(String className)
	{
		this.className = className;
	}

}
