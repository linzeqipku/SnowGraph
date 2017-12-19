package searcher.codepattern.code.ir;

import searcher.codepattern.code.mining.MiningNode;

public interface IRExpression extends VisitorResult {
	IRBoolean TRUE = new IRBoolean(true);
	IRBoolean FALSE = new IRBoolean(false);
	IRKeyword SUPER = new IRKeyword("super");
	IRKeyword THIS = new IRKeyword("this");
	IRKeyword NULL = new IRKeyword("null");

	MiningNode toMiningNode();

	abstract class IRAbstractVariable implements IRExpression {
		private VariableUnit variable;
		private int version = -1;

		public IRAbstractVariable(VariableUnit variable) {
			this.variable = variable;
		}

		public VariableUnit getVariable() {
			return variable;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public int getVersion() {
			return version;
		}

		@Override
		public String toString() {
			if (version == -1 || variable.getDefBoxes().count() == 1) return variable.getName();
			return variable.getName() + "@" + version;
		}

		@Override
		public abstract IRAbstractVariable clone();

		@Override
		public MiningNode toMiningNode() {
			throw new UnsupportedOperationException("IRAbstractVariable can not convert to MiningNode");
		}
	}

	class IRVariable extends IRAbstractVariable {
		public IRVariable(VariableUnit variable) {
			super(variable);
		}

		@Override
		public IRVariable clone() {
			return new IRVariable(getVariable());
		}
	}

	class IRTemp extends IRAbstractVariable {
		private int number;

		public IRTemp(int number) {
			super(new VariableUnit("#" + number));
			this.number = number;
		}

		@Override
		public IRTemp clone() {
			return new IRTemp(number);
		}
	}

	class IRExtern implements IRExpression {
		private String name;

		public IRExtern(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public MiningNode toMiningNode() {
			return new MiningNode(MiningNode.OPType.EXTERN, name);
		}
	}

	class IRString implements IRExpression {
		private String string;

		public IRString(String string) {
			this.string = string;
		}

		@Override
		public String toString() {
			return "\"" + string + "\"";
		}

		@Override
		public MiningNode toMiningNode() {
			return new MiningNode(MiningNode.OPType.STRING, string);
		}
	}

	class IRChar implements IRExpression {
		private char value;

		public IRChar(char value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "'" + value + "'";
		}

		@Override
		public MiningNode toMiningNode() {
			return new MiningNode(MiningNode.OPType.CHAR, "" + value);
		}
	}

	class IRNumber implements IRExpression {
		private String value;

		public IRNumber(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public MiningNode toMiningNode() {
			return new MiningNode(MiningNode.OPType.NUMBER, value);
		}
	}

	class IRBoolean implements IRExpression {
		private boolean value;
		private static MiningNode TRUE_MINING_NODE = new MiningNode(MiningNode.OPType.BOOLEAN, "true");
		private static MiningNode FALSE_MINING_NODE = new MiningNode(MiningNode.OPType.BOOLEAN, "false");

		private IRBoolean(boolean value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "" + value;
		}

		@Override
		public MiningNode toMiningNode() {
			return value ? TRUE_MINING_NODE : FALSE_MINING_NODE;
		}
	}

	class IRType implements IRExpression {
		private String type;

		public IRType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return String.format("%s.class", type);
		}

		@Override
		public MiningNode toMiningNode() {
			return new MiningNode(MiningNode.OPType.TYPE, type);
		}
	}

	class IRKeyword implements IRExpression {
		private String name;
		private static MiningNode SUPER_MINING_NODE = new MiningNode(MiningNode.OPType.KEYWORD, "super");
		private static MiningNode THIS_MINING_NODE = new MiningNode(MiningNode.OPType.KEYWORD, "this");
		private static MiningNode NULL_MINING_NODE = new MiningNode(MiningNode.OPType.KEYWORD, "null");

		private IRKeyword(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public MiningNode toMiningNode() {
			if (this == SUPER) return SUPER_MINING_NODE;
			if (this == THIS) return THIS_MINING_NODE;
			if (this == NULL) return NULL_MINING_NODE;
			throw new RuntimeException("Unknown keyword!");
		}
	}
}
