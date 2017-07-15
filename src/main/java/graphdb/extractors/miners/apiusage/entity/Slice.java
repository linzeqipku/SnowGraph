package graphdb.extractors.miners.apiusage.entity;

import graphdb.extractors.miners.apiusage.codeslice.NameRelevancy;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Slice extends ASTVisitor {
	private String slice;

	private List<APIMethodData> targetAPIs;
	private HashSet<APIMethodData> allAPIs;

	private List<MethodInvocation> methodInvocations;
	private List<String> invocationSignatures;

	public Slice(String _slice) {
		this.slice = _slice;
		methodInvocations = new ArrayList<>();
		invocationSignatures = new ArrayList<>();

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(slice.toCharArray());
		parser.setResolveBindings(true);

		ASTNode sliceNode = parser.createAST(null);
		sliceNode.accept(this);
	}

	public void execute() {
		// remove the target APIs that are not invoked.
		for (int k = 0; k < targetAPIs.size(); k++) {
			String targetAPIName = targetAPIs.get(k).getName();

			boolean gotTarget = false;
			for (int i = 0; i < methodInvocations.size(); i++) {
				if (methodInvocations.get(i).getName().toString().equals(targetAPIName)) {
					gotTarget = true;
					break;
				}
			}

			if (!gotTarget)
				targetAPIs.remove(k);
		}

		// find out all the invoked APIs when target list is empty
		if (targetAPIs.isEmpty()) {
			for (APIMethodData apiMethodData : allAPIs) {
				for (int i = 0; i < methodInvocations.size(); i++) {
					String name = methodInvocations.get(i).getName().toString();

					boolean isTestMethod = false;
					for (int j = 0; j < NameRelevancy.stopWordsList.length; j++) {
						if (name.startsWith(NameRelevancy.stopWordsList[j])) {
							isTestMethod = true;
							break;
						}
					}

					if (!isTestMethod)
						if (name.equals(apiMethodData.getName())) {
							targetAPIs.add(apiMethodData);
							break;
						}
				}
			}
		}

		// 以下产生InvocationSignature
		for (int i = 0; i < methodInvocations.size(); i++) {
			IMethodBinding methodBinding = methodInvocations.get(i).resolveMethodBinding();
			if (methodBinding != null) {
				APIMethodData invokedMethodData = new APIMethodData();

				invokedMethodData.setName(methodBinding.getName());

				ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
				for (int j = 0; j < parameterTypes.length; j++) {
					invokedMethodData.getParameters().add(parameterTypes[j].getName());
				}

				invocationSignatures.add(invokedMethodData.getSignature());
			} else {
				invocationSignatures.add(methodInvocations.get(i).getName().toString());
			}
		}
	}

	public boolean visit(MethodInvocation methodInvocationNode) {
		methodInvocations.add(methodInvocationNode);
		return true;
	}

	public String toString() {
		return slice;
	}

	public String getSlice() {
		return slice;
	}

	public void setSlice(String slice) {
		this.slice = slice;
	}

	public List<MethodInvocation> getMethodInvocations() {
		return methodInvocations;
	}

	public void setMethodInvocations(List<MethodInvocation> methodInvocations) {
		this.methodInvocations = methodInvocations;
	}

	public List<APIMethodData> getTargetAPIs() {
		return targetAPIs;
	}

	public void setTargetAPIs(List<APIMethodData> targetAPIs) {
		this.targetAPIs = targetAPIs;
	}

	public List<String> getInvocationSignatures() {
		return invocationSignatures;
	}

	public void setInvocationSignatures(List<String> invocationSignatures) {
		this.invocationSignatures = invocationSignatures;
	}

	public HashSet<APIMethodData> getAllAPIs() {
		return allAPIs;
	}

	public void setAllAPIs(HashSet<APIMethodData> allAPIs) {
		this.allAPIs = allAPIs;
	}
}
