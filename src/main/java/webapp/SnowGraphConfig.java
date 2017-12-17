package webapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="snowGraph")
class SnowGraphConfig {
    private String projectPackageName;
    private String boltUrl;
    private String dataDir;
    private String githubAccessToken;
    public String getBoltUrl() {
        return boltUrl;
    }
    public void setBoltUrl(String boltUrl) {
        this.boltUrl = boltUrl;
    }
    public String getDataDir() {
        return dataDir;
    }
    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;

    }
    public String getGithubAccessToken() {
        return githubAccessToken;
    }
    public void setGithubAccessToken(String githubAccessToken) {
        this.githubAccessToken = githubAccessToken;
    }
    public String getProjectPackageName() {
        return projectPackageName;
    }
    public void setProjectPackageName(String projectPackageName) {
        this.projectPackageName = projectPackageName;
    }
}