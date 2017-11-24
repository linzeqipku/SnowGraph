package graphdb.extractors.miners.apiusage.codeanalyse;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StatementAST
{
	// 改变block中的子statement，如何影响原代码块？

	private final static int NULL_TYPE = 0;// 错误类型
	private final static int PRIMITIVE_STATEMENT = 1; // 语句语法构成中不包括Statement的语句类型
	private final static int BLOCK_STATEMENT = 2; // block|synchronized
	private final static int NESTED_STATEMENT = 3; // do|while|for|foreach|labeled
													// body(Statement)-->block(statements)
	private final static int IF_STATEMENT = 4; // if-else
	private final static int SWITCH_STATEMENT = 5; // switch() { statements }
	private final static int TRY_STATEMENT = 6; // try block {catchclause}
												// [finally block]
	private final static int TYPE_DECLARATION_STATEMENT = 7; // type|enum
															// declaration

	private final static int TEST_NULL = 100;
	private final static int TEST_ASSERTION = 101;
	private final static int TEST_DECLARATION = 102;
	private final static int TEST_USAGE = 103;
	public final static int TEST_METHODINVOCATION = 104;

	// 2013.05.05
	// private Method method;
	// private MethodAnalyzer methodAnalyzer;
	// private List<MethodInvocationData> methodInvocationInfos;

	private Statement statementNode;

	// 包含子语句的语句的子语句。
	private List<StatementAST> subStatements; // block|do|while|for|enhancedFor|switch|if-then|try1
	private List<StatementAST> subStatements2; // if2-else|try2-finally

	private HashSet<VariableAST> relevantVariables;

	private HashSet<VariableAST> usedVariables;
	private HashSet<VariableAST> definedVariables;

	// Reserved:语句标记，如assert-var1,used-var1,
	private String marker = "";

	// Statement的子类型，对应于最前面所列的8种语法结构类型
	private int statementType = NULL_TYPE;

	// Reserved:语句在测试方法中类型，如assert，declare，invoke
	private int testType = TEST_NULL;

	public StatementAST(Statement statement)
	{
		this.statementNode = statement;

		relevantVariables = new HashSet<>();

		usedVariables = new HashSet<>();
		definedVariables = new HashSet<>();

		parse();
	}

	private void parse()
	{
		initiateTestType();
		parseRelevantVariables();
	}

	// 2013.5.6
	public boolean hasBlock()
	{
		return statementType >= 2 && statementType <= 6;
	}

	private int initiateTestType()
	{
		this.testType = TEST_USAGE;
		int syntaxType = statementNode.getNodeType();
		if (syntaxType == ASTNode.EXPRESSION_STATEMENT)
		{
			if (statementNode.toString().indexOf("assert") >= 0)
				this.testType = TEST_ASSERTION;
		}
		else if (syntaxType == ASTNode.VARIABLE_DECLARATION_STATEMENT)
		{
			this.testType = TEST_DECLARATION;
		}

		return this.testType;
	}

	// public void visitMethodInvocations()
	// {
	// new StatementVisitor(this);
	// }

	private void copyRelevantVariables(StatementAST anotherStatementAST)
	{
		if (anotherStatementAST != null)
		{
			if (anotherStatementAST.getDefinedVariables() != null)
				this.definedVariables.addAll(anotherStatementAST.getDefinedVariables());
			if (anotherStatementAST.getUsedVariables() != null)
				this.usedVariables.addAll(anotherStatementAST.getUsedVariables());
			if (anotherStatementAST.getRelevantVariables() != null)
				this.relevantVariables.addAll(anotherStatementAST.getRelevantVariables());
		}
	}

	private void copyRelevantVariables(ExpressionAST expressionAST)
	{
		if (expressionAST != null)
		{
			if (expressionAST.getDefinedVariables() != null)
				this.definedVariables.addAll(expressionAST.getDefinedVariables());
			if (expressionAST.getUsedVariables() != null)
				this.usedVariables.addAll(expressionAST.getUsedVariables());
			if (expressionAST.getRelevantVariables() != null)
				this.relevantVariables.addAll(expressionAST.getRelevantVariables());
		}
	}

	private void parseRelevantVariables()
	{
		if (statementNode == null)
			return;

		int astNodeType = statementNode.getNodeType();

		if (astNodeType == ASTNode.ASSERT_STATEMENT)
		{
			// AssertStatement:
			// assert Expression1 ;
			// assert Expression1 : Expression2 ;
			statementType = PRIMITIVE_STATEMENT;
		}
		if (astNodeType == ASTNode.BLOCK)
		{
			// Block:
			// '{ { Statement } '}
			statementType = BLOCK_STATEMENT;

			parseBodyToSubStatements((Block) statementNode);
		}
		if (astNodeType == ASTNode.BREAK_STATEMENT)
		{
			// break [ Identifier ] ;
			statementType = PRIMITIVE_STATEMENT;
		}
		if (astNodeType == ASTNode.CONSTRUCTOR_INVOCATION)
		{
			// ConstructorInvocation:
			// [ < Type { , Type } > ] this ( [ Expression { , Expression } ] );
			statementType = PRIMITIVE_STATEMENT;

			@SuppressWarnings("unchecked")
			List<Expression> list = ((ConstructorInvocation) statementNode).arguments();
			for (Expression expression : list)
			{
				ExpressionAST arguExpressionAST = new ExpressionAST(expression);
				copyRelevantVariables(arguExpressionAST);
			}

		}
		if (astNodeType == ASTNode.CONTINUE_STATEMENT)
		{
			// continue [ Identifier ] ;
			statementType = PRIMITIVE_STATEMENT;
		}
		if (astNodeType == ASTNode.DO_STATEMENT)
		{
			// do Statement while ( Expression ) ;
			statementType = NESTED_STATEMENT;

			DoStatement stmt = (DoStatement) statementNode;

			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			parseBodyToSubStatements(stmt.getBody());
		}
		if (astNodeType == ASTNode.EMPTY_STATEMENT)
		{
			// ;
			statementType = PRIMITIVE_STATEMENT;
		}
		if (astNodeType == ASTNode.ENHANCED_FOR_STATEMENT)
		{
			// for ( FormalParameter : Expression )
			// Statement
			statementType = NESTED_STATEMENT;

			EnhancedForStatement stmt = (EnhancedForStatement) statementNode;

			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			parseBodyToSubStatements(stmt.getBody());
		}
		if (astNodeType == ASTNode.EXPRESSION_STATEMENT)
		{
			// StatementExpression ;
			statementType = PRIMITIVE_STATEMENT;

			ExpressionStatement stmt = (ExpressionStatement) statementNode;
			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));
		}
		if (astNodeType == ASTNode.FOR_STATEMENT)
		{
			// for ( [ ForInit ]; [ Expression ] ; [ ForUpdate ] )
			// Statement
			// ForInit: Expression { , Expression }
			// ForUpdate: Expression { , Expression }
			statementType = NESTED_STATEMENT;

			ForStatement stmt = (ForStatement) statementNode;

			// 分析for循环体
			parseBodyToSubStatements(stmt.getBody());

			// ForInit
			List<Expression> initExpressions = stmt.initializers();
			for (Expression initExpression : initExpressions)
			{
				copyRelevantVariables(new ExpressionAST(initExpression));
			}

			// ForConditionalExpression
			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			// ForUpdate
			List<Expression> updateExpressions = stmt.updaters();
			for (Expression updateExpression : updateExpressions)
			{
				copyRelevantVariables(new ExpressionAST(updateExpression));
			}
		}
		if (astNodeType == ASTNode.IF_STATEMENT)
		{
			// if ( Expression ) Statement [ else Statement]
			statementType = IF_STATEMENT;

			IfStatement stmt = (IfStatement) statementNode;

			// IfConditionalExpression
			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			// 分析then block
			Statement thenStatement = stmt.getThenStatement();
			parseBodyToSubStatements(thenStatement);

			// 分析else block
			Statement elseStatement = stmt.getElseStatement();
			parseBodyToSubStatements2(elseStatement);
		}
		if (astNodeType == ASTNode.LABELED_STATEMENT)
		{
			// LabeledStatement:
			// Identifier : Statement
			statementType = NESTED_STATEMENT;

			parseBodyToSubStatements(((LabeledStatement) statementNode).getBody());
		}
		if (astNodeType == ASTNode.RETURN_STATEMENT)
		{
			// return [ Expression ] ;
			statementType = PRIMITIVE_STATEMENT;
			copyRelevantVariables(new ExpressionAST(
					((ReturnStatement) statementNode).getExpression()));
		}
		if (astNodeType == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
		{
			// SuperConstructorInvocation:
			// [ Expression . ] [ < Type { , Type } > ]
			// super ( [ Expression { , Expression } ] ) ;
			statementType = PRIMITIVE_STATEMENT;

			SuperConstructorInvocation stmt = (SuperConstructorInvocation) statementNode;

			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			@SuppressWarnings("unchecked")
			List<Expression> list = stmt.arguments();
			for (int i = 0; i < list.size(); i++)
			{
				ExpressionAST argumentExpressionAST = new ExpressionAST(list.get(i));
				copyRelevantVariables(argumentExpressionAST);
			}
		}
		if (astNodeType == ASTNode.SWITCH_STATEMENT)
		{
			// SwitchStatement:
			// switch ( Expression ) { { SwitchCase | Statement } } }
			// SwitchCase:
			// case Expression :
			// default :
			statementType = SWITCH_STATEMENT;

			SwitchStatement stmt = (SwitchStatement) statementNode;

			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			subStatements = new ArrayList<>();
			parseStatementListToSubStatements(stmt.statements());

		}
		if (astNodeType == ASTNode.SYNCHRONIZED_STATEMENT)
		{
			// synchronized ( Expression ) Block
			statementType = BLOCK_STATEMENT;

			SynchronizedStatement stmt = (SynchronizedStatement) statementNode;

			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));

			parseBodyToSubStatements(stmt.getBody());
		}
		if (astNodeType == ASTNode.THROW_STATEMENT)
		{
			// throw Expression ;
			statementType = PRIMITIVE_STATEMENT;

			copyRelevantVariables(new ExpressionAST(
					((ThrowStatement) statementNode).getExpression()));
		}
		if (astNodeType == ASTNode.TRY_STATEMENT)
		{
			// try Block
			// { CatchClause }
			// [ finally Block ]
			statementType = TRY_STATEMENT;

			TryStatement stmt = (TryStatement) statementNode;

			parseBodyToSubStatements(stmt.getBody());

			Statement finallyStatement = stmt.getFinally();
			if (finallyStatement != null)
				parseBodyToSubStatements2(stmt.getFinally());

			// catch clause
			stmt.catchClauses();
		}
		if (astNodeType == ASTNode.TYPE_DECLARATION_STATEMENT)
		{
			// TypeDeclaration | EnumDeclaration
			statementType = TYPE_DECLARATION_STATEMENT;
		}
		if (astNodeType == ASTNode.VARIABLE_DECLARATION_STATEMENT)
		{
			// { ExtendedModifier } Type VariableDeclarationFragment
			// { , VariableDeclarationFragment } ;
			statementType = PRIMITIVE_STATEMENT;

			// 变量定义位置，存在疑问
			VariableDeclarationStatement stmt = (VariableDeclarationStatement) statementNode;

			List<VariableDeclarationFragment> list = stmt.fragments();
			for (int j = 0; j < list.size(); j++)
			{
				VariableDeclarationFragment varDeclarationFragment = list.get(j);
				VariableAST variable = new VariableAST(stmt.getType().toString(), varDeclarationFragment.getName().toString());

				relevantVariables.add(variable);
				definedVariables.add(variable);

				copyRelevantVariables(new ExpressionAST(varDeclarationFragment.getInitializer()));
			}

		}
		if (astNodeType == ASTNode.WHILE_STATEMENT)
		{
			// while ( Expression ) Statement
			statementType = NESTED_STATEMENT;

			WhileStatement stmt = (WhileStatement) statementNode;

			copyRelevantVariables(new ExpressionAST(stmt.getExpression()));
			parseBodyToSubStatements(stmt.getBody());
		}
	}

	private void parseStatementListToSubStatements(List<Statement> list)
	{
		if (list != null)
		{
			for (Statement subStatement : list)
			{
				StatementAST subStatementAST = new StatementAST(subStatement);
				subStatements.add(subStatementAST);

				copyRelevantVariables(subStatementAST);
			}
		}
	}

	private void parseStatementListToSubStatements2(List<Statement> list)
	{
		if (list != null)
		{
			for (Statement subStatement : list)
			{
				StatementAST subStatementAST = new StatementAST(subStatement);
				subStatements2.add(subStatementAST);

				copyRelevantVariables(subStatementAST);
			}
		}
	}

	private void parseBodyToSubStatements(Statement body)
	{
		subStatements = new ArrayList<>();

		if (body == null)
			return;

		if (body instanceof Block)
		{
			@SuppressWarnings("unchecked")
			List<Statement> list = ((Block) body).statements();
			parseStatementListToSubStatements(list);
		}
		else
		{
			StatementAST statementAST = new StatementAST(body);
			subStatements.add(statementAST);
			copyRelevantVariables(statementAST);
		}
	}

	private void parseBodyToSubStatements2(Statement body)
	{
		subStatements2 = new ArrayList<>();

		if (body == null)
			return;

		if (body instanceof Block)
		{
			@SuppressWarnings("unchecked")
			List<Statement> list = ((Block) body).statements();
			parseStatementListToSubStatements2(list);
		}
		else
		{
			StatementAST statementAST = new StatementAST(body);
			subStatements2.add(statementAST);
			copyRelevantVariables(statementAST);
		}
	}

	public Statement getStatement()
	{
		return statementNode;
	}

	public void setStatement(Statement statement)
	{
		this.statementNode = statement;
	}

	public String getMarker()
	{
		return marker;
	}

	public void setMarker(String marker)
	{
		this.marker = marker;
	}

	public int getTestType()
	{
		return testType;
	}

	public void setTestType(int type)
	{
		this.testType = type;
	}

	public List<StatementAST> getSubStatements()
	{
		return subStatements;
	}

	public Statement getStatementNode()
	{
		return statementNode;
	}

	public void setStatementNode(Statement statementNode)
	{
		this.statementNode = statementNode;
	}

	public HashSet<VariableAST> getRelevantVariables()
	{
		return relevantVariables;
	}

	private HashSet<VariableAST> getUsedVariables()
	{
		return usedVariables;
	}

	public HashSet<VariableAST> getDefinedVariables()
	{
		return definedVariables;
	}

	public int getStatementType()
	{
		return statementType;
	}

	public boolean equals(Object object)
	{
		if (object == null || !(object instanceof StatementAST))
			return false;

		StatementAST _statementInfo = (StatementAST) object;
		return statementNode.equals(_statementInfo.statementNode);
	}

	public String toString()
	{
		if (statementNode == null)
			return "null_statement";

		return statementNode.toString();
	}

	public List<StatementAST> getSubStatements2()
	{
		return subStatements2;
	}
}
//
// class StatementVisitor extends ASTVisitor
// {
// private StatementAST statementAST;
//
// public StatementVisitor(StatementAST _statementInfo)
// {
// this.statementAST = _statementInfo;
// statementAST.getStatement().accept(this);
// }
//
// public boolean visit(MethodInvocation node)
// {
// MethodInvocationData methodInvocationInfo = new MethodInvocationData();
// methodInvocationInfo.setNode(node);
// ASTAnalyzer.parseMethodInvocations(methodInvocationInfo);
//
// statementAST.getMethodAnalyzer().getMethodInvocations().add(methodInvocationInfo);
//
// return true;
// }
//
// public StatementAST getStatementInfo()
// {
// return statementAST;
// }
//
// public void setStatementInfo(StatementAST statementAST)
// {
// this.statementAST = statementAST;
// }
// }
