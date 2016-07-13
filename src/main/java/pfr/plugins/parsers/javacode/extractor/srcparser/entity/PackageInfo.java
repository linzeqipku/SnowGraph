package pfr.plugins.parsers.javacode.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:32
 * @version 1.0
 * PackageInfo对象存储一个包的信息
 */
public class PackageInfo extends CommonInfo {
	/**
	 * 包名
	 */
	private String name;
	
	/**
	 * 包所属的项目
	 */
	private ProjectInfo projectInfo;
	
	/**
	 * 包注释
	 */
	private String comment = "";
	
	/**
	 * 包注释类
	 */
	private CommentInfo commentInfo;
	
	/**
	 * 包所包含的文件
	 */
	private List<GroupInfo> groupInfoList;
	
	private String uuid;

	public PackageInfo() {
		groupInfoList = new ArrayList<GroupInfo>();
		uuid = UUID.randomUUID().toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProjectInfo(ProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
	}

	public ProjectInfo getProjectInfo() {
		return projectInfo;
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

	@Override
	public String getHashName() {
		if(projectInfo != null)
		{
			return projectInfo.getHashName() + "|#|" + name;
		}
		else {
			return "|#|" + name;
		}
	}

	public void setGroupInfoList(List<GroupInfo> groupInfoList) {
		this.groupInfoList = groupInfoList;
	}

	public List<GroupInfo> getGroupInfoList() {
		return groupInfoList;
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	public CommentInfo getCommentInfo() {
		return commentInfo;
	}

	public void setCommentInfo(CommentInfo commentInfo) {
		this.commentInfo = commentInfo;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object o) {
		if(this.getHashName().equals(((PackageInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}
}
