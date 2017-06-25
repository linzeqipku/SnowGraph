package graphdb.extractors.linkers.codetosnippet.code.cfg;

import com.google.common.collect.ImmutableSet;
import graphdb.extractors.linkers.codetosnippet.adt.graph.Graph;

/**
 * 控制流图的接口
 * @author huacy
 */
public interface CFG extends Graph {
	/**
	 * 获取控制流图中的所有基本块，包含入口和出口
	 * @return 控制流图所有基本块的集合
	 */
	ImmutableSet<? extends CFGBlock> getBlocks();

	CFGBlock getEntry();

	CFGBlock getExit();

	ImmutableSet<? extends CFGVariable> getVariables();

	default CFGVariable getVariable(String name) {
		return getVariables().stream().filter(x -> x.getName().equals(name)).findAny().orElse(null);
	}
}
