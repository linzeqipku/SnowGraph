package searcher.codepattern.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParseUtil {
    private static Logger logger = LoggerFactory.getLogger(ParseUtil.class);

	public static int count = 0;

	public static List<String> parseFileContent(String code) {
		try {
			CompilationUnit cu = JavaParser.parse(code);
			List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
			count += methods.size();
			return methods.stream()
				.map(MethodDeclaration::toString)
				.collect(Collectors.toList());
		} catch (ParseProblemException e) {
		    logger.warn("Could not parse code: ");
		    logger.warn(code);
			return new ArrayList<>();
		}
	}
}
