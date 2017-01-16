package extractors.miners.mailqa.entity;

public class Project {

	private int		projectID;

	private String	projectName;

	private String	projectUrl;

	private String	mboxPath;

	public String getMboxPath() {
		return mboxPath;
	}

	public void setMboxPath(String mboxPath) {
		this.mboxPath = mboxPath;
	}

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectUrl() {
		return projectUrl;
	}

	public void setProjectUrl(String projectUrl) {
		this.projectUrl = projectUrl;
	}

}
