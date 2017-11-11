package graphlocater.utils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.RuntimeInterruptedException;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

// 似乎当时相结合Callable+FutureTask对时间进行一些监控，超时超内存跳出
public class StanfordParser implements Callable<Tree> {

	public static LexicalizedParser lexicalizedParser;
	public static TokenizerFactory<CoreLabel> tokenizerFactory;

	private String strToParse;
	private Tree parsedTree;

	static {
		lexicalizedParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	}


	public StanfordParser(String strToParse) {
		super();
		this.strToParse = strToParse;
	}

	public static Tree parseTree(String str) {
		StanfordParser parser = new StanfordParser(str);
		FutureTask<Tree> parserTask = new FutureTask<Tree>(parser);
		ThreadGroup stanfordParserThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(),
				"Stanford-Parser-TG");
		Thread parserThread = new Thread(stanfordParserThreadGroup, parserTask);

		parserThread.start();

		Tree resultTree;
		try {
			resultTree = parserTask.get();
		}
		catch (Exception e) {
			// e.printStackTrace();
			resultTree = null;
		}

		return resultTree;
	}

	// Callable, 会被FutureTask调用
	public Tree call() throws Exception {
		this.parseTree();
		return parsedTree;
	}

	private void parseTree() {
		try {
			List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(strToParse)).tokenize();
			this.parsedTree = lexicalizedParser.apply(rawWords);
		}
		catch (RuntimeInterruptedException e) {
            e.printStackTrace();
		}
	}


	public static Tree parseTreeWithoutMonitoring(String str) {
		long t1 = System.currentTimeMillis();

		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(str)).tokenize();
		Tree tree = lexicalizedParser.apply(rawWords);

		long t2 = System.currentTimeMillis();

		return tree;
	}
}
