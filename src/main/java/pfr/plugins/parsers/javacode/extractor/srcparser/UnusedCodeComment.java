package pfr.plugins.parsers.javacode.extractor.srcparser;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

public class UnusedCodeComment {

	public static boolean isUnusedCodeComment(String comment) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(comment.toCharArray());
		try {
			parser.setKind(ASTParser.K_STATEMENTS);
			Block bl = (Block) parser.createAST(null);
			if (bl.statements().size() > 0)
				return true;
		}
		catch (Exception e) {
			System.out.println("Failed");
			return false;
		}
		return false;
	}

	public static void main(String args[]) {
		if (isUnusedCodeComment("class ll{} class na{int x}"))
			System.out.println("OK");
		else
			System.out.println("NO");
		if (isUnusedCodeComment("class ll{} \n class na{int x;}"))
			System.out.println("OK");
		else
			System.out.println("NO");
		if (isUnusedCodeComment("class ll{} class na{int x;} int %;"))
			System.out.println("OK");
		else
			System.out.println("NO");
		if (isUnusedCodeComment("class ll{} class na{int x;} 阿斯顿 阿萨德;"))
			System.out.println("OK");
		else
			System.out.println("NO");
		System.out.print("Finished");
	}
}
