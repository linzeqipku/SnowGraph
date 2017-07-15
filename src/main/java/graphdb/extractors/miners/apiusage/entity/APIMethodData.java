package graphdb.extractors.miners.apiusage.entity;

import graphdb.extractors.miners.apiusage.codeslice.Clusters;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class APIMethodData extends MethodData implements Serializable {
	private static final long serialVersionUID = -5442545056752410785L;
	private final int exampleNumPerCluster = 2;

	private List<String> parameters;

	private List<Slice> rawExamples = new ArrayList<>();
	private Clusters clusters;
	private List<String> apiExamples = new ArrayList<>();

	private String key = null;

	public void initByIMethodBinding(IMethodBinding mBinding) {
		IMethod iMethod = (IMethod) mBinding.getJavaElement();
		try {
			key = iMethod.getKey().substring(0, iMethod.getKey().indexOf("("))
					+ iMethod.getSignature();
			projectName = mBinding.getJavaElement().getJavaProject()
					.getElementName();
		} catch (Exception e) {
			projectName = "";
		}
		packageName = mBinding.getDeclaringClass().getPackage().getName();
		className = mBinding.getDeclaringClass().getName();
		name = mBinding.getName();

		parameters = new ArrayList<>();
		ITypeBinding[] parameterBindings = mBinding.getParameterTypes();
		for (int i = 0; i < parameterBindings.length; i++) {
			parameters.add(parameterBindings[i].getName());
		}
	}

	public String getKey() {
		return key;
	}

	public String getSignature() {
		try {
			String signature = packageName + "." + className + "." + name + "(";
			if (parameters != null) {
				for (int i = 0; i < parameters.size(); i++) {
					if (i > 0)
						signature += ",";
					signature += parameters.get(i);
				}
			}
			signature += ")";
			return signature;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return "";
		}
	}

	public void clustering() {
		clusters = new Clusters(rawExamples);

		// pick 2(exampleNumPerCluster) example(s) from each cluster.
		for (int i = 0; i < clusters.getClusters().size(); i++) {
			for (int j = 0; j < exampleNumPerCluster
					&& j < clusters.getClusters().get(i).size(); j++) {
				apiExamples
						.add(clusters.getClusters().get(i).get(j).getSlice());
			}
		}
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof APIMethodData))
			return false;

		APIMethodData apiMethodData = (APIMethodData) obj;

		if ("".equals(this.getSignature())
				|| "".equals(apiMethodData.getSignature()))
			return false;

		return this.getSignature().equals(apiMethodData.getSignature());
	}

	public int hashCode() {
		return ("usetec.objects.APIMethodData@" + getSignature()).hashCode();
	}

	public String toString() {
		return getSignature();
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public Clusters getClusters() {
		return clusters;
	}

	public List<Slice> getRawExamples() {
		return rawExamples;
	}

	public void setRawExamples(List<Slice> rawExamples) {
		this.rawExamples = rawExamples;
	}

	public List<String> getApiExamples() {
		return apiExamples;
	}

}
