package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:17
 * @version 0.1 2012-12-17
 * FieldInfo对象存储一个域的信息
 */
public class FieldInfo extends CommonInfo{
	/**
	 * 域名
	 */
	private String name;
	
	/**
	 * 域的类型
	 */
	private String type;
	
	/**
	 * 去除数组及常见容器的等结构后获得的简单类型
	 */
	private List<String> simpleTypes;
	
	/**
	 * 域的可见性
	 */
	private String visibility;
	
	/**
	 * 是否是static域
	 */
	private boolean isStatic;
	
	/**
	 * 是否是Final域
	 */
	private boolean isFinal;
	// private boolean isTransient;
	// private boolean isVolatile;
	
	/**
	 * 域所属的类
	 */
	private ClassInfo classInfo;
	
	/**
	 * 域所属的接口
	 */
	private InterfaceInfo interfaceInfo;
	
	/**
	 * 域注释
	 */
	private String comment;
	private CommentInfo commentInfo;
	private String uuid;
	
	public FieldInfo() {
		simpleTypes = new ArrayList<String>();
		uuid = UUID.randomUUID().toString();
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

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getVisibility() {
		return visibility;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/*
	 * public boolean isTransient() { return isTransient; } public void
	 * setTransient(boolean isTransient) { this.isTransient = isTransient; }
	 * public boolean isVolatile() { return isVolatile; } public void
	 * setVolatile(boolean isVolatile) { this.isVolatile = isVolatile; } public
	 * void setProjectID(long projectID) { this.projectID = projectID; } public
	 * long getProjectID() { return projectID; }
	 */
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
	

	public CommentInfo getCommentInfo() {
		return commentInfo;
	}

	public void setCommentInfo(CommentInfo commentInfo) {
		this.commentInfo = commentInfo;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getHashName() {
		if (interfaceInfo != null) {
			return interfaceInfo.getHashName() + "|#|" + name;
		}
		if (classInfo != null) {
			return classInfo.getHashName() + "|#|" + name;
		}
		return "|#||#||#||#|" + name;
	}
	
	public boolean equals(Object o) {
		if(this.getHashName().equals(((FieldInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}

}
