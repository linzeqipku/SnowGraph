package graphdb.extractors.linkers.codetosnippet.code.cfg.plaincfg;

import graphdb.extractors.linkers.codetosnippet.code.cfg.CFGBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRStatement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;

public class PlainCFGBlock implements CFGBlock {
	private int id;
	private PlainCFG cfg;
	private IRStatement statement;
	protected HashSet<PlainCFGBlock> prevs = new HashSet<>();
	private HashSet<PlainCFGBlock> nexts = new HashSet<>();

	public PlainCFGBlock(PlainCFG cfg) {
		this.id = cfg.getNextID();
		this.cfg = cfg;
	}

	public PlainCFGBlock(PlainCFG cfg, IRStatement statement) {
		this.id = cfg.getNextID();
		this.cfg = cfg;
		this.statement = statement;
	}

	public void addNext(PlainCFGBlock next) {
		nexts.add(next);
		next.prevs.add(this);
	}

	public void removeNext(PlainCFGBlock next) {
		nexts.remove(next);
		next.prevs.remove(this);
	}

	public PlainCFG getCFG() {
		return cfg;
	}

	public IRStatement getStatement() {
		return statement;
	}

	@Override
	public ImmutableSet<PlainCFGBlock> getPrevs() {
		return ImmutableSet.copyOf(prevs);
	}

	@Override
	public ImmutableList<IRStatement> getStatements() {
		if (statement == null) return ImmutableList.of();
		return ImmutableList.of(statement);
	}

	@Override
	public ImmutableSet<PlainCFGBlock> getNexts() {
		return ImmutableSet.copyOf(nexts);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("[PlainCFGBlock %d] %s", id, statement);
	}
}
