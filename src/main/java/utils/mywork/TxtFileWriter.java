package utils.mywork;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import graphdb.framework.Extractor;
import org.neo4j.graphdb.*;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by laurence on 17-6-5.
 */
public class TxtFileWriter implements Extractor {
    GraphDatabaseService db;
    int classCount = 0;
    int mailCount = 0;
    int stackCount = 0;
    @Override
    public void run(GraphDatabaseService graphDB) {
        this.db = graphDB;
        getNode();
    }
    public void writeFile(String fileName, String text){
        try{
            DataOutputStream oStream = new DataOutputStream(new FileOutputStream(fileName));
            oStream.writeUTF(text);
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    public void getNode() {
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while (nodeIter.hasNext()) {
                Node node = nodeIter.next();
                if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))) {
                    classCount ++;
                    String text = "";
                    Map<String, Object> map = node.getAllProperties();
                    for (String key : map.keySet()){
                        String value = map.get(key).toString();
                        text += key + ' ' + value + '\n';
                    }
                    if (classCount == 1) {
                        //writeFile("code/" + node.getId(), text);
                        System.out.println(text);
                    }
                }
                else if (node.hasLabel(Label.label(MailListExtractor.MAIL))){
                    mailCount++;
                }
                else if (node.hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
                    stackCount++;
                }
            }
            tx.success();
        }
        System.out.println("class count " + classCount);
        System.out.println("mail count " + mailCount);
        System.out.println("stack count " + stackCount);

    }
}
