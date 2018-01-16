package exps.codepattern.code.cfg.plaincfg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import exps.codepattern.adt.graph.Node;
import exps.codepattern.code.cfg.CFG;
import exps.codepattern.code.cfg.basiccfg.AbstractBasicCFGBlock;
import exps.codepattern.code.cfg.basiccfg.BasicCFG;
import exps.codepattern.code.cfg.basiccfg.BasicCFGSpecialBlock.Entry;
import exps.codepattern.code.cfg.basiccfg.BasicCFGSpecialBlock.Exit;
import exps.codepattern.code.cfg.basiccfg.CFGVariableImpl;
import exps.codepattern.code.cfg.ddg.DDGBlock;
import exps.codepattern.code.ir.VariableUnit;
import exps.codepattern.code.ir.statement.IRStatement;
import exps.codepattern.utils.SetUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 扁平化控制流图的实现，每条语句作为一个基本块
 * @author huacy
 */
public class PlainCFG implements CFG {
	private int blockNum = 0;
	private PlainCFGBlock entry;
	private PlainCFGBlock exit;
	private Set<PlainCFGBlock> blocks = new HashSet<>();
	private Map<VariableUnit, CFGVariableImpl> variableMap = new HashMap<>();

	private PlainCFG(BasicCFG basicCFG) {
		Map<AbstractBasicCFGBlock, PlainCFGBlock> blockStartMap = new HashMap<>();
		Map<AbstractBasicCFGBlock, PlainCFGBlock> blockEndMap = new HashMap<>();

		basicCFG.getBlocks().forEach(oldBlock -> {
			ImmutableList<IRStatement> statements = oldBlock.getStatements();
			PlainCFGBlock[] newBlocks = new PlainCFGBlock[statements.size()];

			for (int i = 0; i < statements.size(); ++i) {
				IRStatement s = statements.get(i);
				PlainCFGBlock newBlock = new PlainCFGBlock(this, s);
				blocks.add(newBlock);
				newBlocks[i] = newBlock;
			}

			for (int i = 0; i < statements.size() - 1; ++i) newBlocks[i].addNext(newBlocks[i + 1]);

			if (statements.isEmpty()) {
				PlainCFGBlock newBlock = new PlainCFGBlock(this);
				if (oldBlock instanceof Entry) entry = newBlock;
				if (oldBlock instanceof Exit) exit = newBlock;
				blocks.add(newBlock);
				blockStartMap.put(oldBlock, newBlock);
				blockEndMap.put(oldBlock, newBlock);
			} else {
				if (oldBlock instanceof Exit) exit = newBlocks[statements.size() - 1];
				blockStartMap.put(oldBlock, newBlocks[0]);
				blockEndMap.put(oldBlock, newBlocks[statements.size() - 1]);
			}
		});

		basicCFG.getBlocks().forEach(oldEndBlock -> {
			PlainCFGBlock newEndBlock = blockEndMap.get(oldEndBlock);
			oldEndBlock.getNexts().stream().map(blockStartMap::get).forEach(newEndBlock::addNext);
		});

		basicCFG.getVariables().forEach(v -> variableMap.put(v.getVariableUnit(), v));
	}

	private PlainCFG(Set<DDGBlock> ddgBlocks) {
		if (ddgBlocks.isEmpty()) throw new RuntimeException("DDG Block Set is empty!");
		PlainCFG oldCFG = ddgBlocks.iterator().next().getPlainCFG();
		Map<PlainCFGBlock, PlainCFGBlock> blockMap = new HashMap<>();

		oldCFG.getBlocks().forEach(block -> blockMap.put(block, new PlainCFGBlock(this, block.getStatement())));
		blocks.addAll(blockMap.values());

		entry = blockMap.get(oldCFG.getEntry());
		exit = blockMap.get(oldCFG.getExit());

		oldCFG.getBlocks().forEach(oldBlock -> {
			PlainCFGBlock newBlock = blockMap.get(oldBlock);
			oldBlock.getNexts().stream().map(blockMap::get).forEach(newBlock::addNext);
		});

		oldCFG.getVariables().forEach(v -> variableMap.put(v.getVariableUnit(), v));

		Set<PlainCFGBlock> preservedBlocks = ddgBlocks.stream().map(DDGBlock::getPlainCFGBlock).map(blockMap::get).collect(Collectors.toSet());

		Sets.difference(blocks, preservedBlocks).immutableCopy().forEach(this::removeBlock);
	}

	public static PlainCFG createCFG(String methodBody) {
		BasicCFG cfg = (BasicCFG) BasicCFG.createCFG(methodBody, true);
		return new PlainCFG(cfg);
	}

	public static PlainCFG createCFG(BasicCFG basicCFG) {
		if (basicCFG == null) return null;
		return new PlainCFG(basicCFG);
	}

	public static PlainCFG createCFG(Set<DDGBlock> ddgBlocks) {
		return new PlainCFG(ddgBlocks);
	}

	public int getNextID() {
		return blockNum++;
	}

	/**
	 * 从控制流图中删除一个基本块，该基本块非入口或出口
	 * @param block 从控制流图中删除的基本块，如果block为entry或exit，则该函数不会做任何改变
	 */
	public void removeBlock(PlainCFGBlock block) {
		if (block == entry || block == exit) return;
		SetUtils.cartesianProduct(block.getPrevs(), block.getNexts()).forEach(p -> p.getLeft().addNext(p.getRight()));
		block.getNexts().forEach(block::removeNext);
		block.getPrevs().forEach(p -> p.removeNext(block));
		blocks.remove(block);
	}

	@Override
	public ImmutableSet<PlainCFGBlock> getBlocks() {
		return new ImmutableSet.Builder<PlainCFGBlock>().addAll(blocks).add(entry).add(exit).build();
	}

	@Override
	public PlainCFGBlock getExit() {
		return exit;
	}

	@Override
	public PlainCFGBlock getEntry() {
		return entry;
	}

	@Override
	public ImmutableSet<CFGVariableImpl> getVariables() {
		return ImmutableSet.copyOf(variableMap.values());
	}

	@Override
	public ImmutableSet<Node> getNodes() {
		return new ImmutableSet.Builder<Node>().addAll(blocks).add(entry).add(exit).build();
	}
}
