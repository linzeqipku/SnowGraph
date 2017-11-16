package graphdb.extractors.parsers.git.entity;
import graphdb.extractors.parsers.git.GitExtractor;
import org.neo4j.graphdb.*;

/**
 * Created by oliver on 2017/11/16.
 */
public class MutatedContent {

    private MutatedFile.MutatedType type;
    private String commitUUID = "";
    private String formerName = "";
    private String latterName = "";
    private String content = "";
    private int formerStartLineNum = 0;
    private int formerLines= 0;
    private int latterStartLineNum = 0;
    private int latterLines = 0;


    //region <getter>
    public String getCommitUUID() {
        return commitUUID;
    }

    public String getFormerName() {
        return formerName;
    }

    public String getLatterName() {
        return latterName;
    }

    public String getFileName(){
        String result = "";
        if(type == MutatedFile.MutatedType.ADDED){
            result = latterName;
        }else if(type == MutatedFile.MutatedType.DELETED){
            result = formerName;
        }else if(formerName.compareTo(latterName) == 0){
            result = formerName;
        }else{
            result = "";
        }
        return result;
    }

    public String getContent() {
        return content;
    }

    public MutatedFile.MutatedType getType() {
        return type;
    }

    public int getFormerStartLineNum() {
        return formerStartLineNum;
    }

    public int getFormerLines() {
        return formerLines;
    }

    public int getLatterStartLineNum() {
        return latterStartLineNum;
    }

    public int getLatterLines() {
        return latterLines;
    }

    //endregion<getter>
    //region<setter>
    public void setCommitUUID(String commitUUID) {
        this.commitUUID = commitUUID;
    }

    public void setFormerName(String formerName) {
        this.formerName = formerName;
    }

    public void setLatterName(String latterName) {
        this.latterName = latterName;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setType(MutatedFile.MutatedType type) {
        this.type = type;
    }

    public void setFormerStartLineNum(int formerStartLineNum) {
        this.formerStartLineNum = formerStartLineNum;
    }

    public void setFormerLines(int formerLines) {
        this.formerLines = formerLines;
    }

    public void setLatterStartLineNum(int latterStartLineNum) {
        this.latterStartLineNum = latterStartLineNum;
    }

    public void setLatterLines(int latterLines) {
        this.latterLines = latterLines;
    }

    //endregion<setter>

    public MutatedContent(){
        ;
    }

    public void createMutatedFileNode(Node node){
        node.addLabel(Label.label(GitExtractor.MUTATEDCONTENT));

        node.setProperty(GitExtractor.MUTATEDCONTENT_COMMIT_UUID , commitUUID);
        node.setProperty(GitExtractor.MUTATEDCONTENT_TYPE , type.toString());
        node.setProperty(GitExtractor.MUTATEDCONTENT_FORMER_NAME , formerName);
        node.setProperty(GitExtractor.MUTATEDCONTENT_LATTER_NAME , latterName);
        node.setProperty(GitExtractor.MUTATEDCONTENT_CONTENT , content);
        node.setProperty(GitExtractor.MUTATEDCONTENT_FORMER_START_LINE_NUM , formerStartLineNum + "");
        node.setProperty(GitExtractor.MUTATEDCONTENT_FORMER_LINES  , formerLines + "");
        node.setProperty(GitExtractor.MUTATEDCONTENT_LATTER_START_LINE_NUM , latterStartLineNum + "");
        node.setProperty(GitExtractor.MUTATEDCONTENT_LATTER_LINES , latterLines);
    }

}
