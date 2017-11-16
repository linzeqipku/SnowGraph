package graphdb.extractors.miners.apiusage.codeslice;

import graphdb.extractors.miners.apiusage.codeanalyse.InvocationSignature;
import graphdb.extractors.miners.apiusage.codeanalyse.MethodAST;
import graphdb.extractors.miners.apiusage.codeanalyse.StatementAST;
import graphdb.extractors.miners.apiusage.codeanalyse.VariableAST;
import graphdb.extractors.miners.apiusage.entity.APIMethodData;
import graphdb.extractors.miners.apiusage.entity.Slice;
import utils.parse.CollectionUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.*;

public class Slicer {
	private final int WEAK_CONSECUTIVE_ASSETION_LENGTH_THRESHOLD = 6;
	private final double WEAK_CONSECUTIVE_ASSETION_PERCENTAGE_THRESHOLD = 0.57;

	private String sourceFile;
	private MethodAST methodAST;
	private List<String> targetMethods;
	private List<APIMethodData> targetAPIs;
	private HashSet<APIMethodData> allAPIs;
	private InvocationSignature signature;
	private List<String> sliceSnippets;
	private List<Slice> slices;

	public Slicer(MethodAST methodAST) {
		this.methodAST = methodAST;
	}

	public void slice() {
		sliceSnippets = new ArrayList<>();

		findTestTarget();

		interpretTargetNamesToAPIs();
		// 发现某些testtarget竟然在slice中不出现！必须给出一个allAPIs供查询
		genereateAllAPIs();

		signature = new InvocationSignature(methodAST.getMethodDeclarationNode());

		if (isCodePattern_ConsecutiveAssertion()) {
			sliceSnippets.addAll(slice_ConsecutiveAssertion());
		} else if (isCodePattern_TargetAssertionCycle()) {
			sliceSnippets.addAll(sliceByTarget(targetMethods));
		} else if (isCodePattern_WeakConsecutiveAssetionsWithBlock()) {
			sliceSnippets.addAll(slice_WeakConsecutiveAssertionsWithBlock());
		} else {
			List<String> lcs = findCommonLCSOfSignature();
			if (lcs == null || lcs.isEmpty())
				lcs = getLCSOfSignature();

			boolean isValid = false;
			for (int i = 0; i < lcs.size(); i++) {
				String str = lcs.get(i);

				if (!str.equals(InvocationSignature.TEST_LABEL)) isValid = true;
				else lcs.remove(i);
			}

			if (isValid) sliceSnippets.addAll(sliceByTarget(lcs));
			else sliceSnippets.addAll(slice_AssertionSeparatedParagraph());

		}

		slices = new ArrayList<>();
		for (String sliceSnippet : sliceSnippets) {
			Slice slice = new Slice(sliceSnippet);
			slice.setTargetAPIs(targetAPIs);
			slice.setAllAPIs(allAPIs);
			slice.execute();
			slices.add(slice);
		}
	}

