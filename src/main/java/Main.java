import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pfr.framework.KnowledgeGraphBuilder;


public class Main
{

	public static void main(String[] args){
		@SuppressWarnings("resource")
		ApplicationContext context=new ClassPathXmlApplicationContext(args[0]);
		KnowledgeGraphBuilder graphBuilder=(KnowledgeGraphBuilder) context.getBean("graph");
		graphBuilder.buildGraph();
	}
	
}
