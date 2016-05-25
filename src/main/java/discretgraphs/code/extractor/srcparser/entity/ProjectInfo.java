package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:43
 * @version 1.0
 * ProjectInfo对象存储一个项目的信息
 */
public class ProjectInfo extends CommonInfo {
	public static final int STATE_INITIAL = 0;
	public static final int STATE_FINISH_PUBLISH = 1;
	public static final int STATE_FINISH_PARSE = 2;
	public static final int STATE_UNDER_LDA = 3;
	public static final int STATE_FINISH_LDA = 4;
	public static final int STATE_UNDER_FILTER = 5;
	public static final int STATE_FINISH_FILTER = 6;
	public static final int STATE_UNDER_MERGE = 7;
	public static final int STATE_FINISH_MERGE = 8;
	//public static final int STATE_WAIT_STORE = 2;
		
	/**
	 * 项目的名称
	 */
	private String name;
	
	/**
	 * 用户填写的项目描述
	 */
	private String description;
	
	/**
	 * 项目的存储路径
	 */
	private String dirPath;
	
	/**
	 * 项目包含的包
	 */
	private List<PackageInfo> packageInfoList;
	
	private String uuid;
	
	private int state;
	
	private int userTopicNum;

	public ProjectInfo() {
		packageInfoList = new ArrayList<PackageInfo>();
		uuid = UUID.randomUUID().toString();
		userTopicNum = -1;
		state = STATE_INITIAL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public String getDirPath() {
		return dirPath;
	}

	public void setPackageInfoList(List<PackageInfo> packageInfoList) {
		this.packageInfoList = packageInfoList;
	}

	public List<PackageInfo> getPackageInfoList() {
		return packageInfoList;
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

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public String getHashName() {
		return name;
	}
	
	public boolean equals(Object o) {
		if(this.getHashName().equals(((ProjectInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @return the userTopicNum
	 */
	public int getUserTopicNum() {
		return userTopicNum;
	}

	/**
	 * @param userTopicNum the userTopicNum to set
	 */
	public void setUserTopicNum(int userTopicNum) {
		this.userTopicNum = userTopicNum;
	}
}
