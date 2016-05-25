package discretgraphs.code.extractor.srcparser;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

public class CommentUtils {
	public static boolean isUnusedCodeComment(String comment){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(comment.toCharArray());
		try{
			parser.setKind(ASTParser.K_STATEMENTS);
			Block bl = (Block) parser.createAST(null);
			if (bl.statements().size() > 0) return true;			
		}catch (Exception e){
			System.out.println("Failed");
			return false;
		}
		return false;
	} 
	
	public static boolean isTODOcomment(String comment) {
		return comment.startsWith("TODO");
	}
	
	public static boolean isUsefulComment(String comment) {
		return !(isUnusedCodeComment(comment) || isTODOcomment(comment));
	}
	
	public static String trimComment(String comment) {
		String content = "";
		if (comment.startsWith("/*")) {
			int i = 2;
			while (comment.charAt(i) == '*') {
				i++;
			}
			int l = comment.length();
			while(i < l - 2) {
				char c = comment.charAt(i);
				if (c == '\n') {
					i++ ;
					while (comment.charAt(i) == ' ' ||
							comment.charAt(i) == '\t') {
						i++;
					}
					if (comment.charAt(i) == '*') {
						i++;
					}
				} else {
					content += comment.charAt(i);
					i++;
				}
			}
		} else {
			content = comment.substring(2);
		}
		return content.trim();
	}
	
	public static void main(String args[]){
		if (isUnusedCodeComment("class ll{} class na{int x}"))
		System.out.println("OK");else System.out.println("NO");
		if (isUnusedCodeComment("class ll{} \n class na{int x;}"))
		System.out.println("OK");else System.out.println("NO");
		if (isUnusedCodeComment("class ll{} class na{int x;} int %;"))
		System.out.println("OK");else System.out.println("NO");
		if (isUnusedCodeComment("阿斯顿 阿萨德;"))
		System.out.println("OK");else System.out.println("NO");
		System.out.print("Finished");
	}
}
