

import java.util.HashMap;
import java.util.Map;

import pfr.plugins.parsers.javacode.CodeIndexes;
import pfr.plugins.parsers.javacode.PfrPluginForJavaCode;
import pfr.plugins.parsers.qa.QaGraphDbBuilder;
import graphfusion.CodeLinker;

public class GraphBuilderRunner
{
	
	String projectName=null;
	String remoteQaPath=null;
	
	String srcPath=null;
	String binPath=null;
	
	String qPath=null;
	String aPath=null;
	String cPath=null;
	String uPath=null;
	String plPath=null;
	String mPath=null;
	
	String issueFolderPath = null;
	
	String dbPath=null;
	String codeDbPath=null;
	String qaDbPath=null;
	String mailDbPath=null;
	String issueDbPath = null;
	
	public GraphBuilderRunner(String projectName){
		this.projectName=projectName;
		this.remoteQaPath="smb://192.168.2.252/TSR_share/StackOverflowData/stackoverflow-20150806/stackoveflow.com";
		
		this.srcPath="data/"+projectName+"/source_data/src";
		this.binPath="data/"+projectName+"/source_data/bin";
		
		this.qPath="data/"+projectName+"/source_data/qa/Questions.xml";
		this.aPath="data/"+projectName+"/source_data/qa/Answers.xml";
		this.cPath="data/"+projectName+"/source_data/qa/Comments.xml";
		this.uPath="data/"+projectName+"/source_data/qa/Users.xml";
		this.plPath="data/"+projectName+"/source_data/qa/PostLinks.xml";
		
		this.mPath="data/"+projectName+"/source_data/mbox";
		
		this.issueFolderPath="data/"+projectName+"/source_data/issue";
		
		this.dbPath="data/"+projectName+"/graphdb/full";
		this.codeDbPath="data/"+projectName+"/graphdb/code";
		this.qaDbPath="data/"+projectName+"/graphdb/qa";
		this.mailDbPath="data/"+projectName+"/graphdb/mail";
		this.issueDbPath="data/"+projectName+"/graphdb/issue";
	}
	
	public static void main(String[] args){
		new GraphBuilderRunner("apache-poi").run();;
	}
	
	public void run(){
		
		//从StackOverflow Dump中将和指定项目有关的内容单独抽取出来.
		//QaExtractor.extract(projectName, remoteQaPath,qPath,aPath,cPath,uPath,plPath);System.gc();
		
		long beginTime = System.currentTimeMillis();
		
		Map<GraphBuilder,Boolean> graphBuilders=new HashMap<GraphBuilder,Boolean>();
		graphBuilders.put(new PfrPluginForJavaCode(codeDbPath,srcPath,binPath),true);//构造代码结构图
		graphBuilders.put(new QaGraphDbBuilder(qaDbPath, qPath, aPath, cPath,uPath,plPath),true);//构造QA结构图
		//graphBuilders.put(new MailGraphBuilder(mailDbPath,mPath),true);//构造邮件结构图
		//graphBuilders.put(new IssueGraphBuilder(issueDbPath,issueFolderPath,projectName), true);//构建Issue结构图
		
		for (GraphBuilder graphBuilder:graphBuilders.keySet()){
			if (graphBuilders.get(graphBuilder))
				graphBuilder.run();
			graphBuilder.migrateTo(dbPath);
			System.gc();
			System.out.println(graphBuilder.name+" finished.");
		}
		
		System.out.println("Subgraphs are built and migrated together successfully.");
		
		//建立关联关系
		CodeIndexes codeIndexes=new CodeIndexes(dbPath);
		System.out.println("All code nodes are extracted.");
		
		new CodeLinker(dbPath,codeIndexes).run();System.gc();
		System.out.println("All finished!");
		
		long endTime = System.currentTimeMillis();
		long usageTime = endTime - beginTime;
		System.out.println("usage time:" + (usageTime/1000/60) + "mins " + (usageTime/1000%60) + "s.");
	}
}
