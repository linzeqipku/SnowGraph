package apps;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import graphdb.framework.GraphBuilder;


public class BuildGraph {

    public static void main(String[] args) {
        run(args[0]);
    }

    public static void run(String configPath) {
        @SuppressWarnings("resource")
        ApplicationContext context = new FileSystemXmlApplicationContext(configPath);
        GraphBuilder graphBuilder = (GraphBuilder) context.getBean("graph");
        graphBuilder.buildGraph();
    }

}
