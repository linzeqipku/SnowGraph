package exps.codepattern.code.cfg.basiccfg;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import exps.codepattern.adt.graph.Node;
import exps.codepattern.code.cfg.CFG;
import exps.codepattern.code.cfg.SSACFGConverter;
import exps.codepattern.code.cfg.ddg.DDG;
import exps.codepattern.code.ir.IRRepresentation;
import exps.codepattern.code.ir.UnsupportedSyntaxException;
import exps.codepattern.code.ir.VariableUnit;
import exps.codepattern.code.ir.statement.IRAssignment;
import exps.codepattern.code.ir.statement.IRLabel;
import exps.codepattern.code.ir.statement.IRStatement;

import java.util.*;

/**
 * 常规控制流图的实现
 *
 * @author huacy
 */
public class BasicCFG implements CFG {
	private static Logger logger = LoggerFactory.getLogger(BasicCFG.class);

	private int maxBlockNum = 0;
	private boolean isSSAForm = false;
	private BasicCFGRegularBlock currentBlock;
	private BasicCFGSpecialBlock.Entry entry;
	private BasicCFGSpecialBlock.Exit exit;
	private Set<AbstractBasicCFGBlock> blocks = new HashSet<>();
	private Map<IRLabel, AbstractBasicCFGBlock> labelMap = new HashMap<>();
	private Map<BasicCFGRegularBlock, IRLabel> gotoInfo = new HashMap<>();
	private Map<VariableUnit, CFGVariableImpl> variableMap = new HashMap<>();

