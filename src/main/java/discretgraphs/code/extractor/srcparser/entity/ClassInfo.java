package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:15:25
 * @version 0.1 2012-12-15
 * ClassInfo对象存储一个类的信息
 * @author Lingxiao 2014-5-28
 */
public class ClassInfo extends CommonInfo{	
	/**
	 * 类名
	 */
	private String name;
	
	/**
	 * 类全限名，包含完整包路径
	 */
	private String fullName;
	
	/**
	 * file path of this java file
	 * @author Lingxiao
	 */
	private String filePath;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * 类的可见性
	 */
	private String visibility = "packge";
	
	/**
	 * 是否是抽象类
	 */
	private boolean isAbstract = false;
	
	/**
	 * 是否是final类
	 */
	private boolean isFinal = false;
	// private boolean isStrictfp;
	// private long superClassID;
	
	/**
	 * 类所属的文件
	 */
	private GroupInfo groupInfo;
	
	/**
	 * 类所继承的基类
	 */
	private String superClassType;
	
	/**
	 * 类所实现的接口
	 */
	private List<String> superInterfaceTypeList;
	
	/**
	 * 类注释
	 */
	private String comment;
	
	private CommentInfo commentInfo;
	/**
	 * 类所包含的域
	 */
	private List<FieldInfo> fieldInfoList;
	
	/**
	 * 类所包含的方法
	 */
	private List<MethodInfo> methodInfoList;
	
	private String uuid;

	public ClassInfo() {
		superInterfaceTypeList = new ArrayList<String>();
		fieldInfoList = new ArrayList<FieldInfo>();
		methodInfoList = new ArrayList<MethodInfo>();
		uuid = UUID.randomUUID().toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getVisibility() {
		return visibility;
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

	/*
	 * public boolean isStrictfp() { return isStrictfp; } public void
	 * setStrictfp(boolean isStrictfp) { this.isStrictfp = isStrictfp; } public
	 * long getSuperClassID() { return superClassID; } public void
	 * setSuperClassID(long superClassID) { this.superClassID = superClassID; }
	 */
	public void setGroupInfo(GroupInfo groupInfo) {
		this.groupInfo = groupInfo;
	}

	public GroupInfo getGroupInfo() {
		return groupInfo;
	}

	public void setSuperClassType(String superClassType) {
		this.superClassType = superClassType;
	}

	public String getSuperClassType() {
		return superClassType;
	}

	public void setSuperInterfaceTypeList(List<String> superInterfaceTypeList) {
		this.superInterfaceTypeList = superInterfaceTypeList;
	}

	public List<String> getSuperInterfaceTypeList() {
		return superInterfaceTypeList;
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

	public void setFieldInfoList(List<FieldInfo> fieldInfoList) {
		this.fieldInfoList = fieldInfoList;
	}

	public List<FieldInfo> getFieldInfoList() {
		return fieldInfoList;
	}

	public void setMethodInfoList(List<MethodInfo> methodInfoList) {
		this.methodInfoList = methodInfoList;
	}

	public List<MethodInfo> getMethodInfoList() {
		return methodInfoList;
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
	
	
	

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public CommentInfo getCommentInfo() {
		return commentInfo;
	}

	public void setCommentInfo(CommentInfo commentInfo) {
		this.commentInfo = commentInfo;
	}

	public String getHashName() {
		if(groupInfo != null)
		{
			return groupInfo.getHashName() + "|#|" + name;
		}
		else {
			return "|#||#||#|" + name;
		}
	}
	
	public boolean equals(Object o) {
		if(this.getHashName().equals(((ClassInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}


}
