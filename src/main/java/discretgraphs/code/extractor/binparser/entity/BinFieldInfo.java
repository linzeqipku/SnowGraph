package discretgraphs.code.extractor.binparser.entity;

public class BinFieldInfo {

	private String name;	//域名
	private int modifiers;	//域的访问控制符
	
	private String type;	//域的类型，如未找到则取"NotFound"
	private String belongTo;	//域所属的类
	
	public BinFieldInfo(String name, int modifiers, String type, String belongTo){
		this.name=name;
		this.modifiers=modifiers;
		this.type=type;
		this.belongTo=belongTo;
	}
	
	public String getName(){
		return name;
	}
	
	public int getModifiers(){
		return modifiers;
	}
	
	public String getType(){
		return type;
	}
	
	public String getBelongTo(){
		return belongTo;
	}

	/**
	 * 域的全名=所属的类的全名+"."+域名
	 * @return	域的全名
	 */
	public String getFullName() {
		return belongTo+"."+name;
	}
	
}
