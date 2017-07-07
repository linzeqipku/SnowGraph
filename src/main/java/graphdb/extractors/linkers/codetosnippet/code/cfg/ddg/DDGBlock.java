package graphdb.extractors.linkers.codetosnippet.code.cfg.ddg;

import graphdb.extractors.linkers.codetosnippet.code.cfg.CFGBlock;
import graphdb.extractors.linkers.codetosnippet.code.cfg.plaincfg.PlainCFG;
import graphdb.extractors.linkers.codetosnippet.code.cfg.plaincfg.PlainCFGBlock;
import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRStatement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;

public class DDGBlock implements CFGBlock {
	private int id;
	private PlainCFGBlock plainCFGBlock;
	protected HashSet<DDGBlock> prevs = new HashSet<>();
	private HashSet<DDGBlock> nexts = new HashSet<>();

	public DDGBlock(DDG ddg, PlainCFGBlock plainCFGBlock) {
		this.id = ddg.getNextID();
		this.plainCFGBlock = plainCFGBlock;
	}

	public PlainCFG getPlainCFG() {
		return plainCFGBlock.getCFG();
	}

	public PlainCFGBlock getPlainCFGBlock() {
		return plainCFGBlock;
	}

	public void addNext(DDGBlock next) {
		nexts.add(next);
		next.prevs.add(this);
	}

	public int getID() {
		return id;
	}

	public IRStatement getStatement() {
		return plainCFGBlock.getStatement();
	}

	@Override
	public ImmutableSet<DDGBlock> getPrevs() {
		return ImmutableSet.copyOf(prevs);
	}

	@Override
	public ImmutableList<IRStatement> getStatements() {
		return plainCFGBlock.getStatements();
	}

	@Override
	public ImmutableSet<DDGBlock> getNexts() {
		return ImmutableSet.copyOf(nexts);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("[DDGBlock %d] %s", id, getStatement());
	}
}
