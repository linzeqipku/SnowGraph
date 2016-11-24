package pfr.plugins.parsers.javacode.extractor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import pfr.plugins.parsers.javacode.extractor.JavaASTVisitor;
public class JavaParser
{

	public static ElementInfoPool parse(String srcDir)
	{
		ElementInfoPool elementInfoPool = new ElementInfoPool(srcDir);

		Collection<File> javaFiles = FileUtils.listFiles(new File(srcDir), new String[]{"java"}, true);
		Set<String> srcPathSet = new HashSet<String>();
		Set<String> srcFolderSet = new HashSet<String>();
		for (File javaFile : javaFiles)
		{
			String srcPath = javaFile.getAbsolutePath();
			String srcFolderPath=javaFile.getParentFile().getAbsolutePath();
			srcPathSet.add(srcPath);
			srcFolderSet.add(srcFolderPath);
		}
		String[] srcPaths = new String[srcPathSet.size()];
		srcPathSet.toArray(srcPaths);
		String[] srcFolderPaths = new String[srcFolderSet.size()];
		srcFolderSet.toArray(srcFolderPaths);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(null, srcFolderPaths, null, true);
		parser.setResolveBindings(true);
		Map<String, String> options = new Hashtable<String, String>();
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		parser.setCompilerOptions(options);
		parser.setBindingsRecovery(true);
		parser.createASTs(srcPaths, null, new String[] {}, new FileASTRequestor(){
					@Override
					public void acceptAST(String sourceFilePath, CompilationUnit javaUnit){
						try{
							javaUnit.accept(new JavaASTVisitor(elementInfoPool, FileUtils.readFileToString(new File(sourceFilePath))));
						}
						catch (IOException e){
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, null);

		return elementInfoPool;
	}

}
