package graphdb.extractors.parsers.git;

import graphdb.extractors.parsers.git.GitExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2017/5/23.
 */
public class GitCommit {
    private String commitId = "";
    private String version = "";
    private String author;
    private String createDate = "";
    private String logMessage = "";
    private List<String> parents;
    private String content = "";

    public String getCommitId() {
        return commitId;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getContent() {
        return content;
    }

    public GitCommit(File commitFile) {
        if (commitFile.length()<=1024*1024)
            try {
                content=FileUtils.readFileToString(commitFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            content="";
        LineIterator lines = null;
        try {
            lines = FileUtils.lineIterator(commitFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lines.nextLine();
        commitId = lines.nextLine().split("\\s+")[1];
        author = lines.nextLine();
        createDate = lines.nextLine();
        logMessage = "";
        while (lines.hasNext()) {
            String line=lines.nextLine();
            if (line.startsWith("--------"))
                break;
            logMessage += line + "\r\n";
        }
        String parentLine = lines.nextLine();
        parents = new ArrayList<>();
        if (parentLine.startsWith("Parents :")) {
            parentLine = parentLine.substring("Parents : ".length()).trim();
            Matcher matcher = Pattern.compile("\\w+").matcher(parentLine);
            while (matcher.find())
                parents.add(matcher.group());
        }
    }

    public Node createNode(GraphDatabaseService db){
        Node node=db.createNode();
        node.addLabel(Label.label(GitExtractor.COMMIT));
        node.setProperty(GitExtractor.COMMIT_ID,commitId);
        node.setProperty(GitExtractor.COMMIT_DATE,createDate);
        node.setProperty(GitExtractor.COMMIT_LOGMESSAGE,logMessage);
        node.setProperty(GitExtractor.COMMIT_CONTENT,content);
        return node;
    }

}