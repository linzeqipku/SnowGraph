package discretgraphs.code.extractor.binparser.entity;

public class BinInterfaceInfo {

	private String name;	//接口名
	private String fullName;	//接口的全名
	private String template;	//模板，如为空则取"<>"
	private String superInterface;	//父接口，如为空则取"NotFound"

	public BinInterfaceInfo(String name,String fullName,String template,String superInterface){
		this.name=name;
		this.fullName=fullName;
		this.template=template;
		this.superInterface=superInterface;
	}
	
	public String getName(){
		return name;
	}
	
	public String getFullName(){
		return fullName;
	}
	
	public String getTemplate(){
		return template;
	}
	
	public String getSuperInterface() {
		return superInterface;
	}
	
}