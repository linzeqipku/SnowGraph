package graphdb.extractors.parsers.bugzilla.entity;

/**
 * Created by xiaohan on 2017/4/4.
 */
public class BugCommentInfo {
    private String commentid = "";
    private String comment_count = "";
    private String who = "";
    private String who_name = "";
    private String bug_when = "";
    private String thetext = "";

    public String getCommentId() {
        return commentid;
    }
    public void setCommentId(String commentid) {
        this.commentid = commentid;
    }

    public String getWho() {
        return who;
    }
    public void setWho(String who) {
        this.who = who;
    }

    public String getWhoName() {
        return who_name;
    }
    public void setWhoName(String who_name) {
        this.who_name = who_name;
    }

    public String getBugWhen() {
        return bug_when;
    }
    public void setBugWhen(String bug_when) {
        this.bug_when = bug_when;
    }

    public String getThetext() {
        return thetext;
    }
    public void setThetext(String thetext) {
        this.thetext = thetext;
    }
}
