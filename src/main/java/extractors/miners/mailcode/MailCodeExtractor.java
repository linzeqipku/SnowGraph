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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
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
            long total = db.findNodes(Label.label(MailListKnowledgeExtractor.MAIL)).stream().count();
            long count = 0;
            ResourceIterator<Node> ite =  db.findNodes(Label.label(MailListKnowledgeExtractor.MAIL));
            while (ite.hasNext()) {
                ++count;
                System.out.println(String.format("%s/%s", count, total));

                long beginTime = System.currentTimeMillis();
                Node mailNode = ite.next();
                if (isCommitMail(mailNode)) continue;
                String mailBody = mailNode.getProperty(MailListKnowledgeExtractor.MAIL_BODY).toString();
                if (mailBody.startsWith("Dear Wiki user,")) continue;
                if (mailBody.startsWith("<")) continue;

                List<String> lines = MailBodyProcessor.bodyToLines(mailBody);
                List<Segment> segments = MailBodyProcessor.linesToSegments(lines);
                MailBodyProcessor.filterSignature(segments);
                MailBodyProcessor.filterReference(segments);
                segments = MailBodyProcessor.getCodes(segments);
                segments.stream().filter(Segment::isCode).forEach(s -> {
                    Node codeNode = db.createNode(Label.label(CODE_SNIPPET_IN_MAIL));
                    codeNode.setProperty(CODE_SNIPPET_BODY, s.getText());
                    mailNode.createRelationshipTo(codeNode, RelationshipType.withName(MAIL_CONTAIN_SNIPPET));
                });
                long endTime = System.currentTimeMillis();
                System.out.println(String.format("Uses %d ms.", endTime - beginTime));
                if (endTime - beginTime > 30000) {
                    File f = new File("out/" + mailNode.getProperty(MailListKnowledgeExtractor.MAIL_ID).toString());
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

    private boolean isCommitMail(Node mailNode) {
        return mailNode.getProperty(MailListKnowledgeExtractor.MAIL_SUBJECT).toString().startsWith("svn commit:");
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
