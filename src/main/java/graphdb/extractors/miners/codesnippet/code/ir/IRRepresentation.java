package graphdb.extractors.miners.codesnippet.code.ir;

import graphdb.extractors.miners.codesnippet.code.cfg.basiccfg.BasicCFGRegularBlock;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression.IRExtern;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression.IRTemp;
import graphdb.extractors.miners.codesnippet.code.ir.IRExpression.IRVariable;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRAbstractStatement;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRLabel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.*;

public class IRRepresentation implements IRScope {
	private static ASTParser parser = ASTParser.newParser(AST.JLS8);

	private int tempNum = 0, labelNum = 0;
	private List<IRAbstractStatement> statements = new ArrayList<>();
	private Map<String, VariableUnit> variables = new HashMap<>();

	public IRRepresentation(String methodBody) {
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setSource(methodBody.toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		final ASTNode cu = parser.createAST(null);
		cu.accept(new IRBuiltVisitor(this));
	}

	@Override
	public void addStatement(IRAbstractStatement statement) {
		statements.add(statement);
	}

	public IRExpression getVariableOrExtern(String name) {
		VariableUnit v = variables.get(name);
		if (v != null) return new IRVariable(v);
		return new IRExtern(name);
	}

	public IRVariable getVariableOrCreate(String name) {
		VariableUnit v;
		v = variables.get(name);
		if (v == null) {
			v = new VariableUnit(name);
			variables.put(name, v);
		}
		return new IRVariable(v);
	}

	public Collection<VariableUnit> getVariables() {
		return variables.values();
	}

	public IRTemp createTempVariable() {
		IRTemp result = new IRTemp(tempNum++);
		variables.put(result.toString(), result.getVariable());
		return result;
	}

	public IRLabel createLabel() {
		return new IRLabel(labelNum++);
	}

	public void output() {
		statements.forEach(System.out::println);

		variables.forEach((k, v) -> System.out.println(v));
	}

	public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
		for (IRAbstractStatement statement : statements) {
			block = statement.buildCFG(block);
		}
		return block;
	}

}
