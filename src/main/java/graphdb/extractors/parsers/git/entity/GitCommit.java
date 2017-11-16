package graphdb.extractors.parsers.git.entity;

import graphdb.extractors.parsers.git.GitExtractor;
import org.neo4j.graphdb.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by oliver on 2017/5/23.
 */
public class GitCommit {
    private String UUID = "";
    private String version = "";
    private GitCommitAuthor author;
    private String createDate = "";
    private String logMessage = "";
    private String parentUUID = "";
    private String commitSvnUrl = "";
    private List<MutatedFile> mutatedFiles = null;
    private List<MutatedContent> mutatedContents = null;
    private boolean status = false;


    //region <getter>
    public String getUUID(){
        return UUID;
    }

    public GitCommitAuthor getAuthor(){
        return author;
    }

    public String getCreateDate(){
        return createDate;
    }

    public String getLogMessage(){
        return logMessage;
    }

    public String getParentUUID(){
        return parentUUID;
    }

    public String getSvnUrl(){
        return commitSvnUrl;
    }

    public List<MutatedFile> getMutatedFiles(){
        return mutatedFiles;
    }

    public List<MutatedContent> getMutatedContents(){
        return mutatedContents;
    }

    public boolean getStatus(){
        return status;
    }

    //endregion <getter>

    public static void main(String[] args){

        GitCommit c = new GitCommit();
        //System.out.println(MutatedFile.MutatedType.ADDED.toString());
        //GitCommit commit = new GitCommit("I:\\lucene-solr\\commitffdfceba5371b1c3f96b44c727025f2f27bbf12b");
    }

    public GitCommit(){
        ;
    }

    public GitCommit(File commitFile){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(commitFile));
            reader.readLine(); // filter the first line of the file;

            UUID = reader.readLine().split(" ")[1]; // get the commit uuid

            //get the commit author
            String authorName = reader.readLine();
            author = new GitCommitAuthor(authorName);

            //get the commit create time
            createDate = reader.readLine();

            //get the commit log message
            logMessage = reader.readLine();

            String temp;
            //get commit svn url
            do{
                temp = reader.readLine();
                if(temp.indexOf("git-svn-id:") == 0){
                    temp = temp.replace("git-svn-id:" , "").trim();
                    commitSvnUrl = temp.split(" ")[0];
                    version = commitSvnUrl.substring(commitSvnUrl.lastIndexOf("@") + 1);
                }
            }while(temp.indexOf("----------------------------") != 0);

