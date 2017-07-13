package graphdb.extractors.miners.apiusage;

import com.google.common.collect.ImmutableSet;
import graphdb.extractors.miners.apiusage.codeslice.Clusters;
import graphdb.extractors.miners.apiusage.entity.Slice;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.EntityDeclaration;
import graphdb.framework.annotations.PropertyDeclaration;
import graphdb.framework.annotations.RelationshipDeclaration;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.neo4j.graphdb.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class APIUsageExtractor implements Extractor {
	private static final int EXAMPLE_PER_CLUSTER = 2;

	@EntityDeclaration
	public static final String API_USAGE_EXAMPLE = "APIUsageExample";
	@PropertyDeclaration(parent = API_USAGE_EXAMPLE)
	public static final String EXAMPLE_BODY = "body";
	@RelationshipDeclaration
	public static final String HAS_EXAMPLE = "hasExample";

	private String srcPath = "";

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	@Override
	public void run(GraphDatabaseService db) {
		StopWatch watch = new StopWatch();
		watch.start();
		Set<Slice> slices = generateExamples();
		watch.stop();
		long time = watch.getTime();
		System.out.println(String.format("Generate examples using %dms.", time));

		try (Transaction tx = db.beginTx()) {
			db.findNodes(Label.label(JavaCodeExtractor.METHOD)).stream().forEach(node -> {
				String belongTo = (String) node.getProperty(JavaCodeExtractor.METHOD_BELONGTO);
				String name = (String) node.getProperty(JavaCodeExtractor.METHOD_NAME);
				String params = (String) node.getProperty(JavaCodeExtractor.METHOD_PARAMS);
				params = params.replaceAll("final ", "");
				params = Stream.of(params.split(",")).map(String::trim).map(s -> s.split(" ")[0]).reduce((a, b) -> a + "," + b).orElse("");
				String signature = String.format("%s.%s(%s)", belongTo, name, params);
				List<Slice> examples = slices.stream().filter(slice -> slice.getTargetAPIs().stream().map(Object::toString).anyMatch(s -> s.equals(signature))).collect(Collectors.toList());

				Clusters clusters = new Clusters(examples);

				for (int i = 0; i < clusters.getClusters().size(); i++) {
					for (int j = 0; j < EXAMPLE_PER_CLUSTER && j < clusters.getClusters().get(i).size(); j++) {
						String example = clusters.getClusters().get(i).get(j).getSlice();
						Node exampleNode = db.createNode(Label.label(API_USAGE_EXAMPLE));
						exampleNode.setProperty(EXAMPLE_BODY, example);
						node.createRelationshipTo(exampleNode, RelationshipType.withName(HAS_EXAMPLE));
					}
				}

			});
			tx.success();
		}
	}

	private Set<Slice> generateExamples() {
		Set<Slice> slices = new HashSet<>();

		File srcFile = new File(srcPath);
		String[] testPaths = getTestFiles(srcFile).parallelStream().map(File::getAbsolutePath).toArray(String[]::new);
		String[] folderPaths = getAllFiles(srcFile).parallelStream().map(File::getParentFile).map(File::getAbsolutePath).toArray(String[]::new);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(null, folderPaths, null, true);
		parser.setResolveBindings(true);
		Map<String, String> options = new Hashtable<>();
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		parser.setCompilerOptions(options);
		parser.setBindingsRecovery(true);
		parser.createASTs(testPaths, null, new String[]{}, new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit javaUnit) {
				APIMethodVisitor visitor = new APIMethodVisitor();
				javaUnit.accept(visitor);
				slices.addAll(visitor.getSlices());
			}
		}, null);
		return slices;
	}

	private ImmutableSet<File> getAllFiles(File dir) {
		ImmutableSet<File> filesFromDir = Stream.of(dir.listFiles())
			.filter(File::isDirectory)
			.map(this::getAllFiles)
			.flatMap(ImmutableSet::parallelStream)
			.collect(ImmutableSet.toImmutableSet());

		ImmutableSet<File> files = Stream.of(dir.listFiles())
			.filter(f -> !f.isDirectory())
			.filter(f -> f.getName().endsWith(".java"))
			.collect(ImmutableSet.toImmutableSet());

		return new ImmutableSet.Builder<File>().addAll(filesFromDir).addAll(files).build();
	}

	private ImmutableSet<File> getTestFiles(File dir) {
		if (dir.getName().equals("test")) return getFilesInTest(dir);
		return Stream.of(dir.listFiles())
			.filter(File::isDirectory)
			.map(this::getTestFiles)
			.flatMap(ImmutableSet::parallelStream)
//			.filter(f -> f.getName().startsWith("TestCustomSeparatorBreakIterator"))
			.collect(ImmutableSet.toImmutableSet());
	}

	private ImmutableSet<File> getFilesInTest(File dir) {
		ImmutableSet<File> filesFromDir = Stream.of(dir.listFiles())
			.filter(File::isDirectory)
			.map(this::getFilesInTest)
			.flatMap(ImmutableSet::parallelStream)
			.collect(ImmutableSet.toImmutableSet());

		ImmutableSet<File> files = Stream.of(dir.listFiles())
			.filter(f -> !f.isDirectory())
			.filter(f -> f.getName().endsWith(".java"))
			.collect(ImmutableSet.toImmutableSet());

		return new ImmutableSet.Builder<File>().addAll(filesFromDir).addAll(files).build();
	}
}
