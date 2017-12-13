package graphdb.framework;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphBuilder {

    private List<Extractor> extractors = new ArrayList<>();
    private String graphPath = null;
    private String baseGraphPath = null;

    public void addExtractor(Extractor extractor){
        extractors.add(extractor);
    }

    public void setExtractors(List<Extractor> extractors) {
        this.extractors = new ArrayList<>(extractors);
    }

    public void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
    }

    public void setBaseGraphPath(String baseGraphPath) {
        this.baseGraphPath = baseGraphPath;
    }

    public void buildGraph() {

    }

}