            parentUUID = reader.readLine();
            if(parentUUID.indexOf("Parents : ") == 0) {
                parentUUID = parentUUID.replace("Parents : ", "").trim();
            }
            addMutatedFile(reader);
            status = true;
            reader.close();
        }catch(Exception e){
            status = false;
            //System.out.print("parsing commit file meta info filed , commit file:" + commitFile.getAbsolutePath());
            //System.out.println(e.getMessage());
        }
    }

    private void addMutatedFile(MutatedFile file){
        if(mutatedFiles != null){
            mutatedFiles.add(file);
        }else{
            mutatedFiles = new ArrayList<MutatedFile>();
            mutatedFiles.add(file);
        }
    }

    private void addMutatedContent(MutatedContent content){
        if(mutatedContents != null){
            mutatedContents.add(content);
        }else{
            mutatedContents = new ArrayList<MutatedContent>();
            mutatedContents.add(content);
        }
    }

    private void addMutatedFile(BufferedReader reader){
        try{
            String line;
            while((line = reader.readLine())  != null){
                do{
                    // find a new mutated file info section
                    if(line.indexOf("diff") == 0){
                        break;
                    }
                }while((line = reader.readLine()) != null);
                if(line == null)
                    return ;
                MutatedFile file = new MutatedFile();

                /* the next file will contain the information about which file mode this file is
                DELETED MODE : "deleted file mode"
                ADDED MODE : "new file mode"
                MODEFIED : otherwise
                */
                line = reader.readLine();
                if(line.indexOf("new file mode") == 0){
                    file.setMutatedType(MutatedFile.MutatedType.ADDED);
                    file.setCreaterUUID(this.UUID); // set the creater of this file
                    line = reader.readLine(); // filter a line which will be like "index 0000000..63a8ab9"
                }else if(line.indexOf("deleted file mode") == 0){
                    file.setMutatedType(MutatedFile.MutatedType.DELETED);
                    file.setDeleterUUID(this.UUID);
                    line = reader.readLine(); // filter a line which will be likd "index 0000000..63a8ab9"
                }else if(line.indexOf("old mode") == 0 || line.indexOf("new mode") == 0){

                    file.setMutatedType(MutatedFile.MutatedType.MODECHANGED);

                    reader.readLine();// filter the another line "new mode" or "old mode"
                    line = reader.readLine(); // read next line , the line may start with "index" , which is what we want , if not we will
                }
                else  {
                    file.setMutatedType(MutatedFile.MutatedType.MODIFIED); // the line may start with "index" , which is what we want
                    //these is no need to filter line
                }
                if(line.indexOf("index") == 0) {
                    // get file name before mutated
                    String apiName = "";
                    line = reader.readLine();
                    if (line.indexOf("---") != 0) {
                        System.out.print("parsing commit file mutated file filed , commit file UUID:" + this.UUID);
                        break;
                    }
                    String formerName = line.split(" ")[1];
                    if (file.getMutatedType() != MutatedFile.MutatedType.ADDED) {
                        if (formerName.indexOf("a/") == 0) {
                            formerName = formerName.substring(2);
                        }
                        file.setFormerName(formerName);
                    }

                    // get file Name after mutated
                    line = reader.readLine();
                    if (line.indexOf("+++") != 0) {
                        //System.out.print("parsing commit file mutated file filed , commit file UUID:" + this.UUID);
                        break;
                    }
                    String latterName = line.split(" ")[1];
                    if (file.getMutatedType() != MutatedFile.MutatedType.DELETED) {
                        if (latterName.indexOf("b/") == 0) {
                            latterName = latterName.substring(2);
                        }
                        file.setLatterName(latterName);
                    }
                    file.setFileName(file.getFileSingleName());

                    apiName = file.getFileSingleName();
                    if(apiName.length() > 5) {
                        if (apiName.substring(apiName.length() - 5).compareTo(".java") == 0) {
                            apiName = apiName.substring(apiName.lastIndexOf('/') + 1); // remove the file path
                            apiName = apiName.substring(0, apiName.length() - 5);
                            file.setApiName(apiName);

                            String  apiQualifiedName = file.getAbsolutePath();
                            apiQualifiedName = apiQualifiedName.replace("/" , ".");
                            if(apiQualifiedName.indexOf(".org.") > -1) {
                                apiQualifiedName = apiQualifiedName.substring(apiQualifiedName.indexOf(".org.") + 1);
                                apiQualifiedName = apiQualifiedName.substring(0 , apiQualifiedName.length() - ".java".length());
                            }
                            else{
                                //System.out.println("there are some class of which the qualified name can not be found.");
                                //System.out.println("the absolute path is: " + file.getAbsolutePath() + " creater:"  + file.getCreaterUUID() +"   modifier:" + file.getModifierUUID() + "  deleter:" + file.getDeleterUUID());
                            }
                            file.setApiQualifiedName(apiQualifiedName);
                        }

                    }
                    addMutatedFile(file);
                }
                line = reader.readLine();
                Pattern pattern = Pattern.compile("@@ -([0-9]+),([0-9]+) +([0-9]+),([0-9]+) @@");
                Matcher matcher = pattern.matcher(line);

                if(matcher.find()){
                    MutatedContent content = new MutatedContent();

                    content.setType(file.getMutatedType());
                    content.setCommitUUID(this.UUID);
                    content.setFormerName(file.getFormerName());
                    content.setLatterName(file.getLatterName());

                    int formerStartLineNum = Integer.parseInt( matcher.group(1) );
                    int formerLines = Integer.parseInt( matcher.group(2) );
                    int latterStartLineNum = Integer.parseInt( matcher.group(3) );
                    int latterLines = Integer.parseInt( matcher.group(4));

                    content.setFormerStartLineNum(formerStartLineNum ) ;
                    content.setFormerLines( formerLines );
                    content.setLatterStartLineNum( latterStartLineNum );
                    content.setLatterLines( latterLines ) ;

                    String contentString = "";
                    while(formerLines > 0 || latterLines > 0){
                        line = reader.readLine();
                        if( line.startsWith( "+" ) ) latterLines -- ;
                        else if( line.startsWith( "-" ) ) formerLines --;
                        else{
                            formerLines --;
                            latterLines -- ;
                        }
                        contentString += (line + "\n");
                    }

                    content.setContent(contentString);
                    addMutatedContent(content);
                }



            }
        }catch(Exception e){
            //System.out.println("parsing commit file mutated file filed , commit file UUID:" + this.UUID);
        }
    }

    public void createCommitNode(Node node){
        node.addLabel(Label.label(GitExtractor.COMMIT));

        node.setProperty(GitExtractor.COMMIT_UUID , UUID);
        node.setProperty(GitExtractor.COMMIT_VERSION , version);
        node.setProperty(GitExtractor.COMMIT_DATE , createDate);
        node.setProperty(GitExtractor.COMMIT_LOGMESSAGE , logMessage);
        node.setProperty(GitExtractor.COMMIT_PARENT_UUID , parentUUID);
        //System.out.println("file:" + UUID + "    " + commitSvnUrl);
        node.setProperty(GitExtractor.COMMIT_SVN_URL , commitSvnUrl);
    }

    public static boolean createRelationshipTo(Node sourceNode , Node endNode , String relationName){
        boolean isCommitNode = false;

        Iterable<Label> labels = sourceNode.getLabels();
        if(labels != null){
            for(Label label : labels){
                if(label.name().compareTo(GitExtractor.COMMIT) == 0){
                    isCommitNode = true;
                    break;
                }
            }
        }
        if(!isCommitNode){
            return false;
        }

        boolean hasRelation = false;
        Iterable<Relationship> it = sourceNode.getRelationships(RelationshipType.withName(relationName));
        for(Relationship re : it){
            if(re.getEndNode().equals(endNode)){
                hasRelation = true;
                break;
            }
        }

        if(!hasRelation){
            sourceNode.createRelationshipTo(endNode , RelationshipType.withName(relationName));
            return true;
        }else{
            return false;
        }
    }

    //public void createRelationTo(NODE)
}
