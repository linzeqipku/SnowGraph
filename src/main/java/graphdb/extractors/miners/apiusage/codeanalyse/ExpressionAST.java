package graphdb.extractors.miners.apiusage.codeanalyse;

import org.eclipse.jdt.core.dom.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ExpressionAST {
	private Expression expressionNode;

	private HashSet<VariableAST> relevantVariables = new HashSet<>();
	private HashSet<VariableAST> usedVariables = new HashSet<>();
	private HashSet<VariableAST> definedVariables = new HashSet<>();

	public ExpressionAST(Expression node) {
		this.expressionNode = node;
		parseRelevantVariables();
	}

	private void copyRelevantVariables(ExpressionAST anotherExpressionAST) {
		if (anotherExpressionAST != null) {
			if (anotherExpressionAST.getDefinedVariables() != null)
				this.definedVariables.addAll(anotherExpressionAST.getDefinedVariables());
			if (anotherExpressionAST.getUsedVariables() != null)
				this.usedVariables.addAll(anotherExpressionAST.getUsedVariables());
			if (anotherExpressionAST.getRelevantVariables() != null)
				this.relevantVariables.addAll(anotherExpressionAST.getRelevantVariables());
		}
	}

	private void parseRelevantVariables() {
		if (expressionNode == null)
			return;

		if (expressionNode.getNodeType() == ASTNode.CAST_EXPRESSION) {
			// (cast)expression
			ExpressionAST castSubExpressionAST = new ExpressionAST(
				((CastExpression) expressionNode).getExpression());

			copyRelevantVariables(castSubExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
			// exp?exp:exp
			ExpressionAST conditionalExpressionAST = new ExpressionAST(
				((ConditionalExpression) expressionNode).getExpression());
			ExpressionAST thenExpressionAST = new ExpressionAST(
				((ConditionalExpression) expressionNode).getThenExpression());
			ExpressionAST elseExpressionAST = new ExpressionAST(
				((ConditionalExpression) expressionNode).getElseExpression());

			copyRelevantVariables(conditionalExpressionAST);
			copyRelevantVariables(thenExpressionAST);
			copyRelevantVariables(elseExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			// Expression InfixOperator Expression { InfixOperator Expression }
			ExpressionAST leftExpressionAST = new ExpressionAST(
				((InfixExpression) expressionNode).getLeftOperand());
			ExpressionAST rightExpressionAST = new ExpressionAST(
				((InfixExpression) expressionNode).getRightOperand());

			copyRelevantVariables(leftExpressionAST);
			copyRelevantVariables(rightExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
			// Expression instanceof Type
			ExpressionAST instanceExpressionAST = new ExpressionAST(
				((InstanceofExpression) expressionNode).getLeftOperand());

			copyRelevantVariables(instanceExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			// (expression)
			ExpressionAST parenthesizedExpressionAST = new ExpressionAST(
				((ParenthesizedExpression) expressionNode).getExpression());

			copyRelevantVariables(parenthesizedExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.POSTFIX_EXPRESSION) {
			// Expression PostfixOperator(++|--)
			// 必然改变值，故是defvar
			ExpressionAST postfixExpressionAST = new ExpressionAST(
				((PostfixExpression) expressionNode).getOperand());

			definedVariables.addAll(postfixExpressionAST.getRelevantVariables());
			relevantVariables.addAll(postfixExpressionAST.relevantVariables);

		}
		if (expressionNode.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			// prefixopr(++|--|!|~|+|-) exp
			ExpressionAST prefixExpressionAST = new ExpressionAST(
				((PrefixExpression) expressionNode).getOperand());
			PrefixExpression.Operator operator = ((PrefixExpression) expressionNode).getOperator();
			if (operator.equals(PrefixExpression.Operator.INCREMENT)
				|| operator.equals(PrefixExpression.Operator.DECREMENT)) {
				definedVariables.addAll(prefixExpressionAST.getRelevantVariables());
				relevantVariables.addAll(prefixExpressionAST.relevantVariables);
			}
			copyRelevantVariables(prefixExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.THIS_EXPRESSION) {
			// [ ClassName . ] this
			// parseExpression(((ThisExpression) expression).getQualifier());
		}
		if (expressionNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
			// { ExtendedModifier } Type VariableDeclarationFragment { ,
			// VariableDeclarationFragment }
			// VariableDeclarationFragment: Identifier { [] } [ = Expression ]
			@SuppressWarnings("unchecked")
			List<VariableDeclarationFragment> list = ((VariableDeclarationExpression) expressionNode) .fragments();
			for (VariableDeclarationFragment varDeclarationFragment : list) {
				VariableAST var = new VariableAST(((VariableDeclarationExpression) expressionNode).getType().toString(), varDeclarationFragment.getName().getIdentifier());

				definedVariables.add(var);
				relevantVariables.add(var);

				ExpressionAST initializerExpressionAST = new ExpressionAST(varDeclarationFragment.getInitializer());
				copyRelevantVariables(initializerExpressionAST);
			}
		}
		if (expressionNode.getNodeType() == ASTNode.METHOD_INVOCATION) {
			// [ Expression . ][ < Type { , Type } > ] Identifier ( [ Expression
			// {
			// , Expression } ] )

			@SuppressWarnings("unchecked")
			List<Expression> list = ((MethodInvocation) expressionNode).arguments();
			list.stream().map(ExpressionAST::new).forEach(this::copyRelevantVariables);

			// 形如“A.B(c)”的调用，hostExpression为A
			// 形如B(c)的调用，没有hostExpression,就会产生null ptr异常
			Expression hostExpression = ((MethodInvocation) expressionNode).getExpression();

			String[] defWords = {"set", "append"};

			if (hostExpression != null) {
				ExpressionAST hostExpressionAST = new ExpressionAST(hostExpression);

				String methodName = ((MethodInvocation) expressionNode).getName().toString();

				if (Arrays.stream(defWords).anyMatch(methodName::contains)) {
					definedVariables.addAll(hostExpressionAST.getRelevantVariables());
					relevantVariables.addAll(hostExpressionAST.relevantVariables);
				} else
					copyRelevantVariables(hostExpressionAST);

			}
		}
		if (expressionNode.getNodeType() == ASTNode.ASSIGNMENT) {
			// Expression AssignmentOperator(=|x=) Expression
			ExpressionAST leftExpressionAST = new ExpressionAST(
				((Assignment) expressionNode).getLeftHandSide());
			ExpressionAST rightExpressionAST = new ExpressionAST(
				((Assignment) expressionNode).getRightHandSide());

			definedVariables.addAll(leftExpressionAST.getRelevantVariables());
			relevantVariables.addAll(leftExpressionAST.getRelevantVariables());
			copyRelevantVariables(rightExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.SIMPLE_NAME) {
			// TODO:如果simpleName不是变量或对象名，而是诸如方法名怎么办？
			VariableAST var = new VariableAST(((SimpleName) expressionNode).getIdentifier());

			usedVariables.add(var);
			relevantVariables.add(var);
		}
		if (expressionNode.getNodeType() == ASTNode.QUALIFIED_NAME) {
			VariableAST var = new VariableAST(((QualifiedName) expressionNode).getName().getIdentifier());
			usedVariables.add(var);
			relevantVariables.add(var);
		}
		if (expressionNode.getNodeType() == ASTNode.ARRAY_ACCESS) {
			// Expression [ Expression ]
			ExpressionAST arrayExpressionAST = new ExpressionAST(
				((ArrayAccess) expressionNode).getArray());
			ExpressionAST indexExpressionAST = new ExpressionAST(
				((ArrayAccess) expressionNode).getIndex());

			copyRelevantVariables(arrayExpressionAST);
			copyRelevantVariables(indexExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.FIELD_ACCESS) {
			// Expression . Identifier
			ExpressionAST fieldExpressionAST = new ExpressionAST(
				((FieldAccess) expressionNode).getExpression());
			copyRelevantVariables(fieldExpressionAST);
		}
		if (expressionNode.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			// { [ Expression { , Expression} [ , ]] }
			// int s = new int [] {12, 16, anotherint};
			@SuppressWarnings("unchecked")
			List<Expression> list = ((ArrayInitializer) expressionNode).expressions();
			list.stream().map(ExpressionAST::new).forEach(this::copyRelevantVariables);
		}
		if (expressionNode.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			// [ Expression . ] new [ < Type { , Type } > ] Type ( [ Expression
			// { , Expression } ] ) [ AnonymousClassDeclaration ]
			// TODO:
		}
		if (expressionNode.getNodeType() == ASTNode.ARRAY_CREATION) {
			// new type [dimension] {initializer}
			ExpressionAST arrayInitExpressionAST = new ExpressionAST(
				((ArrayCreation) expressionNode).getInitializer());
			copyRelevantVariables(arrayInitExpressionAST);

			@SuppressWarnings("unchecked")
			List<Expression> list = ((ArrayCreation) expressionNode).dimensions();
			list.stream().map(ExpressionAST::new).forEach(this::copyRelevantVariables);
		}
	}

	public HashSet<VariableAST> getRelevantVariables() {
		return relevantVariables;
	}

	public HashSet<VariableAST> getUsedVariables() {
		return usedVariables;
	}

	public HashSet<VariableAST> getDefinedVariables() {
		return definedVariables;
	}

}
