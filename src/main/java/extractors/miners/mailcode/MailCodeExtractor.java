package extractors.miners.mailcode;

import extractors.parsers.mail.MailListKnowledgeExtractor;
import framework.KnowledgeExtractor;
import framework.KnowledgeGraphBuilder;
import framework.annotations.EntityDeclaration;
import framework.annotations.PropertyDeclaration;
import framework.annotations.RelationshipDeclaration;
import org.neo4j.graphdb.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.List;

public class MailCodeExtractor implements KnowledgeExtractor {

    @EntityDeclaration
    public static final String CODE_SNIPPET_IN_MAIL = "codeSnippetInMail";
    @PropertyDeclaration(parent = CODE_SNIPPET_IN_MAIL)
    public static final String CODE_SNIPPET_BODY = "body";

    @RelationshipDeclaration
    public static final String MAIL_CONTAIN_SNIPPET = "containSnippet";

    GraphDatabaseService db = null;

    public void run(GraphDatabaseService db) {
        this.db = db;

        try (Transaction tx = db.beginTx()) {
            db.findNodes(Label.label(MailListKnowledgeExtractor.MAIL)).forEachRemaining(mailNode -> {
                String mailBody = mailNode.getProperty(MailListKnowledgeExtractor.MAIL_BODY).toString();
                List<String> lines = MailBodyProcessor.bodyToLines(mailBody);
                List<Segment> segments = MailBodyProcessor.linesToSegments(lines);
                segments = MailBodyProcessor.filterCodes(segments);
                segments.stream().filter(Segment::isCode).forEach(s -> {
                    Node codeNode = db.createNode(Label.label(CODE_SNIPPET_IN_MAIL));
                    codeNode.setProperty(CODE_SNIPPET_BODY, s.getText());
                    mailNode.createRelationshipTo(codeNode, RelationshipType.withName(MAIL_CONTAIN_SNIPPET));
                });
            });
            tx.success();
        }
    }

    public static void main(String[] args) {
        run("resources/configs/config.xml");
    }

    public static void run(String configPath) {
        @SuppressWarnings("resource")
        ApplicationContext context = new FileSystemXmlApplicationContext(configPath);
        KnowledgeGraphBuilder graphBuilder = (KnowledgeGraphBuilder) context.getBean("graph");
        graphBuilder.buildGraph();
    }
}