	public static void main(String[] args) {
		String code = "{\n" +
			"  if (query instanceof SpanQuery) {\n" +
			"    SpanQuery sq=(SpanQuery)query;\n" +
			"    if (sq.getField() != null && sq.getField().equals(field)) {\n" +
			"      return (SpanQuery)query;\n" +
			"    }\n" +
			" else {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			"  }\n" +
			" else   if (query instanceof BooleanQuery) {\n" +
			"    List<BooleanClause> queryClauses=((BooleanQuery)query).clauses();\n" +
			"    List<SpanQuery> spanQs=new ArrayList<>();\n" +
			"    for (int i=0; i < queryClauses.size(); i++) {\n" +
			"      if (!queryClauses.get(i).isProhibited()) {\n" +
			"        tryToAdd(field,convert(field,queryClauses.get(i).getQuery()),spanQs);\n" +
			"      }\n" +
			"    }\n" +
			"    if (spanQs.size() == 0) {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			" else     if (spanQs.size() == 1) {\n" +
			"      return spanQs.get(0);\n" +
			"    }\n" +
			" else {\n" +
			"      return new SpanOrQuery(spanQs.toArray(new SpanQuery[spanQs.size()]));\n" +
			"    }\n" +
			"  }\n" +
			" else   if (query instanceof PhraseQuery) {\n" +
			"    PhraseQuery phraseQuery=((PhraseQuery)query);\n" +
			"    Term[] phraseQueryTerms=phraseQuery.getTerms();\n" +
			"    if (phraseQueryTerms.length == 0) {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			" else     if (!phraseQueryTerms[0].field().equals(field)) {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			"    SpanQuery[] clauses=new SpanQuery[phraseQueryTerms.length];\n" +
			"    for (int i=0; i < phraseQueryTerms.length; i++) {\n" +
			"      clauses[i]=new SpanTermQuery(phraseQueryTerms[i]);\n" +
			"    }\n" +
			"    int slop=phraseQuery.getSlop();\n" +
			"    int[] positions=phraseQuery.getPositions();\n" +
			"    if (positions.length > 0) {\n" +
			"      int lastPos=positions[0];\n" +
			"      int sz=positions.length;\n" +
			"      for (int i=1; i < sz; i++) {\n" +
			"        int pos=positions[i];\n" +
			"        int inc=pos - lastPos - 1;\n" +
			"        slop+=inc;\n" +
			"        lastPos=pos;\n" +
			"      }\n" +
			"    }\n" +
			"    boolean inorder=false;\n" +
			"    if (phraseQuery.getSlop() == 0) {\n" +
			"      inorder=true;\n" +
			"    }\n" +
			"    SpanNearQuery sp=new SpanNearQuery(clauses,slop,inorder);\n" +
			"    if (query instanceof BoostQuery) {\n" +
			"      return new SpanBoostQuery(sp,((BoostQuery)query).getBoost());\n" +
			"    }\n" +
			" else {\n" +
			"      return sp;\n" +
			"    }\n" +
			"  }\n" +
			" else   if (query instanceof TermQuery) {\n" +
			"    TermQuery tq=(TermQuery)query;\n" +
			"    if (tq.getTerm().field().equals(field)) {\n" +
			"      return new SpanTermQuery(tq.getTerm());\n" +
			"    }\n" +
			" else {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			"  }\n" +
			" else   if (query instanceof ConstantScoreQuery) {\n" +
			"    return convert(field,((ConstantScoreQuery)query).getQuery());\n" +
			"  }\n" +
			" else   if (query instanceof DisjunctionMaxQuery) {\n" +
			"    List<SpanQuery> spanQs=new ArrayList<SpanQuery>();\n" +
			"    for (Iterator<Query> iterator=((DisjunctionMaxQuery)query).iterator(); iterator.hasNext(); ) {\n" +
			"      tryToAdd(field,convert(field,iterator.next()),spanQs);\n" +
			"    }\n" +
			"    if (spanQs.size() == 0) {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			" else     if (spanQs.size() == 1) {\n" +
			"      return spanQs.get(0);\n" +
			"    }\n" +
			" else {\n" +
			"      return new SpanOrQuery(spanQs.toArray(new SpanQuery[spanQs.size()]));\n" +
			"    }\n" +
			"  }\n" +
			" else   if (query instanceof MatchAllDocsQuery) {\n" +
			"    return getEmptySpanQuery();\n" +
			"  }\n" +
			" else   if (query instanceof MultiPhraseQuery) {\n" +
			"    final MultiPhraseQuery mpq=(MultiPhraseQuery)query;\n" +
			"    final Term[][] termArrays=mpq.getTermArrays();\n" +
			"    if (termArrays.length == 0) {\n" +
			"      return getEmptySpanQuery();\n" +
			"    }\n" +
			" else     if (termArrays.length > 1) {\n" +
			"      Term[] ts=termArrays[0];\n" +
			"      if (ts.length > 0) {\n" +
			"        Term t=ts[0];\n" +
			"        if (!t.field().equals(field)) {\n" +
			"          return getEmptySpanQuery();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"    final int[] positions=mpq.getPositions();\n" +
			"    if (positions.length > 0) {\n" +
			"      int maxPosition=positions[positions.length - 1];\n" +
			"      for (int i=0; i < positions.length - 1; ++i) {\n" +
			"        if (positions[i] > maxPosition) {\n" +
			"          maxPosition=positions[i];\n" +
			"        }\n" +
			"      }\n" +
			"      @SuppressWarnings(\"unchecked\") final List<SpanQuery>[] disjunctLists=new List[maxPosition + 1];\n" +
			"      int distinctPositions=0;\n" +
			"      for (int i=0; i < termArrays.length; ++i) {\n" +
			"        final Term[] termArray=termArrays[i];\n" +
			"        List<SpanQuery> disjuncts=disjunctLists[positions[i]];\n" +
			"        if (disjuncts == null) {\n" +
			"          disjuncts=(disjunctLists[positions[i]]=new ArrayList<SpanQuery>(termArray.length));\n" +
			"          ++distinctPositions;\n" +
			"        }\n" +
			"        for (int j=0; j < termArray.length; ++j) {\n" +
			"          disjuncts.add(new SpanTermQuery(termArray[j]));\n" +
			"        }\n" +
			"      }\n" +
			"      int positionGaps=0;\n" +
			"      int position=0;\n" +
			"      final SpanQuery[] clauses=new SpanQuery[distinctPositions];\n" +
			"      for (int i=0; i < disjunctLists.length; ++i) {\n" +
			"        List<SpanQuery> disjuncts=disjunctLists[i];\n" +
			"        if (disjuncts != null) {\n" +
			"          if (disjuncts.size() == 1) {\n" +
			"            clauses[position++]=disjuncts.get(0);\n" +
			"          }\n" +
			" else {\n" +
			"            clauses[position++]=new SpanOrQuery(disjuncts.toArray(new SpanQuery[disjuncts.size()]));\n" +
			"          }\n" +
			"        }\n" +
			" else {\n" +
			"          ++positionGaps;\n" +
			"        }\n" +
			"      }\n" +
			"      final int slop=mpq.getSlop();\n" +
			"      final boolean inorder=(slop == 0);\n" +
			"      SpanNearQuery sp=new SpanNearQuery(clauses,slop + positionGaps,inorder);\n" +
			"      if (query instanceof BoostQuery) {\n" +
			"        return new SpanBoostQuery(sp,((BoostQuery)query).getBoost());\n" +
			"      }\n" +
			" else {\n" +
			"        return sp;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			" else   if (query instanceof MultiTermQuery) {\n" +
			"    return new SpanMultiTermQueryWrapper<>((MultiTermQuery)query);\n" +
			"  }\n" +
			" else   if (query instanceof SynonymQuery) {\n" +
			"    List<SpanQuery> clauses=new ArrayList<>();\n" +
			"    for (    Term term : ((SynonymQuery)query).getTerms()) {\n" +
			"      clauses.add(new SpanTermQuery(term));\n" +
			"    }\n" +
			"    return new SpanOrQuery(clauses.toArray(new SpanQuery[clauses.size()]));\n" +
			"  }\n" +
			"  throw new IllegalArgumentException(\"Can't convert query of type: \" + query.getClass());\n" +
			"}\n";
		DDG.createCFG(code);
	}

	private BasicCFG() {
	}

	public static CFG createCFG(IRRepresentation irRepresentation, boolean isSSAForm) {
		BasicCFG cfg = new BasicCFG();
		cfg.entry = new BasicCFGSpecialBlock.Entry(cfg, cfg.maxBlockNum++);
		cfg.exit = new BasicCFGSpecialBlock.Exit(cfg, cfg.maxBlockNum++);
		cfg.currentBlock = cfg.createRegularBlock();
		cfg.entry.setNext(cfg.currentBlock);
		irRepresentation.buildCFG(cfg.currentBlock).setNext(cfg.exit);
		cfg.setupGotoInfo();
		cfg.setupVariable(irRepresentation);
		cfg.optimize();
		if (isSSAForm) SSACFGConverter.convertToSSAForm(cfg);
		return cfg;
	}

	public static CFG createCFG(String methodBody, boolean isSSAForm) {
		try {
			IRRepresentation body = IRRepresentation.create(methodBody);
			return createCFG(body, isSSAForm);
		} catch (UnsupportedSyntaxException e) {
			logger.warn("Unsupported syntax!");
			logger.warn("\n" + methodBody);
			logger.warn("{}", e.getStackTrace()[0]);
			return null;
		} catch (NullPointerException e) {
			logger.error("Null Pointer Exception!");
			logger.error(methodBody);
			e.printStackTrace();
			return null;
		}
	}

	public BasicCFGRegularBlock createRegularBlock() {
		BasicCFGRegularBlock block = new BasicCFGRegularBlock(this, maxBlockNum++);
		blocks.add(block);
		return block;
	}

	public BasicCFGConditionBlock createConditionBlock() {
		BasicCFGConditionBlock block = new BasicCFGConditionBlock(this, maxBlockNum++);
		blocks.add(block);
		return block;
	}

	public boolean isSSAForm() {
		return isSSAForm;
	}

	public void ssaSetup() {
		isSSAForm = true;
		BasicCFGRegularBlock exitPrev = createRegularBlock();
		exitPrev.prevs.addAll(exit.getPrevs());
		for (AbstractBasicCFGBlock block : exit.getPrevs()) {
			if (block instanceof BasicCFGRegularBlock) ((BasicCFGRegularBlock) block).setNext(exitPrev);
			else if (block instanceof BasicCFGSpecialBlock.Entry)
				((BasicCFGSpecialBlock.Entry) block).setNext(exitPrev);
			else if (block instanceof BasicCFGConditionBlock)
				((BasicCFGConditionBlock) block).replaceNext(exit, exitPrev);
		}
		exit.prevs.clear();
		exitPrev.setNext(exit);
	}

	public CFGVariableImpl getVariable(VariableUnit variableUnit) {
		return variableMap.get(variableUnit);
	}

	public void mapLabelBlock(IRLabel label, AbstractBasicCFGBlock block) {
		labelMap.put(label, block);
	}

	public void recordGotoInfo(BasicCFGRegularBlock block, IRLabel label) {
		gotoInfo.put(block, label);
	}

	private void setupGotoInfo() {
		gotoInfo.forEach((block, label) -> {
			AbstractBasicCFGBlock nextBlock = labelMap.get(label);
			block.setNext(nextBlock);
		});
	}

	private void setupVariable(IRRepresentation irRepresentation) {
		irRepresentation.getVariables().forEach(v -> variableMap.put(v, new CFGVariableImpl(v)));
	}

	private void optimize() {
		optimizeTempVar();
		// 去除空Block
		blocks.stream()
			.filter(x -> x instanceof BasicCFGRegularBlock)
			.map(x -> (BasicCFGRegularBlock) x)
			.filter(x -> x.getStatements().isEmpty())
			.forEach(BasicCFGRegularBlock::removeBlock);
		// 去除不可达Block
		entry.visit();
		blocks.removeIf(x -> !x.reachable);
		blocks.forEach(AbstractBasicCFGBlock::checkPrev);
		exit.checkPrev();
	}

	private void optimizeTempVar() {
		List<VariableUnit> removedVar = new ArrayList<>();
		variableMap.forEach((unit, var) -> {
			if (!unit.getName().startsWith("#")) return;
			if (unit.getUseBoxes().count() != 1) return;
			if (unit.getDefBoxes().count() != 1) return;
			IRStatement useStatement = unit.getUseBoxes().findFirst().get();
			IRStatement defStatement = unit.getDefBoxes().findFirst().get();
			if (useStatement.getBelongBlock() != defStatement.getBelongBlock()) return;
			if (!(useStatement instanceof IRAssignment)) return;
			defStatement.replaceDef(useStatement.getDef());
			useStatement.getBelongBlock().removeStatement(useStatement);
			removedVar.add(unit);
		});
		removedVar.forEach(variableMap::remove);
	}

	@Override
	public ImmutableSet<AbstractBasicCFGBlock> getBlocks() {
		return new ImmutableSet.Builder<AbstractBasicCFGBlock>().addAll(blocks).add(entry).add(exit).build();
	}

	@Override
	public BasicCFGSpecialBlock.Exit getExit() {
		return exit;
	}

	@Override
	public BasicCFGSpecialBlock.Entry getEntry() {
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
