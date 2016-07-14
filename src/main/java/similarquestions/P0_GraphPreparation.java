package similarquestions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import pfr.plugins.parsers.javacode.CodeIndexes;
import pfr.plugins.parsers.javacode.PfrPluginForJavaCode;
import pfr.plugins.parsers.stackoverflow.PfrPluginForStackOverflow;
import crawlers.qa.QaExtractor;
import graphfusion.CodeLinker;
import similarquestions.utils.SimilarQuestionTaskConfig;

public class P0_GraphPreparation {

	SimilarQuestionTaskConfig config=null;
	
	public static void main(String[] args){
		P0_GraphPreparation p=new P0_GraphPreparation("apache-poi");
		try {
			p.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public P0_GraphPreparation(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
	}
	
	public void run() throws IOException{
		if (!new File(config.qPath).exists()||!new File(config.aPath).exists()||
				!new File(config.cPath).exists()||!new File(config.uPath).exists()
				||!new File(config.plPath).exists()){
			FileUtils.cleanDirectory(new File(config.qaPath));
			QaExtractor.extract(config.projectName,config.qPath, config.aPath, config.cPath, config.uPath, config.plPath);
		}
		FileUtils.cleanDirectory(new File(config.graphPath));
		
		FileUtils.cleanDirectory(new File(config.tmpPath));
		PfrPluginForJavaCode codeGraphBuilder=new PfrPluginForJavaCode(config.tmpPath, config.srcPath, config.binPath);
		codeGraphBuilder.run();
		codeGraphBuilder.migrateTo(config.graphPath);
		
		FileUtils.cleanDirectory(new File(config.tmpPath));
		PfrPluginForStackOverflow qaGraphDbBuilder=new PfrPluginForStackOverflow(config.tmpPath, config.qPath, config.aPath, config.cPath, config.uPath, config.plPath);
		qaGraphDbBuilder.run();
		qaGraphDbBuilder.migrateTo(config.graphPath);
		
		CodeIndexes codeIndexes=new CodeIndexes(config.graphPath);
		CodeLinker codeLinker=new CodeLinker(config.graphPath, codeIndexes);
		codeLinker.run();
	}
	
}
