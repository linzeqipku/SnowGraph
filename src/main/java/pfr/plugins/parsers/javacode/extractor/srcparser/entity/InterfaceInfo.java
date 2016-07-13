package pfr.plugins.parsers.javacode.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:20
 * @version 0.1 2012-12-15
 * InterfaceInfo对象存储一个接口的信息
 */
public class InterfaceInfo extends CommonInfo {
	/**
	 * 接口名
	 */
	private String name;
	
	/**
	 * 接口全路径名，包含包路径
	 */
	private String fullName;
	
	private String filePath;
	
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * 接口的可见性
	 */
	private String visibility = "package";
	
	/**
	 * 接口所属的文件
	 */
	private GroupInfo groupInfo;
	
	/**
	 * 接口继承的接口，java语言里这种可以是多继承
	 */
	private List<String> superInterfaceTypeList;
	
	/**
	 * 接口注释
	 */
	private String comment;
	
	/**
	 * 接口注释 CommentInfo
	 */
	private CommentInfo commentInfo;
	
	/**
	 * 接口所包含的域
	 */
	private List<FieldInfo> fieldInfoList;
	
	/**
	 * 接口所包含的方法
	 */
	private List<MethodInfo> methodInfoList;
	
	private String uuid;

	public InterfaceInfo() {
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

	public void setGroupInfo(GroupInfo groupInfo) {
		this.groupInfo = groupInfo;
	}

	public GroupInfo getGroupInfo() {
		return groupInfo;
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
	
	

	public CommentInfo getCommentInfo() {
		return commentInfo;
	}

	public void setCommentInfo(CommentInfo commentInfo) {
		this.commentInfo = commentInfo;
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

	@Override
	public String getHashName() {
		if(groupInfo != null)
		{
			return groupInfo.getHashName() + "|#|" + name;
		}
		else {
			return "|#||#||#|" + name;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this.getHashName().equals(((InterfaceInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}

}
