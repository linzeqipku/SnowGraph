package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:38
 * @version 0.1 2012-12-17
 * ParameterInfo对象存储一个方法参数的信息
 */
public class ParameterInfo extends CommonInfo {
	/**
	 * 参数名
	 */
	private String name;
	
	/**
	 * 参数所属类型
	 */
	private String type;
	
	/**
	 * 去除数组及常见容器的等结构后获得的简单类型
	 */
	private List<String> simpleTypes;
	
	/**
	 * 参数是否为final的
	 */
	private boolean isFinal = false;
	
	/**
	 * 参数所属的method
	 */
	private MethodInfo methodInfo;
	
	public ParameterInfo() {
		simpleTypes = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public void setMethodInfo(MethodInfo methodInfo) {
		this.methodInfo = methodInfo;
	}

	public MethodInfo getMethodInfo() {
		return methodInfo;
	}

	@Override
	public String getHashName() {
		if(methodInfo != null)
		{
			return methodInfo.getHashName() + "|#|" + name;
		}
		else {
			return "|#||#||#||#||#|" + name;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this.getHashName().equals(((ParameterInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}

}
