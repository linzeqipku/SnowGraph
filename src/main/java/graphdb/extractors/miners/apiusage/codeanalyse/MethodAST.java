package graphdb.extractors.miners.apiusage.codeanalyse;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class MethodAST extends ASTVisitor {
	private MethodDeclaration methodDeclarationNode;

	private List<MethodInvocation> methodInvocations;
	private List<StatementAST> statements;
	private List<VariableAST> variableDeclarations;

	public MethodAST(MethodDeclaration methodDeclarationNode) {
		statements = new ArrayList<>();
		methodInvocations = new ArrayList<>();
		variableDeclarations = new ArrayList<>();

		this.methodDeclarationNode = methodDeclarationNode;
		// visit分析调用和变量定义
		this.methodDeclarationNode.accept(this);

		parseStatements();
	}

	private void parseStatements() {
		Block body = methodDeclarationNode.getBody();
		if (body != null && body.statements() != null) {
			List<Statement> rawStatements = body.statements();
			for (Statement curStmt : rawStatements) {
				StatementAST curStatementAST = new StatementAST(curStmt);
				statements.add(curStatementAST);
			}
		}
	}

	public MethodDeclaration getMethodDeclarationNode() {
		return methodDeclarationNode;
	}

	public List<MethodInvocation> getMethodInvocations() {
		return methodInvocations;
	}

	public List<StatementAST> getStatements() {
		return statements;
	}

	public boolean visit(MethodInvocation node) {
		methodInvocations.add(node);

		return true;
	}

	public boolean visit(VariableDeclarationStatement node) {
		List<VariableDeclaration> list = node.fragments();
		for (VariableDeclaration aList : list) {
			VariableAST variable = new VariableAST(node.getType().toString(), aList.getName().toString(), node);

			if (!variableDeclarations.contains(variable)) variableDeclarations.add(variable);
		}

		return true;
	}

	public String toString() {
		return methodDeclarationNode.toString();
	}

	public List<VariableAST> getVariableDeclarations() {
		return variableDeclarations;
	}

}