	// 总结对方法调用的处理：
	// 每一次方法调用是一个不同的MethodInvocation
	// 如果调用的是相同的方法，会解析成相同的MethodBinding
	// 如果名称相同，不论签名如何，都是相同的String methodName
	private void findTestTarget() {
		targetMethods = new ArrayList<String>();

		String testMethodName = methodAST.getMethodDeclarationNode().getName().toString();

		Map<String, List<MethodInvocation>> invokedMethodDirectory = new HashMap<>();
		List<Relevancy> apiRelevancies = new ArrayList<>();

		// Array of MethodInvocation would be null if the testMethod contains no
		// method invocation...
		if (methodAST.getMethodInvocations() != null
			&& methodAST.getMethodInvocations().size() != 0) {
			for (MethodInvocation methodInvocation : methodAST.getMethodInvocations()) {
				// 一个测试方法内会否调用多个同名方法的不同重载？
				// IMethodBinding methodBinding =
				// methodInvocation.resolveMethodBinding();

				String methodInvoctaionName = methodInvocation.getName().toString();
				if (!invokedMethodDirectory.containsKey(methodInvoctaionName)) {
					invokedMethodDirectory.put(methodInvoctaionName,
						new ArrayList<MethodInvocation>());
				}

				invokedMethodDirectory.get(methodInvoctaionName).add(methodInvocation);

			}

			for (String apiName : invokedMethodDirectory.keySet()) {
				if (apiName.contains("assert"))
					continue;

				NameRelevancy nameRelevancy = new NameRelevancy();
				nameRelevancy.setTestMethodName(testMethodName);
				nameRelevancy.setApiMethodName(apiName.toString());
				nameRelevancy.execute();

				int callCount = invokedMethodDirectory.get(apiName).size();

				Relevancy relevancy = new Relevancy();
				relevancy.setInvokedMethodKey(apiName);
				relevancy.setTestMethodKey(testMethodName);
				relevancy.setNameRelevancy(nameRelevancy.getCommonWordsRatio());

				relevancy.setCallCount(callCount);

				apiRelevancies.add(relevancy);
			}
		}

		// 相关度从小到大排序
		Collections.sort(apiRelevancies);

		int lastIndex = apiRelevancies.size() - 1;
		if (lastIndex >= 0 && apiRelevancies.get(lastIndex).getNameRelevancy() > 0) {
			int i = lastIndex;
			while (i >= 0) {
				// 相关度最大的几个方法被输出到targetMethods的列表中
				targetMethods.add(apiRelevancies.get(i).getInvokedMethodKey());

				// 不存在并列则跳出
				if (i > 0 && !apiRelevancies.get(i).hasSameRelevancyWith(apiRelevancies.get(i - 1)))
					break;

				i--;
			}
		}
	}

	private void interpretTargetNamesToAPIs() {
		targetAPIs = new ArrayList<>();
		List<IMethodBinding> targetAPIBindings = new ArrayList<>();
		List<MethodInvocation> invocations = methodAST.getMethodInvocations();

		for (int i = 0; i < targetMethods.size(); i++) {
			String targetName = targetMethods.get(i);

			HashMap<IMethodBinding, Integer> methodCountMap = new HashMap<>();
			for (int j = 0; j < invocations.size(); j++) {
				IMethodBinding mBinding = invocations.get(j).resolveMethodBinding();
				if (mBinding != null) {
					if (mBinding.getName().equals(targetName))
						methodCountMap.put(mBinding, methodCountMap.get(mBinding) == null ? 1
							: methodCountMap.get(mBinding) + 1);
				}
			}

			int maxInvoked = 0;
			for (Integer count : methodCountMap.values()) {
				if (count > maxInvoked)
					maxInvoked = count;
			}

			for (IMethodBinding methodBinding : methodCountMap.keySet()) {
				if (methodCountMap.get(methodBinding) == maxInvoked)
					targetAPIBindings.add(methodBinding);
			}
		}

		for (int i = 0; i < targetAPIBindings.size(); i++) {
			APIMethodData apiMethodData = new APIMethodData();
			apiMethodData.initByIMethodBinding(targetAPIBindings.get(i));
			targetAPIs.add(apiMethodData);
		}
	}

	private void genereateAllAPIs() {
		allAPIs = new HashSet<>();
		List<MethodInvocation> invocations = methodAST.getMethodInvocations();
		for (int j = 0; j < invocations.size(); j++) {
			APIMethodData apiMethodData = new APIMethodData();

			IMethodBinding mBinding = invocations.get(j).resolveMethodBinding();
			if (mBinding != null) {
				apiMethodData.initByIMethodBinding(mBinding);
				allAPIs.add(apiMethodData);
			}
		}
	}

	public boolean isCodePattern_ConsecutiveAssertion() {
		List<String> signatureLines = new ArrayList<>(Arrays.asList(signature.getTestSignature()
			.toString().split("\n")));

		for (int i = 0; i < signatureLines.size(); i++) {
			if (!signatureLines.get(i).startsWith(InvocationSignature.TEST_LABEL))
				return false;
		}
		return true;
	}

