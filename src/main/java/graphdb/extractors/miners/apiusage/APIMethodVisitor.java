package graphdb.extractors.miners.apiusage;

import com.google.common.collect.ImmutableSet;
import graphdb.extractors.miners.apiusage.codeanalyse.MethodAST;
import graphdb.extractors.miners.apiusage.codeslice.Slicer;
import graphdb.extractors.miners.apiusage.entity.Slice;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.HashSet;
import java.util.Set;

class APIMethodVisitor extends ASTVisitor {
	private Set<Slice> slices = new HashSet<>();

	@Override
	public boolean visit(MethodDeclaration node) {
		MethodAST methodAST = new MethodAST(node);

		Slicer slicer = new Slicer(methodAST);
		slicer.slice();
		slices.addAll(slicer.getSlices());
		return true;
	}

	public ImmutableSet<Slice> getSlices() {
		return ImmutableSet.copyOf(slices);
	}
}
