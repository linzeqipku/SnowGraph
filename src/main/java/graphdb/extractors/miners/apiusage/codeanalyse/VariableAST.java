package graphdb.extractors.miners.apiusage.codeanalyse;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class VariableAST {
	private String type;
	private String name;

	private VariableDeclarationStatement variableDeclarationNode;

	public VariableAST(String name) {
		this.name = name;
	}

	public VariableAST(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public VariableAST(String type, String name, VariableDeclarationStatement variableDeclarationNode) {
		this.type = type;
		this.name = name;
		this.variableDeclarationNode = variableDeclarationNode;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean equals(Object object) {
		if (object == null || !(object instanceof VariableAST))
			return false;

		VariableAST obj = (VariableAST) object;
		if (this.name == null)
			return obj.name == null;

		return this.name.equals(obj.name);
	}

	public String toString() {
		if (variableDeclarationNode == null)
			return name;
		else
			return variableDeclarationNode.toString();
	}
}