	private List<String> slice_ConsecutiveAssertion() {
		List<String> sliceSnippets = new ArrayList<>();

		for (StatementAST stmt : methodAST.getStatements()) {
			boolean blockFlag = false;
			if (stmt.hasBlock()) {
				// ==block中的两种情况==
				// testIsSet()
				// for (int j=0x80; j <= 0x3F80; j+=0x80) {
				// assertTrue(bf_multi.isSet(j));
				// }
				// *******
				// testFactorial()
				// for (int i = 1; i < 21; i++) {
				// Assert.assertEquals(i + "! ", factorial(i),
				// Assert.assertEquals(i + "! ", factorial(i),
				// Assert.assertEquals(i + "! ", FastMath.log(factorial(i)),
				// }
				// =======
				// 现在对以上两种情况的解决都是：for循环体整个切出
				// TODO: 以后，按说应该对第二种情况有所作为！
				if (stmt.getSubStatements().size() > 1)
					;// TODO
			}

			String sliceSnippet = stmt.toString();
			sliceSnippets.add(sliceSnippet);
		}
		return sliceSnippets;
	}

	public boolean isCodePattern_TargetAssertionCycle() {
		if (targetMethods != null && !targetMethods.isEmpty()) {
			List<String> signatureLines = signature.getTargetTestCycleSignatureLines(targetMethods);
			boolean hasTarget = false;
			boolean isTest = false;
			for (int i = 0; i < signatureLines.size(); i++) {
				if (signatureLines.get(i).startsWith(InvocationSignature.TEST_LABEL)) {
					if (!isTest && !hasTarget)
						return false;

					isTest = true;
					hasTarget = false;
				} else if (signatureLines.get(i).startsWith(InvocationSignature.TARGET_LABEL)) {
					isTest = false;
					hasTarget = true;
				} else {
					isTest = false;
				}
			}
			return true;
		}

		return false;
	}

	private List<String> sliceByTarget(List<String> sliceTargets) {
		List<String> sliceSnippets = new ArrayList<>();

		// 先从调用序列入手，查找关键方法名出现的位置
		List<String> invocationLines = signature.getTestSignatureLines();
		// Lines();

		boolean isTargetPhase = true;

		int lastTestLineNumber = -1;
		List<Integer> targetLineNumbers = new ArrayList<>();
		List<Integer> testLineNumbers = new ArrayList<>();

		for (int i = 0; i < invocationLines.size(); i++) {
			boolean isTarget = false;
			boolean isTest = false;

			for (String targetWord : sliceTargets) {
				if (invocationLines.get(i).contains(targetWord)) {
					isTarget = true;
					break;
				}
			}

			if (isTarget) {
				if (!isTargetPhase) {
					// 遇到target行, 但在读测试行阶段，说明上一个测试用例已经结束了
					sliceSnippets.add(sliceByTargetAndTest(lastTestLineNumber, targetLineNumbers,
						testLineNumbers));

					// 清空数据，等待下一切片
					lastTestLineNumber = testLineNumbers.get(testLineNumbers.size() - 1);
					targetLineNumbers.clear();
					testLineNumbers.clear();
					isTargetPhase = true;
					targetLineNumbers.add(i);
				} else {
					// isTargetPhase==true
					// 遇到target行，在读target行时，读入即可
					targetLineNumbers.add(i);
				}
			} else {
				// !isTarget
				// 先判断是否在读测试行
				if (InvocationSignature.isTestLine(invocationLines.get(i)))
					isTest = true;

				if (isTest) {
					if (isTargetPhase) {
						// 遇到测试行，在读target行时，说明这一次用例已经进入测试阶段
						isTargetPhase = false;
						testLineNumbers.add(i);
					} else {
						// !isTargetPhase
						// 遇到测试行，在读测试行时，继续读入
						testLineNumbers.add(i);
					}
				}
				// 其余既不是Target也不是Test的读入不理睬
			}
		}
		// 处理结尾的最后一个段落
		if (!isTargetPhase) {
			sliceSnippets.add(sliceByTargetAndTest(lastTestLineNumber, targetLineNumbers,
				testLineNumbers));
		}

		// System.out.println(sliceSnippets);
		return sliceSnippets;
	}

