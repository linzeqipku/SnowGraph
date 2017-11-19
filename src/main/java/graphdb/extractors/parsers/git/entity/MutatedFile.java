package graphdb.extractors.parsers.git.entity;

import graphdb.extractors.parsers.git.GitExtractor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by oliver on 2017/5/23.
 * when developer commit for a project , their commit content will include some file has been changed
 * so , I call one of these  changed file as a mutated file.
 */
public class MutatedFile {
    public static enum MutatedType{
        ADDED,
        DELETED,
        MODIFIED,
        MODECHANGED
    }
    private String apiName = "";
    private String apiQualifiedName = "";
    private String formerName = "";
    private String latterName = "";
    private String fileName = "";
    private MutatedType type ;
    private String createrUUID = "";
    private String deleterUUID = "";
    private String modifierUUID = "";

    public void setApiName(String apiName){
        this.apiName = apiName;
    }
    public String getApiName(){
        return apiName;
    }

    public void setApiQualifiedName(String apiQualifiedName){
        this.apiQualifiedName = apiQualifiedName;
    }
    public String getApiQualifiedName(){
        return apiQualifiedName;
    }

    public void setFormerName (String formerName){
        this.formerName = formerName;
    }
    public String getFormerName(){
        return this.formerName;
    }

    public void setLatterName(String latterName){
        this.latterName = latterName;
    }
    public String getLatterName(){
        return this.latterName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    public String getFileName(){
        return fileName;
    }

    /**
     * get the file name only with the file name , without path information
     * for example , if the file absolute path is a/b/c.exe , only c.exe will be returned.
     * @return
     */
    public String getFileSingleName(){
        String absolutePath = getAbsolutePath();
        String result = absolutePath.substring(absolutePath.lastIndexOf("/") + 1);
        return result;
    }

    public String getAbsolutePath(){
        String result = "";
        if(type == MutatedType.ADDED){
            result = latterName;
        }else if(type == MutatedType.DELETED){
            result = formerName;
        }else if(formerName.compareTo(latterName) == 0){
            result = formerName;
        }else{
            result = "";
        }
        return result;
    }

    public void setMutatedType(MutatedType type){
        this.type = type;
    }
    public MutatedType getMutatedType(){
        return this.type;
    }

    public void setCreaterUUID(String UUID){
        this.createrUUID = UUID;
    }
    public String getCreaterUUID(){
        return this.createrUUID;
    }

    public void setModifierUUID(String UUID){
        this.modifierUUID = UUID;
    }
    public String getModifierUUID(){
        return this.modifierUUID;
    }

    public void setDeleterUUID(String UUID){
        this.deleterUUID = UUID;
    }
    public String getDeleterUUID(){
        return this.deleterUUID;
    }

    public void createMutatedFileNode(Node node){
        node.addLabel(Label.label(GitExtractor.MUTATEDFILE));
        node.setProperty(GitExtractor.MUTATEDFILE_API_NAME , apiName);
        node.setProperty(GitExtractor.MUTATEDFILE_API_QUALIFIEDNAME , apiQualifiedName);
        node.setProperty(GitExtractor.MUTATEDFILE_TYPE , type.toString());
        node.setProperty(GitExtractor.MUTATEDFILE_FILE_NAME , fileName);
        node.setProperty(GitExtractor.MUTATEDFILE_FORMER_NAME , formerName);
        node.setProperty(GitExtractor.MUTATEDFILE_LATTER_NAME , latterName);
        //node.setProperty(GitExtractor.  );
    }

    public static boolean createrRelationshipTo(Node fileNode , Node endNode , String relationName){
        boolean isFileNode = false;
        boolean isCommitNode = false;
        Iterable<Label> labels = fileNode.getLabels();
        if(labels != null) {
            for (Label label : labels) {
                if (label.name().compareTo(GitExtractor.MUTATEDFILE) == 0) {
                    isFileNode = true;
                    break;
                }
            }
        }
        if(!isFileNode )
            return false;

        boolean hasRelation =false;
        Iterable<Relationship> it = fileNode.getRelationships(RelationshipType.withName(relationName));
        for(Relationship re : it) {
            if ( re.getEndNode().equals(endNode) ){
                hasRelation = true;
                break;
            }
        }

        if(!hasRelation) {
            fileNode.createRelationshipTo(endNode , RelationshipType.withName(relationName));
            return true;
        }else{
            return false;
        }
    }
}

