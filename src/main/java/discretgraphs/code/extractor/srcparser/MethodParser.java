package discretgraphs.code.extractor.srcparser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import discretgraphs.code.extractor.srcparser.entity.*;









/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-6-19 上午11:01:45
 */
public class MethodParser {
	private static ElementInfoPool elementInfoPool;
	public static void parseMethodBody(MethodInfo methodInfo, Block methodBody, ElementInfoPool thisElementInfoPool) {
		elementInfoPool = thisElementInfoPool;
		parseMethodBody(methodInfo, methodBody);
	}	
	
	@SuppressWarnings({ "unchecked" })
	private static void parseExpression(MethodInfo methodInfo, Expression expression) {
		if(expression == null) {
			return;
		}
		if(expression.getNodeType() == ASTNode.ARRAY_INITIALIZER)
		{
			List<Expression> expressions = ((ArrayInitializer) expression).expressions();
			for(Expression expression2 : expressions) {
				parseExpression(methodInfo, expression2);
			}
		}
		if(expression.getNodeType() == ASTNode.CAST_EXPRESSION)
		{
			parseExpression(methodInfo, ((CastExpression) expression).getExpression());
		}
		if(expression.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION)
		{
			parseExpression(methodInfo, ((ConditionalExpression) expression).getExpression());
			parseExpression(methodInfo, ((ConditionalExpression) expression).getElseExpression());
			parseExpression(methodInfo, ((ConditionalExpression) expression).getThenExpression());
		}
		if(expression.getNodeType() == ASTNode.INFIX_EXPRESSION)
		{
			parseExpression(methodInfo, ((InfixExpression) expression).getLeftOperand());
			parseExpression(methodInfo, ((InfixExpression) expression).getRightOperand());
		}
		if(expression.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION)
		{
			parseExpression(methodInfo, ((InstanceofExpression) expression).getLeftOperand());
		}
		if(expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION)
		{
			parseExpression(methodInfo, ((ParenthesizedExpression) expression).getExpression());
		}
		if(expression.getNodeType() == ASTNode.POSTFIX_EXPRESSION)
		{
			parseExpression(methodInfo, ((PostfixExpression) expression).getOperand());
		}
		if(expression.getNodeType() == ASTNode.PREFIX_EXPRESSION)
		{
			parseExpression(methodInfo, ((PrefixExpression) expression).getOperand());
		}
		if(expression.getNodeType() == ASTNode.THIS_EXPRESSION)
		{
			parseExpression(methodInfo, ((ThisExpression) expression).getQualifier());
		}
		if(expression.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION)
		{
			//System.out.println("ASTNode.VARIABLE_DECLARATION_EXPRESSION");
			//System.out.println(expression);
			String type = ((VariableDeclarationExpression) expression).getType().toString();
			List<VariableDeclaration> list = ((VariableDeclarationExpression) expression).fragments();
			for (int j = 0; j < list.size(); j++) {
				String name = list.get(j).getName().getFullyQualifiedName();	
				VariableInfo variableInfo = new VariableInfo();
				variableInfo.setMethodInfo(methodInfo);
				variableInfo.setName(name);
				variableInfo.setType(type);
				List<String> simpleTypeList = TypeDisposer.inferSimpleType(type);
				variableInfo.setSimpleTypes(simpleTypeList);
				if(! methodInfo.getVariableInfoList().contains(variableInfo))
				{
					methodInfo.getVariableInfoList().add(variableInfo);
				}
			}
		}
		if(expression.getNodeType() == ASTNode.METHOD_INVOCATION)
		{
			//System.out.println("methodInvocation: " + expression + " " + ((MethodInvocation) expression).getExpression());
			List<String> argumentList = new ArrayList<String>();
			List<Expression> list = ((MethodInvocation) expression).arguments();
			for(int i = 0; i < list.size(); i++)
			{
				argumentList.add(list.get(i).toString());
			}
			MethodInvocationInfo methodInvocationInfo = new MethodInvocationInfo();
			methodInvocationInfo.setMethodInfo(methodInfo);
			methodInvocationInfo.setName(expression.toString());
			//methodInvocationInfo.setArgumentList(argumentList);
			if(! methodInfo.getMethodInvocationInfoList().contains(methodInvocationInfo))
			{
				methodInfo.getMethodInvocationInfoList().add(methodInvocationInfo);
			}
			
			parseExpression(methodInfo, ((MethodInvocation) expression).getExpression());
		}
		if(expression.getNodeType() == ASTNode.ASSIGNMENT)
		{
			parseExpression(methodInfo, ((Assignment) expression).getLeftHandSide());
			parseExpression(methodInfo, ((Assignment) expression).getRightHandSide());
		}
		if(expression.getNodeType() == ASTNode.NUMBER_LITERAL)
		{}
		if(expression.getNodeType() == ASTNode.STRING_LITERAL)
		{
			//VariableInfo variableInfo = new VariableInfo();
			//variableInfo.setMethodInfo(methodInfo);
			//variableInfo.setName(expression.toString());
			//variableInfo.setType("String");
			//if(! methodInfo.getVariableInfoList().contains(variableInfo))
			//{
			//	methodInfo.getVariableInfoList().add(variableInfo);
			//}
		}
		if(expression.getNodeType() == ASTNode.SIMPLE_NAME)
		{}
		if(expression.getNodeType() == ASTNode.QUALIFIED_NAME)
		{
			//System.out.println("qualifiedName: " + expression + " " + ((QualifiedName) expression).getQualifier());
			//parseExpression(((MethodInvocation) expression).getExpression());
			parseExpression(methodInfo, ((QualifiedName) expression).getQualifier());
		}
	}

