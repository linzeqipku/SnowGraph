package utils.parse;

import com.google.common.collect.Lists;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class ParseUtil {
	private static ASTParser parser = ASTParser.newParser(AST.JLS8);

	public static List<String> getMethodBodys(String code) {
		ASTNode node;
		node = parse(code, ASTParser.K_CLASS_BODY_DECLARATIONS);
		if (node instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) node;
			return getMethodsFromType(type).stream()
				.map(MethodDeclaration::getBody)
				.filter(x -> x != null)
				.filter(block -> !block.statements().isEmpty())
				.map(Block::toString).collect(Collectors.toList());
		}
		node = parse(code, ASTParser.K_STATEMENTS);
		if (!((Block) node).statements().isEmpty()) {
			return Lists.newArrayList(node.toString());
		}
		node = parse(code, ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) node;
		List<AbstractTypeDeclaration> types = cu.types();
		return types.stream()
			.map(ParseUtil::getMethodsFromType)
			.flatMap(List::stream)
			.map(MethodDeclaration::getBody)
			.filter(x -> x != null)
			.filter(block -> !block.statements().isEmpty())
			.map(Block::toString)
			.collect(Collectors.toList());
	}

	public static ASTNode parse(String code, int kind) {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setSource(code.toCharArray());
		parser.setKind(kind);
		parser.setResolveBindings(true);
		final ASTNode node = parser.createAST(null);
		return node;
	}

	public static List<MethodDeclaration> getMethodsFromType(AbstractTypeDeclaration type) {
		List<MethodDeclaration> methods = new ArrayList<>();

		if (type instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration) type;
			methods.addAll(Arrays.asList(td.getMethods()));
			for (TypeDeclaration innerType : td.getTypes()) methods.addAll(getMethodsFromType(innerType));
		}
		return methods;
	}


}
