package app;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import framework.KnowledgeGraphBuilder;


public class Main {

    public static void main(String[] args) {
        run(args[0]);
    }

    public static void run(String configPath) {
        @SuppressWarnings("resource")
        ApplicationContext context = new FileSystemXmlApplicationContext(configPath);
        KnowledgeGraphBuilder graphBuilder = (KnowledgeGraphBuilder) context.getBean("graph");
        graphBuilder.buildGraph();
    }

}