	public boolean isCodePattern_WeakConsecutiveAssetions(List<String> signatureLines) {
		if (signatureLines.size() < WEAK_CONSECUTIVE_ASSETION_LENGTH_THRESHOLD)
			return false;

		int assertionCount = 0;
		for (int i = 0; i < signatureLines.size(); i++) {
			if (signatureLines.get(i).startsWith(InvocationSignature.TEST_LABEL)) {
				assertionCount++;
			} else if (assertionCount > 0) {
				// 开始读入tests之后不应再有中断，否则跳出
				return false;
			}
		}
		if ((double) assertionCount / signatureLines.size() > WEAK_CONSECUTIVE_ASSETION_PERCENTAGE_THRESHOLD)
			return true;
		else
			return false;
	}

	public boolean isCodePattern_WeakConsecutiveAssetionsWithBlock() {
		return isCodePattern_WeakConsecutiveAssetions(Arrays.asList(signature
			.getTestSignatureByStatements().split("\n")));
	}

	private List<String> slice_WeakConsecutiveAssertionsWithBlock() {
		List<String> sliceSnippets = new ArrayList<>();
		String[] signatureByLinesStrings = signature.getTestSignatureByStatements().split("\n");

		int lastPreLineNum = -1;
		for (int i = 0; i < signatureByLinesStrings.length; i++) {
			// 按照stmt给出的signature，每一行和stmt对应，故下标i可以通用
			StatementAST stmt = methodAST.getStatements().get(i);
			boolean isAssertionStmt = signatureByLinesStrings[i]
				.startsWith(InvocationSignature.TEST_LABEL);

			if (isAssertionStmt) {
				// 逐条assertion添加到切片组中
				StringBuilder str = new StringBuilder();

				str.append(stmt.getStatement().toString());

				List<VariableAST> relevantVars = new ArrayList<>(stmt.getRelevantVariables());

				for (int j = lastPreLineNum; j >= 0; j--) {
					StatementAST preStmt = methodAST.getStatements().get(j);

					List<VariableAST> curLineVars = new ArrayList<>(preStmt.getRelevantVariables());
					boolean isRelevant = false;
					for (int m = 0; m < curLineVars.size(); m++) {
						VariableAST curLineVar = curLineVars.get(m);

						for (int k = 0; k < relevantVars.size(); k++) {
							VariableAST relVar = relevantVars.get(k);

							if (relVar.equals(curLineVar)) {
								isRelevant = true;
								break;
							}
						}
						if (isRelevant)
							break;
					}

					if (isRelevant) {
						str.insert(0, preStmt.toString());
						relevantVars.addAll(curLineVars);
					}
				}

				sliceSnippets.add(str.toString());
			} else {
				lastPreLineNum = i;
			}
		}

		return sliceSnippets;
	}

	private List<String> findCommonLCSOfSignature() {
		List<String> lcs = new ArrayList<>();
		List<String> testLines = signature.getTestSignatureLines();

		List<List<String>> testParagraphs = separateByTests(testLines);

		for (int i = 1; i < testParagraphs.size(); i++) {
			if (i == 1)
				lcs = CollectionUtils.lcs(testParagraphs.get(i - 1), testParagraphs.get(i));
			else
				lcs = CollectionUtils.lcs(lcs, testParagraphs.get(i));

			// 根据test分段规则，必然最少有一个common <TEST>，size至少为1
			if (lcs.size() <= 1)
				return null;
		}

		return lcs;
	}

