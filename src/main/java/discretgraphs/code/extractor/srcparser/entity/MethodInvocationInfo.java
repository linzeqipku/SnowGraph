/**
 * 
 */
package discretgraphs.code.extractor.srcparser.entity;

/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-6-21 下午11:02:46
 * @version 1.0
 * MethodInvocationInfo对象存储一个方法体中的方法调用
 */
public class MethodInvocationInfo extends CommonInfo {
	
	/**
	 * 方法调用字符串
	 * 如name = "System.out.println()";
	 */
	private String name;
	
	/**
	 * 方法调用所属的方法
	 */
	private MethodInfo methodInfo;

	// private List<String> argumentList;
	/**
	 * 
	 */
	public MethodInvocationInfo() {
		//setArgumentList(new ArrayList<String>());
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the methodInfo
	 */
	public MethodInfo getMethodInfo() {
		return methodInfo;
	}

	/**
	 * @param methodInfo
	 *            the methodInfo to set
	 */
	public void setMethodInfo(MethodInfo methodInfo) {
		this.methodInfo = methodInfo;
	}

	/**
	 * @return the argumentList
	 */
	// public List<String> getArgumentList() {
	// return argumentList;
	// }
	/**
	 * @param argumentList
	 *            the argumentList to set
	 */
	// public void setArgumentList(List<String> argumentList) {
	// this.argumentList = argumentList;
	// }
	public String getHashName() {
		if (methodInfo != null) {
			return methodInfo.getHashName() + "|#|" + name;
		}
		return "|#||#||#||#||#|" + name;
	}

	public boolean equals(Object o) {
		if (this.getHashName().equals(((MethodInvocationInfo) o).getHashName())) {
			return true;
		} else {
			return false;
		}
	}

}
