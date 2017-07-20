package graphdb.extractors.parsers.word.corpus;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.word.utils.ApiJudge;
import graphdb.extractors.parsers.word.utils.Config;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by maxkibble on 2017/7/9.
 */
public class ApiTokenizer {
    private static String graphPath = Config.getProjectGraphPath();
    private static HashSet<String> tokens = new HashSet<>();

    public static void main(String[] args) throws IOException {
        File graphFile = new File(graphPath);
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(graphFile);
        String name;
        ArrayList<String> nameTokens;
        int apiNum = 0;
        try (Transaction tx = db.beginTx()) {
            for (Node node : db.getAllNodes()) {
                if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS)))
                    name = (String) node.getProperty(JavaCodeExtractor.CLASS_NAME);
                else if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
                    name = (String) node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
                else if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
                    name = (String) node.getProperty(JavaCodeExtractor.METHOD_NAME);
                else continue;
                apiNum++;
                nameTokens = ApiJudge.splitCamelCase(name);
                for (String token : nameTokens) tokens.add(token);
            }
            tx.success();
        }
        db.shutdown();

        System.out.println("API NUM: " + apiNum);
        System.out.println("TOKEN NUM: " + tokens.size());
        StringBuilder toPrint = new StringBuilder();
        FileOutputStream fout = new FileOutputStream(new File(Config.getProjectApiTokenPath()));
        for(String token : tokens) {
            toPrint.append(token + "\n");
        }
        fout.write(toPrint.toString().getBytes());
    }
}
