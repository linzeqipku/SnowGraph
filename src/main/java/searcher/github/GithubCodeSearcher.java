package searcher.github;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class GithubCodeSearcher {

    private final String accessToken;
    private final String apiPrefix;

    public GithubCodeSearcher(String accessToken) {
        this.accessToken = accessToken;
        apiPrefix="https://api.github.com/search/code?access_token="+accessToken
                +"&q=language:Java+";
    }

    public void search(List<String> keywords){
        String url=apiPrefix+ StringUtils.join(keywords,"+");
    }

}
