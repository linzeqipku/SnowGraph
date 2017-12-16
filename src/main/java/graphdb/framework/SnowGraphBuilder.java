package graphdb.framework;

import graphdb.framework.annotations.SnowGraphBuilderConfig;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.yaml.snakeyaml.Yaml;
import webapp.SnowGraphContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SnowGraphBuilder {

    private SnowGraphBuilderConfig config;
    private List<Extractor> extractors=new ArrayList<>();

    public static void main(String[] args){
        SnowGraphBuilder.buildSnowGraph(SnowGraphContext.class.getResource("/").getPath()+"snowgraph-builder.yml");
    }

    public static void buildSnowGraph(String configPath){
        try {
            new SnowGraphBuilder(configPath).run();
        } catch (FileNotFoundException|ClassNotFoundException|IllegalAccessException|InstantiationException e) {
            e.printStackTrace();
        }
    }

    private SnowGraphBuilder(String configPath) throws FileNotFoundException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        config=new Yaml().loadAs(new FileInputStream(new File(configPath)),SnowGraphBuilderConfig.class);
        for (String extractorLine:config.getExtractors()){
            extractorLine=extractorLine.trim().replaceAll("\\s+"," ");
            int p=extractorLine.indexOf(" ");
            String extractorName=extractorLine;
            String argString="";
            if (p>0){
                extractorName=extractorLine.substring(0,p);
                argString=extractorLine.substring(p+1);
            }
            String[] args=argString.split(" ");
            Extractor extractor= (Extractor) Class.forName(extractorName).newInstance();
            extractor.config(args);
            extractors.add(extractor);
        }
    }

    private void run(){
        GraphDatabaseService graph=new GraphDatabaseFactory().newEmbeddedDatabase( new File(config.getGraphPath()) );
        for (int i=0;i<extractors.size();i++) {
            System.out.println(extractors.get(i).getClass().getName()+" started.");
            extractors.get(i).run(graph);
            extractors.set(i, new DefaultExtractor());
            System.gc();
        }
    }

}