	private List<String> getLCSOfSignature() {
		List<String> lcs = new ArrayList<>();
		List<String> testLines = signature.getTestSignatureLines();

		int starter = 0;
		List<String> s1 = new ArrayList<>();
		List<String> s2 = new ArrayList<>();

		boolean isScanningAssertion = false;

		for (int i = 0; i < testLines.size(); i++) {
			boolean isAssertion = testLines.get(i).startsWith(InvocationSignature.TEST_LABEL);
			if ((isScanningAssertion && !isAssertion) || i == testLines.size() - 1) {
				if (i == testLines.size() - 1) {
					String line = testLines.get(i);
					s1.add(line.startsWith("<") ? line.substring(0, line.indexOf(">") + 1) : line);
					i++;
				}

				for (int j = 0; j < testLines.size(); j++) {
					if (j >= starter && j < i)
						continue;

					String line = testLines.get(j);
					s2.add(line.startsWith("<") ? line.substring(0, line.indexOf(">") + 1) : line);
				}

				List<String> temp = CollectionUtils.lcs(s1, s2);

				if (temp.size() > lcs.size())
					lcs = temp;

				if (i < testLines.size()) {
					starter = i;
					isScanningAssertion = false;
					s1 = new ArrayList<>();
					s2 = new ArrayList<>();
					String line = testLines.get(i);
					s1.add(line.startsWith("<") ? line.substring(0, line.indexOf(">") + 1) : line);
				}
			} else if (isAssertion) {
				isScanningAssertion = true;
				s1.add(InvocationSignature.TEST_LABEL);
			} else {
				String line = testLines.get(i);
				s1.add(line.startsWith("<") ? line.substring(0, line.indexOf(">") + 1) : line);
			}

		}

		return lcs;
	}

	private List<String> slice_AssertionSeparatedParagraph() {
		List<String> sliceSnippets = new ArrayList<>();

		List<List<Integer>> assertionParagraphs = new ArrayList<>();
		String[] sign = signature.getTestSignatureByStatements().split("\n");

		// 标签行状态
		// 1：读取中间调用
		// 2：读取assertion
		int state = 1;
		List<Integer> testLineNums = new ArrayList<>();
		for (int i = 0; i < sign.length; i++) {
			boolean isAssertion = sign[i].startsWith(InvocationSignature.TEST_LABEL);

			if (state == 2 && !isAssertion) {
				state = 1;
				assertionParagraphs.add(testLineNums);
				testLineNums = new ArrayList<>();
			} else if (isAssertion) {
				state = 2;
				testLineNums.add(i);
			}
		}
		if (state == 2)
			assertionParagraphs.add(testLineNums);

		if (assertionParagraphs.size() <= 1)
			sliceSnippets.add(methodAST.getMethodDeclarationNode().toString());
		else {
			for (int i = 0; i < assertionParagraphs.size(); i++) {
				int startStmtNum = i == 0 ? 0 : assertionParagraphs.get(i - 1).get(
					assertionParagraphs.get(i - 1).size() - 1) + 1;
				sliceSnippets.add(slice_SmallRootStrategy_ByRootStmts(startStmtNum,
					assertionParagraphs.get(i)));
			}
		}

		return sliceSnippets;
	}

	// used in findCommonLCSOfSignature()
	// 在找每一个scenario的LCS时候，都需要先把scenario大致分开，分开方式就是按照test
	private List<List<String>> separateByTests(List<String> lines) {
		List<List<String>> separatedList = new ArrayList<>();

		List<String> tempList = new ArrayList<>();
		boolean isScanningAssertion = false;

		for (int i = 0; i < lines.size(); i++) {
			boolean isAssertion = lines.get(i).startsWith(InvocationSignature.TEST_LABEL);
			if (isScanningAssertion && !isAssertion) {
				separatedList.add(tempList);
				isScanningAssertion = false;
				tempList = new ArrayList<>();
				String line = lines.get(i);
				tempList.add(line.startsWith("<") ? line.substring(0, line.indexOf(">") + 1) : line);
			} else if (isAssertion) {
				isScanningAssertion = true;
				tempList.add(InvocationSignature.TEST_LABEL);
			} else {
				String line = lines.get(i);
				tempList.add(line.startsWith("<") ? line.substring(0, line.indexOf(">") + 1) : line);
			}
		}
		separatedList.add(tempList);

		return separatedList;
	}

