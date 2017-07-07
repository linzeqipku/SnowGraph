package extractors.utils.mywork;

import extractors.parsers.javacode.JavaCodeKnowledgeExtractor;
import extractors.parsers.stackoverflow.StackOverflowKnowledgeExtractor;
import org.neo4j.graphdb.*;
import framework.KnowledgeExtractor;

import java.io.*;
import java.util.*;

/**
 * Created by laurence on 17-4-22.
 */

public class CommentUtil implements KnowledgeExtractor {
    GraphDatabaseService db = null;
    HashMap<String, String> classComment;
    HashMap<String, String> classFuncMap;
    HashMap<String, List<String>> stackCandidate;
    @Override
    public void run(GraphDatabaseService graphDB) {
        this.db = graphDB;
        classComment = new HashMap<>();
        classFuncMap = new HashMap<>();
        stackCandidate = new HashMap<>();
        //getComment();
        getStackSents();
    }

    public void writeFile(){
        try {
            ObjectOutputStream oStream = new ObjectOutputStream(new FileOutputStream(
                    "/home/laurence/Documents/java/wordfilter/src/main/resources/class_comment_map"));
            oStream.writeObject(classComment);
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile(){
        try{
            ObjectInputStream iStream = new ObjectInputStream(new FileInputStream(
                    "/home/laurence/Documents/java/wordfilter/src/main/resources/class_comment_map"));
            classFuncMap = (HashMap<String, String>)iStream.readObject();
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void getComment() {
        try (Transaction tx = db.beginTx()) {
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while (nodeIter.hasNext()) {
                Node node = nodeIter.next();
                if (node.hasLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS))) {
                    String className = (String)node.getProperty(JavaCodeKnowledgeExtractor.CLASS_NAME);
                    String text = (String) node.getProperty(JavaCodeKnowledgeExtractor.CLASS_COMMENT);
                    text = text.replaceAll("\\n|\\*", "");
                    text = text.replaceAll("<pre.*?>.+?</pre>", "");
                    if (className.equals("QueryBuilder"))
                        System.out.println(text);
                    text = text.replaceAll("/|<.+?>|(&lt;.+?&gt;)|@code |@link |\\{|}", "");
                    text = text.replaceAll("#|@lucene\\..*? ", " ").trim();
                    text = text.replaceAll(" +", " ");
                    if (text.length() > 0){
                        classComment.put(className, text);
                        if (className.length() > 20){
                            System.out.println(className + " " + text);
                        }
                    }
                }
            }
            System.out.println("total comment " + classComment.size());
            tx.success();
        }
        writeFile();
    }

    public String stackFileter(String text){
        text = text.replaceAll("\\n", "");
        text = text.replaceAll("<pre.*?>.+?</pre>|<a.*?>.+?</a>", "");
        text = text.replaceAll("<.+?>", "");
        text = text.replaceAll(" +", " ");
        return text.trim();
    }

    public void getStackSents(){
        readFile();
        try(Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodeIter = db.getAllNodes().iterator();
            while(nodeIter.hasNext()) {
                Node node = nodeIter.next();
                String text = "";
                if (node.hasLabel(Label.label(StackOverflowKnowledgeExtractor.QUESTION))){
                    text = (String)node.getProperty(StackOverflowKnowledgeExtractor.QUESTION_TITLE);
                    text += " " + node.getProperty(StackOverflowKnowledgeExtractor.QUESTION_BODY);
                }
                if (node.hasLabel(Label.label(StackOverflowKnowledgeExtractor.ANSWER))){
                    text = (String)node.getProperty(StackOverflowKnowledgeExtractor.ANSWER_BODY);
                }
                if (text.length() > 0){
                    text = stackFileter(text);
                    for (String name : classFuncMap.keySet()){
                        if (text.contains(name)) {
                            System.out.println(name + " " + text);
                            List<String> curList = new ArrayList<>(2);
                            curList.add(text);
                            List<String> oldList = stackCandidate.put(name, curList);
                            if (oldList != null){
                                curList.addAll(oldList);
                                //System.out.println(curList.toString());
                            }
                        }
                    }
                }

            }
            tx.success();
        }
        System.out.println(stackCandidate.size());
    }
}