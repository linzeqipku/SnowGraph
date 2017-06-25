package graphdb.extractors.linkers.codetosnippet;

import de.parsemis.graph.Graph;
import graphdb.extractors.linkers.codetosnippet.code.cfg.ddg.DDG;
import graphdb.extractors.linkers.codetosnippet.code.mining.Miner;
import graphdb.extractors.linkers.codetosnippet.code.mining.MiningNode;
import graphdb.extractors.miners.codesnippet.CodeSnippetExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.utils.ParseUtil;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.RelationshipDeclaration;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CodeToSnippetExtractor implements Extractor {

	@RelationshipDeclaration
	public static final String CONTAINS_API = "containsAPI";

	@RelationshipDeclaration
	public static final String CODE_EXAMPLE = "codeExample";

	@Override
	public void run(GraphDatabaseService graphDB) {
		createContainsAPILink(graphDB);
		createCodeExampleLink(graphDB);
	}

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

}