	// used in sliceByTarget
	// sliceByTarget第一阶段把每个test找出来，然后根据target和test来切分
	private String sliceByTargetAndTest(int lastTestLineNum, List<Integer> targetLineNum,
	                                    List<Integer> testLineNum) {
		Map<Integer, Integer> lineNumberIndex = signature.getLineNumberIndex();

		int startStmtNum = lastTestLineNum < 0 ? 0 : lineNumberIndex.get(lastTestLineNum) + 1;

		List<Integer> rootStmtNum = new ArrayList<>();

		for (Integer integer : targetLineNum)
			rootStmtNum.add(lineNumberIndex.get(integer));

		for (Integer integer : testLineNum)
			rootStmtNum.add(lineNumberIndex.get(integer));

		int strategy = 3;
		switch (strategy) {
			case 1:
				// strategy 1: paragraph slice
				return slice_ParagraphStrategy_ByRootStmts(startStmtNum,
					rootStmtNum.get(rootStmtNum.size() - 1));
			case 3:
				// strategy 3: small root slice
				return slice_SmallRootStrategy_ByRootStmts(startStmtNum, rootStmtNum);
		}

		return "";
	}

	// used in sliceByTargetAndTest
	private String slice_ParagraphStrategy_ByRootStmts(int startStmtNum, int endStmtNum) {
		StringBuilder sliceBuilder = new StringBuilder();

		HashSet<VariableAST> relevantVariables = new HashSet<>();

		// 把中间那段方法取出
		for (int i = startStmtNum; i <= endStmtNum; i++) {
			StatementAST stmtAst = methodAST.getStatements().get(i);
			sliceBuilder.append(stmtAst.getStatement().toString());
			relevantVariables.addAll(stmtAst.getRelevantVariables());
		}

		// 把变量的定义切分出

		List<VariableAST> vars = methodAST.getVariableDeclarations();
		for (int i = vars.size() - 1; i >= 0; i--) {
			for (VariableAST relVar : relevantVariables) {
				if (relVar.equals(vars.get(i)) && sliceBuilder.indexOf(vars.get(i).toString()) < 0)
					sliceBuilder.insert(0, vars.get(i));
			}
		}

		return sliceBuilder.toString();
	}

	// used in sliceByTargetAndTest
	// used in slice_AssertionSeparatedParagraph
	private String slice_SmallRootStrategy_ByRootStmts(int startStmtNum,
	                                                   List<Integer> rootStmtsIndex) {
		StringBuilder sliceBuilder = new StringBuilder();

		HashSet<VariableAST> relevantVariables = new HashSet<>();

		// 生成relevantVar的初始集
		for (int i = 0; i < rootStmtsIndex.size(); i++) {
			StatementAST stmtAst = methodAST.getStatements().get(rootStmtsIndex.get(i));
			relevantVariables.addAll(stmtAst.getRelevantVariables());
		}

		// 从root集最后一个语句向前切分到lastassert之后
		for (int i = rootStmtsIndex.get(rootStmtsIndex.size() - 1); i >= startStmtNum; i--) {
			StatementAST stmtAst = methodAST.getStatements().get(i);

			if (rootStmtsIndex.contains(i))
				sliceBuilder.insert(0, stmtAst.getStatement().toString());
			else {
				boolean flag = false;
				for (VariableAST rootVar : relevantVariables) {
					for (VariableAST curStmtVar : stmtAst.getRelevantVariables()) {

						if (rootVar.equals(curStmtVar)) {
							flag = true;
							break;
						}
					}
					if (flag)
						break;
				}

				// 如果是相关语句，假如slice中，并累积相关变量
				if (flag) {
					sliceBuilder.insert(0, stmtAst.getStatement().toString());
					relevantVariables.addAll(stmtAst.getRelevantVariables());
				}
			}
		}

		List<VariableAST> vars = methodAST.getVariableDeclarations();
		for (int i = vars.size() - 1; i >= 0; i--) {
			for (VariableAST relVar : relevantVariables) {
				if (relVar.equals(vars.get(i)) && sliceBuilder.indexOf(vars.get(i).toString()) < 0)
					sliceBuilder.insert(0, vars.get(i));
			}
		}

		return sliceBuilder.toString();

	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public List<String> getSliceSnippets() {
		return sliceSnippets;
	}

	public List<Slice> getSlices() {
		return slices;
	}

	public void setSlices(List<Slice> slices) {
		this.slices = slices;
	}

}

