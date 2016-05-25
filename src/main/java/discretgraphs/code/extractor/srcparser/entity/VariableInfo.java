/**
 * 
 */
package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-6-21 下午11:02:22
 * @version 1.0
 * VariableInfo对象存储一个方法体局部变量的信息
 */
public class VariableInfo extends CommonInfo {
	/**
	 * 变量名
	 */
	private String name;
	
	/**
	 * 变量类型
	 */
	private String type;
	
	/**
	 * 变量所属方法
	 */
	private MethodInfo methodInfo;
	
	private List<String> simpleTypes;

	public VariableInfo() {
		simpleTypes = new ArrayList<String>();
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the methodInfo
	 */
	public MethodInfo getMethodInfo() {
		return methodInfo;
	}
	/**
	 * @param methodInfo the methodInfo to set
	 */
	public void setMethodInfo(MethodInfo methodInfo) {
		this.methodInfo = methodInfo;
	}
	/**
	 * @return the simpleTypes
	 */
	public List<String> getSimpleTypes() {
		return simpleTypes;
	}
	/**
	 * @param simpleTypes the simpleTypes to set
	 */
	public void setSimpleTypes(List<String> simpleTypes) {
		this.simpleTypes = simpleTypes;
	}
	@Override
	public String getHashName() {
		if (methodInfo != null) {
			return methodInfo.getHashName() + "|#|" + type + " " + name;
		}
		return "|#||#||#||#||#|" + type + " " + name;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this.getHashName().equals(((VariableInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}
}
