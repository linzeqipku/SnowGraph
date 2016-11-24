package pfr.framework;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class KnowledgeGraphBuilderTest
{

	public static void main(String[] args){
		@SuppressWarnings("resource")
		ApplicationContext context=new ClassPathXmlApplicationContext("codegraph.xml");
		KnowledgeGraphBuilder graphBuilder=(KnowledgeGraphBuilder) context.getBean("graph");
		graphBuilder.buildGraph();
	}
	
}
