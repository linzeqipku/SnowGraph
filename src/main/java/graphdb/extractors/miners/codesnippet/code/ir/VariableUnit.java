package graphdb.extractors.miners.codesnippet.code.ir;

import com.google.common.base.Joiner;
import graphdb.extractors.miners.codesnippet.code.ir.statement.IRStatement;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class VariableUnit {
	private String name;
	private Set<IRStatement> defBoxes = new HashSet<>();
	private Set<IRStatement> useBoxes = new HashSet<>();

	public VariableUnit(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addDef(IRStatement statement) {
		defBoxes.add(statement);
	}

	public void removeDef(IRStatement statement) {
		defBoxes.remove(statement);
	}

	public void addUse(IRStatement statement) {
		useBoxes.add(statement);
	}

	public void removeUse(IRStatement statement) {
		useBoxes.remove(statement);
	}

	public Stream<IRStatement> getDefBoxes() {
		return defBoxes.stream();
	}

	public Stream<IRStatement> getUseBoxes() {
		return useBoxes.stream();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VariableUnit)) return false;
		VariableUnit other = (VariableUnit) obj;
		return name.equals(other.name);
	}

	@Override
	public String toString() {
		return String.format("[Variable]%s def in [%s] and use in [%s]",
			name,
			Joiner.on(", ").join(defBoxes),
			Joiner.on(", ").join(useBoxes)
		);
	}
}
