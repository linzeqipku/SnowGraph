package extractors.parsers.bugzilla.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaohan on 2017/4/4.
 */
public class BugInfo {
    private String bug_id = "";
    private String creation_ts = ""; //The times of the bug's creation.
    private String short_desc = "";  //A short description of the bug.
    private String delta_ts = "";    //The timestamp of the last update.
    private String reporter_accessible = ""; //1 if the reporter can see this bug
    private String cclist_accessible = "";   //1 if people on the CC list can see this bug
    private String classification_id = "";
    private String classification = "";
    private String product = "";
    private String component = "";   //The product component
    private String version = "";     //The product version.
    private String rep_platform = "";//The platform on which the bug was reported.
    private String op_sys = "";      //The operating system on which the bug was observed.
    private String bug_status = "";  //The workflow status of the bug.
    private String resolution = "";  //The bug's resolution.
    private String bug_file_loc = "";//A URL which points to more information about the bug.
    private String status_whiteboard = "";//This seems to be just a small whiteboard field.
    private String keywords = "";    //A set of keywords.
    private String priority = "";    //The priority of the bug.
    private String bug_severity = "";     //The severity values of bugs.
    private String target_milestone = ""; //The milestone by which this bug should be resolved.
    private String everconfirmed = "";    //1 if this bug has ever been confirmed.
    private String reporter = "";         //The user who reported this.
    private String reporter_name = "";    //reporter's true name.
    private String assigned_to = "";      //The current owner of the bug.
    private String assigned_to_name = ""; //owner's true name.
    private String cc_list = "";     //several names separated by comma.
    private String votes = "";       //The number of votes.
    private String comment_sort_order = "";

    private List<BugCommentInfo> comment = new ArrayList<>();

    public String getBugId() {
        return bug_id;
    }
    public void setBugId(String bug_id) {
        this.bug_id = bug_id;
    }

    public String getCreationTs() {
        return creation_ts;
    }
    public void setCreationTs(String creation_ts) {
        this.creation_ts = creation_ts;
    }

    public String getShortDesc() {
        return short_desc;
    }
    public void setShortDesc(String short_desc) {
        this.short_desc = short_desc;
    }

    public String getDeltaTs() {
        return delta_ts;
    }
    public void setDeltaTs(String delta_ts) {
        this.delta_ts = delta_ts;
    }

    public String getClassification() {
        return classification;
    }
    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }

    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public String getRepPlatform() {
        return rep_platform;
    }
    public void setRepPlatform(String rep_platform) {
        this.rep_platform = rep_platform;
    }

    public String getOpSys() {
        return op_sys;
    }
    public void setOpSys(String op_sys) {
        this.op_sys = op_sys;
    }

    public String getBugStatus() {
        return bug_status;
    }
    public void setBugStatus(String bug_status) {
        this.bug_status = bug_status;
    }

    public String getResolution() {
        return resolution;
    }
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getBugSeverity() {
        return bug_severity;
    }
    public void setBugSeverity(String bug_severity) {
        this.bug_severity = bug_severity;
    }

    public String getReporter() {
        return reporter;
    }
    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getReporterName() {
        return reporter_name;
    }
    public void setReporterName(String reporter_name) {
        this.reporter_name = reporter_name;
    }

    public String getAssignedTo() {
        return assigned_to;
    }
    public void setAssignedTo(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    public String getAssignedToName() {
        return assigned_to_name;
    }
    public void setAssignedToName(String assigned_to_name) {
        this.assigned_to_name = assigned_to_name;
    }

    public List<BugCommentInfo> getComment() {
        return comment;
    }
    public void setComment(List<BugCommentInfo> comment) {
        this.comment = comment;
    }
    public void addComment(BugCommentInfo comment) { this.comment.add(comment); }

}
