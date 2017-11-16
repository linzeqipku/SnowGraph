package graphdb.extractors.miners.codesnippet;

import de.parsemis.graph.Graph;
import graphdb.extractors.miners.codesnippet.code.cfg.ddg.DDG;
import graphdb.extractors.miners.codesnippet.code.mining.Miner;
import graphdb.extractors.miners.codesnippet.code.mining.MiningNode;
import graphdb.extractors.miners.codesnippet.mail.MailBodyProcessor;
import graphdb.extractors.miners.codesnippet.mail.Segment;
import graphdb.extractors.miners.codesnippet.stackoverflow.StackOverflowParser;
import graphdb.extractors.miners.codesnippet.stackoverflow.entity.ContentInfo;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.EntityDeclaration;
import graphdb.framework.annotations.PropertyDeclaration;
import graphdb.framework.annotations.RelationshipDeclaration;
import utils.ParseUtil;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CodeSnippetExtractor implements Extractor {

	@EntityDeclaration
	public static final String CODE_SNIPPET = "CodeSnippet";
	@PropertyDeclaration(parent = CODE_SNIPPET)
	public static final String CODE_SNIPPET_BODY = "body";
	@RelationshipDeclaration
	public static final String CONTAIN_SNIPPET = "containSnippet";
	@RelationshipDeclaration
	public static final String CONTAINS_API = "containsAPI";
	@RelationshipDeclaration
	public static final String CODE_EXAMPLE = "codeExample";

	private boolean parseMail = true;
	private boolean parseStackoverflow = true;

	public void createContainsAPILink(GraphDatabaseService db) {
		try (Transaction tx = db.beginTx()) {
			ResourceIterator<Node> ite = db.findNodes(Label.label(CodeSnippetExtractor.CODE_SNIPPET));
			while (ite.hasNext()) {
				Node node = ite.next();
				String code = node.getProperty(CodeSnippetExtractor.CODE_SNIPPET_BODY).toString();
				System.out.println(code);
				ASTNode root = ParseUtil.parse(code, ASTParser.K_STATEMENTS);
				root.accept(new APIVisitor(node, db));
			}
			tx.success();
		}
	}

	public void createCodeExampleLink(GraphDatabaseService db) {
		try (Transaction tx = db.beginTx()) {
			ResourceIterator<Node> ite = db.findNodes(Label.label(JavaCodeExtractor.METHOD));
			while (ite.hasNext()) {
				Node node = ite.next();
				Iterable<Relationship> edges = node.getRelationships(RelationshipType.withName(CONTAINS_API));
				List<Pair<Node, String>> snippets = StreamSupport.stream(edges.spliterator(), false)
					.map(r -> r.getOtherNode(node))
					.map(n -> Pair.of(n, n.getProperty(CodeSnippetExtractor.CODE_SNIPPET_BODY).toString()))
					.collect(Collectors.toList());

				Map<Node, DDG> ddgs = new HashMap<>();
				snippets.forEach(p -> ddgs.put(p.getLeft(), DDG.createCFG(p.getRight())));
				List<Graph<MiningNode, Integer>> frequents = Miner.mineGraphFromDDG(ddgs.values(), Miner.createSetting(3, 3));
				List<Node> result = Sorter.sort(ddgs, frequents);
				result.stream().limit(5).forEach(r -> node.createRelationshipTo(r, RelationshipType.withName(CODE_EXAMPLE)));
			}
			tx.success();
		}
	}

	public boolean isParseMail() {
		return parseMail;
	}

	public void setParseMail(boolean parseMail) {
		this.parseMail = parseMail;
	}

	public boolean isParseStackoverflow() {
		return parseStackoverflow;
	}

	public void setParseStackoverflow(boolean parseStackoverflow) {
		this.parseStackoverflow = parseStackoverflow;
	}

	@Override
	public void run(GraphDatabaseService db) {
		if (parseMail) extractFromMail(db);
		if (parseStackoverflow) extractFromStackoverflow(db);
		createContainsAPILink(db);
		createCodeExampleLink(db);
	}

	public void extractFromMail(GraphDatabaseService db) {
		try (Transaction tx = db.beginTx()) {
			long total = db.findNodes(Label.label(MailListExtractor.MAIL)).stream().count();
			long count = 0;
			ResourceIterator<Node> ite = db.findNodes(Label.label(MailListExtractor.MAIL));
			while (ite.hasNext()) {
				++count;
				System.out.println(String.format("%s/%s", count, total));

				long beginTime = System.currentTimeMillis();
				Node mailNode = ite.next();
				if (isCommitMail(mailNode)) continue;
				String mailBody = mailNode.getProperty(MailListExtractor.MAIL_BODY).toString();
				if (mailBody.startsWith("Dear Wiki user,")) continue;
				if (mailBody.startsWith("<")) continue;

				List<String> lines = MailBodyProcessor.bodyToLines(mailBody);
				List<Segment> segments = MailBodyProcessor.linesToSegments(lines);
				MailBodyProcessor.filterSignature(segments);
				MailBodyProcessor.filterReference(segments);
				segments = MailBodyProcessor.getCodes(segments);
				segments.stream().filter(Segment::isCode).forEach(s -> {
					Node codeNode = db.createNode(Label.label(CODE_SNIPPET));
					codeNode.setProperty(CODE_SNIPPET_BODY, s.getText());
					mailNode.createRelationshipTo(codeNode, RelationshipType.withName(CONTAIN_SNIPPET));
				});
				long endTime = System.currentTimeMillis();
				System.out.println(String.format("Uses %d ms.", endTime - beginTime));
				if (endTime - beginTime > 30000) {
					File f = new File("out/" + mailNode.getProperty(MailListExtractor.MAIL_ID).toString());
					PrintStream ps = new PrintStream(f);
					ps.println(mailBody);
					ps.close();
				}
			}
			tx.success();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void extractFromStackoverflow(GraphDatabaseService db) {
		try (Transaction tx = db.beginTx()) {
			long total = db.findNodes(Label.label(StackOverflowExtractor.QUESTION)).stream().count();
			total += db.findNodes(Label.label(StackOverflowExtractor.ANSWER)).stream().count();
			long count = 0;
			ResourceIterator<Node> iteQuestion = db.findNodes(Label.label(StackOverflowExtractor.QUESTION));
			ResourceIterator<Node> iteAnswer = db.findNodes(Label.label(StackOverflowExtractor.ANSWER));
			while (iteQuestion.hasNext()) {
				++count;
				System.out.println(String.format("%s/%s", count, total));
				createLink(db, iteQuestion.next());
			}
			while (iteAnswer.hasNext()) {
				++count;
				System.out.println(String.format("%s/%s", count, total));
				createLink(db, iteAnswer.next());
			}
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createLink(GraphDatabaseService db, Node node) {
		String body = node.getProperty(StackOverflowExtractor.QUESTION_BODY).toString();

		ContentInfo content = StackOverflowParser.parse(body);
		content.getParagraphList().stream()
			.flatMap(p -> ParseUtil.getMethodBodys(p.getContent()).stream())
			.forEach(c -> {
				Node codeNode = db.createNode(Label.label(CODE_SNIPPET));
				codeNode.setProperty(CODE_SNIPPET_BODY, c);
				node.createRelationshipTo(codeNode, RelationshipType.withName(CONTAIN_SNIPPET));
			});
	}

	private boolean isCommitMail(Node mailNode) {
		return mailNode.getProperty(MailListExtractor.MAIL_SUBJECT).toString().startsWith("svn commit:");
	}
}
