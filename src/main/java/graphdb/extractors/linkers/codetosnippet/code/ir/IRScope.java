package graphdb.extractors.linkers.codetosnippet.code.ir;

import graphdb.extractors.linkers.codetosnippet.code.ir.statement.IRAbstractStatement;

public interface IRScope {
	void addStatement(IRAbstractStatement statement);
}
