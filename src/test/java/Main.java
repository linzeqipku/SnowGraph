import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pfr.framework.KnowledgeGraphBuilder;

/**
 * Created by maxkibble on 2016/11/24.
 */
public class Main {
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("codegraph.xml");
        KnowledgeGraphBuilder graphBuilder = (KnowledgeGraphBuilder) context.getBean("graph");
        graphBuilder.buildGraph();
    }
}
