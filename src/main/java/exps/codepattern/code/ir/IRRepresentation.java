package exps.codepattern.code.ir;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.BodyDeclaration;
import exps.codepattern.code.cfg.basiccfg.BasicCFGRegularBlock;
import exps.codepattern.code.ir.IRExpression.IRExtern;
import exps.codepattern.code.ir.IRExpression.IRTemp;
import exps.codepattern.code.ir.IRExpression.IRVariable;
import exps.codepattern.code.ir.statement.IRAbstractStatement;
import exps.codepattern.code.ir.statement.IRLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IRRepresentation implements IRScope {
    private static Logger logger = LoggerFactory.getLogger(IRRepresentation.class);

    private int tempNum = 0, labelNum = 0;
    private List<IRAbstractStatement> statements = new ArrayList<>();
    private Map<String, VariableUnit> variables = new HashMap<>();

    private IRRepresentation(String methodBody) {
        final BodyDeclaration<?> cu = JavaParser.parseBodyDeclaration(methodBody);
        cu.accept(new IRVisitor(this), new VisitorContext(this, null, null));
    }

    public static IRRepresentation create(String methodBody) {
        return new IRRepresentation(methodBody);
    }

    public static void main(String[] args) {
        IRRepresentation ir = IRRepresentation.create("final public void testDeleteDocumentint() throws Exception {\n" +
            "\t\tSimpleAnalyzer analyzer = new SimpleAnalyzer();\n" +
            "\t\tMockControl indexFactoryControl = MockControl.createStrictControl(IndexFactory.class);\n" +
            "\t\tIndexFactory indexFactory = (IndexFactory)indexFactoryControl.getMock();\n" +
            "\t\tMockControl indexReaderControl = MockControl.createStrictControl(LuceneIndexReader.class);\n" +
            "\t\tLuceneIndexReader indexReader = (LuceneIndexReader)indexReaderControl.getMock();\n" +
            "\n" +
            "\t\tindexFactory.getIndexReader();\n" +
            "\t\tindexFactoryControl.setReturnValue(indexReader, 1);\n" +
            "\t\t\n" +
            "\t\tindexReader.deleteDocument(0);\n" +
            "\t\tindexReaderControl.setVoidCallable(1);\n" +
            "\t\t\n" +
            "\t\tindexReader.close();\n" +
            "\t\tindexReaderControl.setVoidCallable(1);\n" +
            "\t\t\n" +
            "\t\tindexFactoryControl.replay();\n" +
            "\t\tindexReaderControl.replay();\n" +
            "\t\t\n" +
            "\t\t//Lucene template\n" +
            "\t\tLuceneIndexTemplate template = new DefaultLuceneIndexTemplate(indexFactory, analyzer);\n" +
            "\t\ttemplate.deleteDocument(0);\n" +
            "\n" +
            "\t\tindexFactoryControl.verify();\n" +
            "\t\tindexReaderControl.verify();\n" +
            "\t}");
        ir.output();
    }

    @Override
    public void addStatement(IRAbstractStatement statement) {
        statements.add(statement);
    }

    public IRExpression getVariableOrExtern(String name) {
        VariableUnit v = variables.get(name);
        if (v != null) return new IRVariable(v);
        return new IRExtern(name);
    }

    public IRVariable getVariableOrCreate(String name) {
        VariableUnit v;
        v = variables.get(name);
        if (v == null) {
            v = new VariableUnit(name);
            variables.put(name, v);
        }
        return new IRVariable(v);
    }

    public Collection<VariableUnit> getVariables() {
        return variables.values();
    }

    public IRTemp createTempVariable() {
        IRTemp result = new IRTemp(tempNum++);
        variables.put(result.toString(), result.getVariable());
        return result;
    }

    public IRLabel createLabel() {
        return new IRLabel(labelNum++);
    }

    public void output() {
        statements.forEach(System.out::println);

        variables.forEach((k, v) -> System.out.println(v));
    }

    public BasicCFGRegularBlock buildCFG(BasicCFGRegularBlock block) {
        for (IRAbstractStatement statement : statements) {
            block = statement.buildCFG(block);
        }
        return block;
    }
}
