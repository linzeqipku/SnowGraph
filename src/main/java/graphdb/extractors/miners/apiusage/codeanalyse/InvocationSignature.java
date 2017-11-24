package graphdb.extractors.miners.apiusage.codeanalyse;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InvocationSignature extends ASTVisitor {
	private static final String DECLARE_LABEL = "<DECLARE>";
	private static final String DEFINE_LABEL = "<DEFINE>";
	private static final String NONE_LABEL = "<NONE>";
	public static final String TEST_LABEL = "<TEST>";
	public static final String TARGET_LABEL = "<TARGET>";
	private static final String[] TEST_PREFIXES = {"assert", "check", "confirm", "fail", "test",
		"verify"};
	private ASTNode node;
	private StringBuilder signature;
	private ArrayList<String> lines;
	// key:Lines集合中的下标编号;value:在当前方法中所处的Statement的序号
	private HashMap<Integer, Integer> lineNumberIndex;

	public InvocationSignature(ASTNode node) {
		super();
		this.node = node;

		parseSignature();
	}

	public static boolean isTestLine(String str) {
		if (str.startsWith(TEST_LABEL))
			return true;

		for (String prefix : TEST_PREFIXES)
			if (str.startsWith(prefix))
				return true;

		return false;
	}

	@SuppressWarnings("unchecked")
	private void parseSignature() {
		if (node == null)
			return;

		signature = new StringBuilder();
		lines = new ArrayList<>();
		lineNumberIndex = new HashMap<>();

		if (node instanceof MethodDeclaration) {
			// 方法体进入
			if (((MethodDeclaration) node).getBody() == null)
				return;
			List<Statement> stmts = ((MethodDeclaration) node).getBody().statements();
			for (int i = 0; i < stmts.size(); i++) {
				InvocationSignature stmtSignature = new InvocationSignature(stmts.get(i));
				if (!stmtSignature.isEmpty())
					signature.append(stmtSignature);

				if (!stmtSignature.isEmptyLines()) {
					// lines.addAll(stmtSignature.getLines());
					for (String signatureLine : stmtSignature.getLines()) {
						lines.add(signatureLine);
						// 仅在stmt一级添加索引
						lineNumberIndex.put(lines.size() - 1, i);
					}
				}
			}
		} else if (node instanceof Statement) {
			// 语句进入
			StatementAST statementAST = new StatementAST((Statement) node);

			if (statementAST.getSubStatements() != null) {
				// 包含嵌套block的语句体
				for (StatementAST stmt : statementAST.getSubStatements()) {
					// 最终还是要落到对每个stmt的解析，所以还是要进入后面的简单Stmt分支
					InvocationSignature stmtSignature = new InvocationSignature(stmt.getStatement());
					if (!stmtSignature.isEmpty())
						signature.append(stmtSignature);
					if (!stmtSignature.isEmptyLines())
						lines.addAll(stmtSignature.getLines());
				}

				if (statementAST.getSubStatements2() != null) {
					for (StatementAST stmt : statementAST.getSubStatements2()) {
						InvocationSignature stmtSignature = new InvocationSignature(
							stmt.getStatement());
						if (!stmtSignature.isEmpty())
							signature.append(stmtSignature);
						if (!stmtSignature.isEmptyLines())
							lines.addAll(stmtSignature.getLines());
					}
				}
			} else {
				// 简单Statement
				// if (statementAST.getStatementType() ==
				// StatementAST.PRIMITIVE_STATEMENT)
				// 每一个Stmt最终都要归入到这个分支，accept

				Statement primitiveStmt = (Statement) node;

				primitiveStmt.accept(this);
				if (this.isEmpty()) {
					StringBuilder str = new StringBuilder();

					// 解析当前stmt的结果为空，不含方法调用
					// 只有在不含方法调用时候，才解析变量声明和定义的情况
					if (primitiveStmt instanceof VariableDeclarationStatement) {
						// 如果是变量定义，比如Byte[] b;
						StatementAST stmtAst = new StatementAST(primitiveStmt);

						str.append(DECLARE_LABEL + "[");

						boolean needComma = false;
						for (VariableAST var : stmtAst.getDefinedVariables()) {
							if (needComma)
								str.append(", ");

							str.append(
								((VariableDeclarationStatement) primitiveStmt).getType()
									.toString()).append(" ").append(var.getName());
						}
						str.append("]");
					} else if (primitiveStmt instanceof ExpressionStatement) {
						Expression expression = ((ExpressionStatement) primitiveStmt)
							.getExpression();
						if (expression instanceof Assignment) {
							Assignment assignment = (Assignment) expression;
							if (assignment.resolveTypeBinding() != null) str.append(DEFINE_LABEL).append("[")
								.append(assignment.resolveTypeBinding().getName()).append(" ")
								.append(assignment.getLeftHandSide()).append("]");
							else str.append(DEFINE_LABEL).append("[").append(assignment.getLeftHandSide()).append("]");
						} else if (expression instanceof PostfixExpression) {
							PostfixExpression postfixExpression = (PostfixExpression) expression;
							if (postfixExpression.resolveTypeBinding() != null) str.append(DEFINE_LABEL).append("[")
								.append(postfixExpression.resolveTypeBinding().getName())
								.append(" ").append(postfixExpression.getOperand()).append("]");
							else str.append(DEFINE_LABEL).append("[").append(postfixExpression.getOperand()).append("]");
						} else if (expression instanceof PrefixExpression) {
							PrefixExpression prefixExpression = (PrefixExpression) expression;
							if (prefixExpression.getOperator() == PrefixExpression.Operator.INCREMENT
								|| prefixExpression.getOperator() == PrefixExpression.Operator.DECREMENT) {
								if (prefixExpression.resolveTypeBinding() != null) str.append(DEFINE_LABEL).append("[")
									.append(prefixExpression.resolveTypeBinding().getName())
									.append(" ").append(prefixExpression.getOperand())
									.append("]");
								else str.append(DEFINE_LABEL).append("[").append(prefixExpression.getOperand()).append("]");
							}
						}
					}

					if (str.length() > 0) {
						signature.append(str);
						lines.add(str + "\n");
					}

				}
				if (this.isEmpty())
					signature.append(NONE_LABEL);
				signature.append("\n");
			}
		} else if (node instanceof MethodInvocation) {
			// 不是方法体或语句，方法调用的表达式段落进入
			// 从accept进入的
			// a.b(c());
			MethodInvocation methodInvocation = (MethodInvocation) node;

			// 发起调用的对象部分的表达式
			InvocationSignature hostSignature = new InvocationSignature(
				methodInvocation.getExpression());
			if (!hostSignature.isEmpty())
				signature.insert(0, hostSignature + ", ");
			if (!hostSignature.isEmptyLines())
				lines.addAll(hostSignature.getLines());

			// 调用方法的名称，唯一可能append实际identifier的地方，其他地方仅copy和连接
			signature.append(methodInvocation.getName().toString());

			// 方法调用的诸参数表达式的分析
			List<InvocationSignature> argumentsSignature = new ArrayList<>();

			for (Expression argument : (List<Expression>) methodInvocation.arguments()) {
				InvocationSignature arguSignature = new InvocationSignature(argument);
				if (!arguSignature.isEmpty())
					argumentsSignature.add(arguSignature);
			}

			if (!argumentsSignature.isEmpty()) {
				signature.append("(").append(argumentsSignature.get(0));

				lines.addAll(argumentsSignature.get(0).getLines());

				if (argumentsSignature.size() > 1) {
					for (int i = 1; i < argumentsSignature.size(); i++) {
						if (!argumentsSignature.get(i).isEmpty())
							signature.append(", ").append(argumentsSignature.get(i));

						if (!argumentsSignature.get(i).isEmptyLines())
							lines.addAll(argumentsSignature.get(i).getLines());
					}
				}

				signature.append(")");
			}

			// 按照调用完成顺序添加lines,host调用在最初,argu调用次之,本方法调用在最后
			lines.add(methodInvocation.getName().toString() + "\n");

		} else {
			// 不是方法体，也不是语句，也不是方法调用的结点
			// 看看其中还有无别的方法调用
			node.accept(this);
		}
	}

	public boolean visit(MethodInvocation methodInvocation) {
		// 只有在解析单个stmt或者expression进入此处，进入之后要新建sign，回到前文
		// 其他节点，如果遇到方法调用，则解析方法调用节点，其余不解析
		// 一个其他类型节点，顺序出现了n个方法调用，逐个分析并逗号分隔加入签名。
		InvocationSignature invocationSignature = new InvocationSignature(methodInvocation);
		char lastSymbol = signature.length() == 0 ? 0 : signature.charAt(signature.length() - 1);
		if (Character.isLetter(lastSymbol))
			signature.append(", ");

		// 将遇到的方法调用顺序添加到签名中
		signature.append(invocationSignature);
		lines.addAll(invocationSignature.getLines());

		// 用methodInvocation三个part的解析来控制，不深入
		return false;
	}

	private String markWords(String originalString, String[] words, String markLabel) {
		String tempString = originalString;
		for (String wordToMark : words) {
			if (tempString.startsWith(wordToMark))
				tempString = tempString.replace(wordToMark, markLabel + wordToMark);
			tempString = tempString.replace("\n" + wordToMark, "\n" + markLabel + wordToMark);
			tempString = tempString.replace("(" + wordToMark, "(" + markLabel + wordToMark);
			tempString = tempString.replace(" " + wordToMark, " " + markLabel + wordToMark);
		}
		return tempString;
	}

	public String getTestSignatureByStatements() {
		if (!(node instanceof MethodDeclaration))
			return null;
		MethodAST methodAST = new MethodAST((MethodDeclaration) node);

		StringBuilder signatureByStmts = new StringBuilder();

		for (int i = 0; i < methodAST.getStatements().size(); i++) {
			StatementAST stmt = methodAST.getStatements().get(i);
			String stmtsign = "";
			if (stmt.hasBlock()) {
				String sign = new InvocationSignature(stmt.getStatement()).getTestSignature();
				if (sign.contains(InvocationSignature.TEST_LABEL))
					stmtsign = InvocationSignature.TEST_LABEL;

				stmtsign += sign.replace('\n', ';') + "\n"; // 记得补上句末的换行
			} else {
				stmtsign = new InvocationSignature(stmt.getStatement()).getTestSignature();
			}
			signatureByStmts.append(stmtsign);
		}
		return signatureByStmts.toString();
	}

	// 把原始的signature转换为test标签标注的
	public String getTestSignature() {
		return markWords(signature.toString(), TEST_PREFIXES, TEST_LABEL);
	}

	// 把原始的signature打上test标记，再对targetAPI也打上标记
    private String getTargetTestSignature(List<String> targets) {
		return markWords(getTestSignature(), targets.toArray(new String[0]), TARGET_LABEL);
	}

	// 把原始的signatureLines转换为test标签标注的
	public ArrayList<String> getTestSignatureLines() {
		ArrayList<String> testLines = new ArrayList<>();
		for (String string : lines) {
			testLines.add(markWords(string, TEST_PREFIXES, TEST_LABEL));
		}
		return testLines;
	}

	// 把原始的signatureLines打上test标记，再对targetAPI也打上标记
	public ArrayList<String> getTargetTestSignatureLines(List<String> targets) {
		ArrayList<String> targetTestLines = new ArrayList<>();
		for (String string : getTestSignatureLines()) {
			targetTestLines.add(markWords(string, targets.toArray(new String[0]), TARGET_LABEL));
		}
		return targetTestLines;
	}

	// 把原始的signatureLines打上test标记，再对targetAPI也打上标记
	public ArrayList<String> getTargetTestCycleSignatureLines(List<String> targets) {
		String sign = getTargetTestSignature(targets);
		ArrayList<String> targetTestLines = new ArrayList<>();
		for (String string : sign.split("\n")) {
			// 如果包含target调用但不在一行sign的最前面，则把所有target找出来放到最前面
			if (!string.startsWith(TARGET_LABEL)) {
				String tempString = string;
				while (tempString.contains(TARGET_LABEL)) {
					String remainder = tempString.substring(tempString.indexOf(TARGET_LABEL));
					String targetWord = remainder.split(",|\\)|\\(|\n")[0];

					targetTestLines.add(targetWord + "\n");

					tempString = remainder.substring(targetWord.length());
				}
			}
			// 不论如何，原string还是直接加在列表中。
			targetTestLines.add(string + "\n");
		}
		return targetTestLines;
	}

	private boolean isEmpty() {
		return signature == null || signature.toString().equals("");
	}

	private boolean isEmptyLines() {
		return lines == null || lines.size() <= 0;
	}

	public StringBuilder getSignature() {
		return signature;
	}

	public ASTNode getNode() {
		return node;
	}

	public void setNode(ASTNode node) {
		this.node = node;
		parseSignature();
	}

	public String toString() {
		return signature.toString();
	}

	private ArrayList<String> getLines() {
		return lines;
	}

	public HashMap<Integer, Integer> getLineNumberIndex() {
		return lineNumberIndex;
	}

}