	@SuppressWarnings("unchecked")
	public static void parseMethodBody(MethodInfo methodInfo, Block methodBody){
		//System.out.println("method content" + methodInfo.getMethodContent() + "#########");
		List<Statement> statementList = methodBody.statements();
		List<Statement> statements = new ArrayList<Statement>();
		for(int i = 0; i < statementList.size(); i++)
		{
			statements.add(statementList.get(i));
		}
		//System.out.println("######################");
		int statementCount = 0;
		for(int i = 0; i < statements.size(); i++){
			
			if(statements.get(i).getNodeType() == ASTNode.BLOCK)
			{
				List<Statement> blockStatements = ((Block) statements.get(i)).statements();
				for(int j = 0; j < blockStatements.size(); j++)
				{
					statements.add(i + j + 1, blockStatements.get(j));
				}
				continue;
			}
			
				
			
			if(statements.get(i).getNodeType() == ASTNode.ASSERT_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.ASSERT_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((AssertStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);					
				}
				expression = ((AssertStatement) statements.get(i)).getMessage();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);					
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.BREAK_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.BREAK_STATEMENT");
				//System.out.println(statements.get(i));
			}
			if(statements.get(i).getNodeType() == ASTNode.CONTINUE_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.CONTINUE_STATEMENT");
				//System.out.println(statements.get(i));
			}
			
			if(statements.get(i).getNodeType() == ASTNode.DO_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.DO_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((DoStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement doBody = ((DoStatement) statements.get(i)).getBody();
				if(doBody != null)
				{
					statements.add(i + 1, doBody);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.ENHANCED_FOR_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((EnhancedForStatement) statements.get(i)).getExpression();
				//System.out.println(expression);
				String type = ((EnhancedForStatement) statements.get(i)).getParameter().getType().toString();
				String name = ((EnhancedForStatement) statements.get(i)).getParameter().getName().getFullyQualifiedName();
				VariableInfo variableInfo = new VariableInfo();
				variableInfo.setMethodInfo(methodInfo);
				variableInfo.setName(name);
				variableInfo.setType(type);
				List<String> simpleTypeList = TypeDisposer.inferSimpleType(type);
				variableInfo.setSimpleTypes(simpleTypeList);
				if(! methodInfo.getVariableInfoList().contains(variableInfo))
				{
					methodInfo.getVariableInfoList().add(variableInfo);
				}
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement forBody = ((EnhancedForStatement) statements.get(i)).getBody();
				if(forBody != null)
				{
					statements.add(i + 1, forBody);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.EMPTY_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.EMPTY_STATEMENT");
				//System.out.println(statements.get(i));
			}
			if(statements.get(i).getNodeType() == ASTNode.EXPRESSION_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.EXPRESSION_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((ExpressionStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
			}		
			if(statements.get(i).getNodeType() == ASTNode.FOR_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.FOR_STATEMENT");
				//System.out.println(statements.get(i));
				List<Expression> list = ((ForStatement) statements.get(i)).initializers();
				for(int j = 0; j < list.size(); j++)
				{
					parseExpression(methodInfo, list.get(j));
				}
				Expression expression = ((ForStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement forBody = ((ForStatement) statements.get(i)).getBody();
				if(forBody != null)
				{
					statements.add(i + 1, forBody);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.IF_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.IF_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((IfStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement thenStatement = ((IfStatement) statements.get(i)).getThenStatement();
				Statement elseStatement = ((IfStatement) statements.get(i)).getElseStatement();
				if(elseStatement != null)
				{
					statements.add(i + 1, elseStatement);
				}
				if(thenStatement != null)
				{
					statements.add(i + 1, thenStatement);				
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.LABELED_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.LABELED_STATEMENT");
				//System.out.println(statements.get(i));
			}
			if(statements.get(i).getNodeType() == ASTNode.RETURN_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.RETURN_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((ReturnStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.SWITCH_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.SWITCH_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((SwitchStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				List<Statement> switchStatements = ((SwitchStatement) statements.get(i)).statements();
				for(int j = 0; j < switchStatements.size(); j++)
				{
					statements.add(i + j + 1, switchStatements.get(j));
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.THROW_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.THROW_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((ThrowStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.TRY_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.TRY_STATEMENT");
				//System.out.println(statements.get(i));
				Statement tryStatement = ((TryStatement) statements.get(i)).getBody();
				if(tryStatement != null)
				{
					statements.add(i + 1, tryStatement);
					//System.out.println("tryStatement= " + tryStatement);
				}
				continue;
			}
			if(statements.get(i).getNodeType() == ASTNode.TYPE_DECLARATION_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.TYPE_DECLARATION_STATEMENT");
				//System.out.println(statements.get(i));
			}
			if(statements.get(i).getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT)
			{
				
				//System.out.println("##########################################");
				//System.out.println("ASTNode.VARIABLE_DECLARATION_STATEMENT");
				//System.out.println(statements.get(i));
				
				String type = ((VariableDeclarationStatement) statements.get(i)).getType().toString();
				List<VariableDeclaration> list = ((VariableDeclarationStatement) statements.get(i)).fragments();
				for(int j = 0; j < list.size(); j++){
					//System.out.println(list.get(j).getName().getFullyQualifiedName());
					VariableInfo variableInfo = new VariableInfo();
					variableInfo.setMethodInfo(methodInfo);
					variableInfo.setName(list.get(j).getName().getFullyQualifiedName());
					variableInfo.setType(type);
					List<String> simpleTypeList = TypeDisposer.inferSimpleType(type);
					variableInfo.setSimpleTypes(simpleTypeList);
					if(! methodInfo.getVariableInfoList().contains(variableInfo))
					{
						methodInfo.getVariableInfoList().add(variableInfo);
					}
					
					parseExpression(methodInfo, list.get(j).getInitializer());
				}
			}			
			if(statements.get(i).getNodeType() == ASTNode.WHILE_STATEMENT)
			{
				//System.out.println("##########################################");
				//System.out.println("ASTNode.WHILE_STATEMENT");
				//System.out.println(statements.get(i));
				Expression expression = ((WhileStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement whileBody = ((WhileStatement) statements.get(i)).getBody();
				if(whileBody != null)
				{
					statements.add(i + 1, whileBody);
				}
			}
			//System.out.println(statements.get(i).toString() + "#########");
			StatementInfo statementInfo = new StatementInfo();
			
			statementInfo.setCount(statementCount);
			statementCount++;
			statementInfo.setStatementString(statements.get(i).toString().trim());

//			String javaDoc = null;
			
			//之前对于statement只取一句注释
			//CommentInfo commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(statements.get(i).getStartPosition());
			
			//存在有的一句话对应多行注释
			List<CommentInfo> commentInfoList = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(statements.get(i).getStartPosition(), statements.get(i).getStartPosition() + statements.get(i).getLength());
			
			if (commentInfoList != null && commentInfoList.size() > 0) {
				
//				System.out.println("##########");
//				System.out.println(statementInfo.getStatementString());
//				System.out.println(commentInfo.getCommentString());
//				System.out.println("hehe");
//				System.out.println("##########");
				statementInfo.setCommentInfoList(commentInfoList);
				String commentString = "";
				for (CommentInfo commentInfo : commentInfoList) {
					commentString = commentString + ";" + commentInfo.getCommentString();
				}
				statementInfo.setComment(commentString);
			}
			else {
				statementInfo.setComment("");
			}
			methodInfo.addStatementInfo(statementInfo);
		
		}
		//for(int i = 0; i < statements.size(); i++)
		//{
			//System.out.println(statements.get(i));
			//methodInfo.setMethodContent(methodInfo.getMethodContent() + statements.get(i).toString() + "\n");
		//}
	}

}
