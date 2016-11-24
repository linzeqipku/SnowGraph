package pfr.plugins.parsers.javacode.entity;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo{	

	public String name;
	public String fullName;
	public String visibility = "packge";
	public boolean isAbstract = false;
	public boolean isFinal = false;
	public String superClassType;
	public List<String> superInterfaceTypeList=new ArrayList<String>();
	public String comment="";
	public String content;

}
