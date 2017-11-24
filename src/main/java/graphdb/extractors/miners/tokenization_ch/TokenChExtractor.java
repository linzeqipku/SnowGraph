package graphdb.extractors.miners.tokenization_ch;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.word.utils.ApiJudge;
import graphdb.framework.Extractor;
import graphdb.framework.annotations.PropertyDeclaration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;

public class TokenChExtractor implements Extractor {
    @PropertyDeclaration
    private static final String TOKENS_CH = "tokensCh";

    private GraphDatabaseService db = null;
    private static Dictionary dictionary = new Dictionary();

    private ArrayList<String> setChineseTokens(String comment, String name) {
        ArrayList<String> ret;
        ret = ApiJudge.commentParser(comment);
        if (ret.isEmpty()) {
            ArrayList<String> tokens = ApiJudge.splitCamelCase(name);
            for (String token : tokens) {
                ArrayList<String> trans = dictionary.getTranslation(token);
                ret.addAll(trans);
            }
        }
        return ret;
    }

    @Override
    public void run(GraphDatabaseService db) {
        this.db = db;
        try(Transaction tx = db.beginTx()) {
            for(Node node : db.getAllNodes()) {
                if(node.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
                    String comment = (String)node.getProperty(JavaCodeExtractor.CLASS_COMMENT);
                    String name = (String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
                    node.setProperty(TokenChExtractor.TOKENS_CH, String.join(" ", setChineseTokens(comment, name)));
                }
                else if(node.hasLabel(Label.label(JavaCodeExtractor.METHOD))) {
                    String comment = (String)node.getProperty(JavaCodeExtractor.METHOD_COMMENT);
                    String name = (String)node.getProperty(JavaCodeExtractor.METHOD_NAME);
                    node.setProperty(TokenChExtractor.TOKENS_CH, String.join(" ", setChineseTokens(comment, name)));
                }
                else if(node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))) {
                    String comment = (String)node.getProperty(JavaCodeExtractor.INTERFACE_COMMENT);
                    String name = (String)node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                    node.setProperty(TokenChExtractor.TOKENS_CH, String.join("", setChineseTokens(comment, name)));
                }
            }
            tx.success();
        }
        dictionary.printQuery();
    }
}
