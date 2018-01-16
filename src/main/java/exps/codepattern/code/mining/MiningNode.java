package exps.codepattern.code.mining;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningNode {
	public static final MiningNode EMPTY = new MiningNode(OPType.OP, "EMPTY");
	public static final MiningNode ARRAY_CREATION = new MiningNode(OPType.OP, "ARRAY_CREATION");
	public static final MiningNode ASSERT = new MiningNode(OPType.OP, "ASSERT");
	public static final MiningNode ARRAY_ACCESS = new MiningNode(OPType.OP, "ARRAY_ACCESS");
	public static final MiningNode PHI = new MiningNode(OPType.OP, "PHI");
	public static final MiningNode ASSIGNMENT = new MiningNode(OPType.OP, "ASSIGNMENT");
	public static final MiningNode THROW = new MiningNode(OPType.OP, "THROW");
	public static final MiningNode RETURN = new MiningNode(OPType.OP, "RETURN");
	public static final MiningNode PRE_PLUS = new MiningNode(OPType.OP, "PRE_PLUS");
	public static final MiningNode PRE_MINUS = new MiningNode(OPType.OP, "PRE_MINUS");
	public static final MiningNode PRE_COMPLEMENT = new MiningNode(OPType.OP, "PRE_COMPLEMENT");
	public static final MiningNode PRE_NOT = new MiningNode(OPType.OP, "PRE_NOT");
	private OPType op;
	private String info;

	public MiningNode(OPType op, String info) {
		this.op = op;
		this.info = info;
	}

	public MiningNode(String s) {
		Pattern p = Pattern.compile("#(.*)(?:\\$(.*))?");
		Matcher m = p.matcher(s);
		String opGroup = m.group(1);
		String infoGroup = m.group(2);
		op = OPType.valueOf(opGroup);
		info = infoGroup;
	}

	@Override
	public String toString() {
		return op + (info == null ? "" : "$" + info);
	}

	@Override
	public int hashCode() {
		return op.hashCode() ^ (info == null ? 0 : info.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MiningNode)) return false;
		MiningNode other = ((MiningNode) obj);
		if (!op.equals(other.op)) return false;
		if (info == null) return other.info == null;
		return info.equals(other.info);
	}

	public enum OPType {
		OP("#OP"),
		METHOD_INVOCATION("#METHOD_INVOCATION"),
		REFERENCE("#REFERENCE"),
		BINARY("#BINARY"),
		FIELD_ACCESS("#FIELD_ACCESS"),
		STRING("#STRING"),
		CHAR("#CHAR"),
		NUMBER("#NUMBER"),
		BOOLEAN("#BOOLEAN"),
		TYPE("#TYPE"),
		KEYWORD("#KEYWORD"),
		EXTERN("#EXTERN");

		private String op;

		OPType(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return op;
		}
	}
}
