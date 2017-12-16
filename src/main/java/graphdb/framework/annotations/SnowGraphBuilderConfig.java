package graphdb.framework.annotations;

import java.util.List;

public class SnowGraphBuilderConfig {
    private String graphPath;
    private List<String> extractors;

    public String getGraphPath() {
        return graphPath;
    }

    public void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
    }

    public List<String> getExtractors() {
        return extractors;
    }

    public void setExtractors(List<String> extractors) {
        this.extractors = extractors;
    }
}