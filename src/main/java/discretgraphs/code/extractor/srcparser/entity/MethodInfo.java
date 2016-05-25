package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:25
 * @version 0.1 2012-12-17
 * MethodInfo对象存储一个方法的信息
 */
public class MethodInfo extends CommonInfo {
	/**
	 * 方法的整个签名
	 */
	private String name;
	
	/**
	 * 方法名，不包含参数等其它信息
	 */
	private String shortName;
	
	/**
	 * 方法的返回类型
	 */
	private String returnType;
	
	/**
	 * 去除数组及常见容器的等结构后获得的简单返回类型
	 */
	private List<String> simpleTypes;
	
	/**
	 * 方法的可见性
	 */
	private String visibility;
	
	/**
	 * 是否是构造函数
	 */
	private boolean isConstruct;
	
	/**
	 * 是否是抽象方法
	 */
	private boolean isAbstract;
	
	/**
	 * 是否是final方法
	 */
	private boolean isFinal;
	
	/**
	 * 是否是static方法
	 */
	private boolean isStatic;
	
	/**
	 * 是否是同步方法
	 */
	private boolean isSynchronized;
	// private boolean isNative;
	// private boolean isStrictfp;
	
	/**
	 * 方法体的内容
	 */
	private String methodContent;
	
	/**
	 * 方法所属的类
	 */
	private ClassInfo classInfo;
	
	/**
	 * 方法所属的接口
	 */
	private InterfaceInfo interfaceInfo;
	
	/**
	 * 方法注释
	 */
	private String comment;
	
	private CommentInfo commentInfo;
	
	/**
	 * 方法包含的参数
	 */
	private List<ParameterInfo> parameterInfoList;
	
	/**
	 * 方法体包含的局部变量
	 */
	private List<VariableInfo> variableInfoList;
	
	/**
	 * 方法体包含的方法调用
	 */
	private List<MethodInvocationInfo> methodInvocationInfoList;
	
	private String uuid;

	private List<StatementInfo> statementInfoList;
	public MethodInfo() {
		simpleTypes = new ArrayList<String>();
		parameterInfoList = new ArrayList<ParameterInfo>();
		variableInfoList = new ArrayList<VariableInfo>();
		methodInvocationInfoList = new ArrayList<MethodInvocationInfo>();
		statementInfoList = new ArrayList<StatementInfo>();
		uuid = UUID.randomUUID().toString(); 
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
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

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getVisibility() {
		return visibility;
	}

	/*
	 * public String getAccessModifier() { return accessModifier; } public void
	 * setAccessModifier(String accessModifier) { this.accessModifier =
	 * accessModifier; }
	 */
	public boolean isConstruct() {
		return isConstruct;
	}

	public void setConstruct(boolean isConstruct) {
		this.isConstruct = isConstruct;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * @return the isStatic
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * @param isStatic the isStatic to set
	 */
	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public boolean isSynchronized() {
		return isSynchronized;
	}

	public void setSynchronized(boolean isSynchronized) {
		this.isSynchronized = isSynchronized;
	}

	/*
	 * public boolean isNative() { return isNative; } public void
	 * setNative(boolean isNative) { this.isNative = isNative; } public boolean
	 * isStrictfp() { return isStrictfp; } public void setStrictfp(boolean
	 * isStrictfp) { this.isStrictfp = isStrictfp; }
	 */
	public void setMethodContent(String methodContent) {
		this.methodContent = methodContent;
	}

	public String getMethodContent() {
		return methodContent;
	}

	public void setClassInfo(ClassInfo classInfo) {
		this.classInfo = classInfo;
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public void setInterfaceInfo(InterfaceInfo interfaceInfo) {
		this.interfaceInfo = interfaceInfo;
	}

	public InterfaceInfo getInterfaceInfo() {
		return interfaceInfo;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setParameterInfoList(List<ParameterInfo> parameterInfoList) {
		this.parameterInfoList = parameterInfoList;
	}

	public List<ParameterInfo> getParameterInfoList() {
		return parameterInfoList;
	}

	/**
	 * @return the variableInfoList
	 */
	public List<VariableInfo> getVariableInfoList() {
		return variableInfoList;
	}

	/**
	 * @param variableInfoList the variableInfoList to set
	 */
	public void setVariableInfoList(List<VariableInfo> variableInfoList) {
		this.variableInfoList = variableInfoList;
	}

	/**
	 * @return the methodInvocationInfoList
	 */
	public List<MethodInvocationInfo> getMethodInvocationInfoList() {
		return methodInvocationInfoList;
	}

	/**
	 * @param methodInvocationInfoList the methodInvocationInfoList to set
	 */
	public void setMethodInvocationInfoList(List<MethodInvocationInfo> methodInvocationInfoList) {
		this.methodInvocationInfoList = methodInvocationInfoList;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	

	public List<StatementInfo> getStatementInfoList() {
		return statementInfoList;
	}

	public void setStatementInfoList(List<StatementInfo> statementInfoList) {
		this.statementInfoList = statementInfoList;
	}

	@Override
	public String getHashName() {
		if (interfaceInfo != null) {
			return interfaceInfo.getHashName() + "|#|" + name;
		}
		if (classInfo != null) {
			return classInfo.getHashName() + "|#|" + name;
		}
		return "|#||#||#||#|" + name;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this.getHashName().equals(((MethodInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}

	public void addStatementInfo(StatementInfo statementInfo) {
		statementInfoList.add(statementInfo);
	}
	public CommentInfo getCommentInfo() {
		return commentInfo;
	}

	public void setCommentInfo(CommentInfo commentInfo) {
		this.commentInfo = commentInfo;
	}
	
}
