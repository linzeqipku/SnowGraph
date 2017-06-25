package graphdb.extractors.linkers.codetosnippet;

import com.google.common.collect.Lists;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.utils.GraphUtil;
import org.eclipse.jdt.core.dom.*;
import org.neo4j.graphdb.*;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class APIVisitor extends ASTVisitor {
	private Logger logger = Logger.getLogger(this.getClass().toString());

	private GraphDatabaseService db;
	private Node codeSnippet;
	private Map<String, SimpleType> variables = new HashMap<>();

	public APIVisitor(Node codeSnippet, GraphDatabaseService db) {
		this.db = db;
		this.codeSnippet = codeSnippet;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		System.out.println("=====");
		System.out.println(node);
		Expression receiver = node.getExpression();
		List<Node> methods = Lists.newArrayList(db.findNodes(Label.label(JavaCodeExtractor.METHOD), JavaCodeExtractor.METHOD_NAME, node.getName().toString()));
		if (receiver != null) {
			if (receiver instanceof SimpleName) {
				String variable = ((SimpleName) receiver).getIdentifier();
				if (resolveStaticMethod(variable, methods)) return true;
				SimpleType t = variables.get(variable);
				if (t != null) resolveMethod(t, methods);
				else resolveUnknownMethod(node.getName().toString());
			} else {
				// todo: 表达式类型推导
				resolveUnknownMethod(node.getName().toString());
			}

		}

		return true;
	}

	public boolean visit(VariableDeclarationStatement node) {
		if (node.getType() instanceof SimpleType) {
			node.fragments().forEach(f -> {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) f;
				// Todo: 考虑数组
				variables.put(fragment.getName().toString(), (SimpleType) node.getType());
			});
		} else {
			logger.log(Level.WARNING, "not support non-simple type declaration: " + node);
		}

		return true;
	}

	private boolean resolveStaticMethod(String className, List<Node> methods) {
		List<Node> classes = Lists.newArrayList(db.findNodes(Label.label(JavaCodeExtractor.CLASS), JavaCodeExtractor.CLASS_NAME, className));
		List<Node> result = methods.stream().filter(m -> m.getProperty(JavaCodeExtractor.METHOD_IS_STATIC).equals(true)).filter(m ->
			classes.stream().anyMatch(c -> GraphUtil.hasRelationShip(c, m, JavaCodeExtractor.HAVE_METHOD))
		).collect(Collectors.toList());
		if (result.size() > 1) {
			logger.log(Level.WARNING, "Can not resolve static method.");
			return true;
		}

		if (result.size() == 0) return false;

		createLink(codeSnippet, result.get(0));
		return true;
	}

	private boolean resolveMethod(@Nonnull Node type, List<Node> methods) {
		List<Node> result = methods.stream().filter(m -> GraphUtil.hasRelationShip(type, m, JavaCodeExtractor.HAVE_METHOD)).collect(Collectors.toList());
		if (result.size() > 1) {
			logger.log(Level.WARNING, "Can not resolve method.");
			return true;
		}

		if (result.size() == 1) {
			createLink(codeSnippet, result.get(0));
			return true;
		}

		Relationship superRel = type.getSingleRelationship(RelationshipType.withName(JavaCodeExtractor.CLASS_SUPERCLASS), Direction.OUTGOING);

		if (superRel == null) return false;

		Node superClass = superRel.getOtherNode(type);
		return resolveMethod(superClass, methods);
	}

	private boolean resolveMethod(SimpleType t, List<Node> methods) {
		Node classNode = db.findNode(Label.label(JavaCodeExtractor.CLASS), JavaCodeExtractor.CLASS_NAME, t.getName().getFullyQualifiedName());
		if (classNode == null) return false;
		return resolveMethod(classNode, methods);
	}

	private void resolveUnknownMethod(String methodName) {
		ResourceIterator<Node> method = db.findNodes(Label.label(JavaCodeExtractor.METHOD), JavaCodeExtractor.METHOD_NAME, methodName);
		List<Node> l = Lists.newArrayList(method);
		if (l.isEmpty()) return;

		if (l.size() > 1) System.out.println("Can not resolve method " + methodName);
		else createLink(codeSnippet, l.get(0));
	}

	private void createLink(Node from, Node to) {
		Iterable<Relationship> edges = from.getRelationships(RelationshipType.withName(CodeToSnippetExtractor.CONTAINS_API));
		for (Relationship edge : edges) {
			if (edge.getOtherNode(from).equals(to)) return;
		}
		from.createRelationshipTo(to, RelationshipType.withName(CodeToSnippetExtractor.CONTAINS_API));
	}
}
