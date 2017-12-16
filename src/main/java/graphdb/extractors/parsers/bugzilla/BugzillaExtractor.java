package graphdb.extractors.parsers.bugzilla;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.EntityDeclaration;
import graphdb.framework.annotations.PropertyDeclaration;
import graphdb.framework.annotations.RelationshipDeclaration;
import graphdb.extractors.parsers.bugzilla.entity.BugCommentInfo;
import graphdb.extractors.parsers.bugzilla.entity.BugInfo;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaohan on 2017/4/4.
 */
public class BugzillaExtractor implements Extractor {

    // 定义 bugzilla issue 实体
    @EntityDeclaration
    public static final String BUGZILLAISSUE = "bugzilla_issue";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_BUGID = "bug_id";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_CREATIONTS = "creation_ts";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_SHORTDESC = "short_desc";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_DELTATS = "delta_ts";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_CLASSIFICATION = "classification";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_PRODUCT = "product";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_COMPONENT = "component";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_VERSION = "version";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_REPPLATFORM = "rep_platform";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_OPSYS = "op_sys";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_BUGSTATUS = "bug_status";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_RESOLUTION = "resolution";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_PRIORITY = "priority";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_BUGSEVERITIY = "bug_severity";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_REPROTER = "reporter";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_REPROTERNAME = "reporter_name";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_ASSIGNEDTO = "assigned_to";
    @PropertyDeclaration(parent = BUGZILLAISSUE)
    public static final String ISSUE_ASSIGNEENAME = "assignee_name";

    // 定义 comment 实体
    @EntityDeclaration
    public static final String ISSUECOMMENT = "bugzilla_comment";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String COMMENT_ID = "commentid";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String COMMENT_WHO = "who";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String COMMENT_NAME = "name";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String COMMENT_BUGWHEN = "bug_when";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String COMMENT_THETEXT = "thetext";

    // 定义 user实体
    @EntityDeclaration
    public static final String BUGZILLAUSER = "bugzilla_user";
    @PropertyDeclaration(parent = BUGZILLAUSER)
    public static final String USER_ID = "user_id";
    @PropertyDeclaration(parent = BUGZILLAUSER)
    public static final String USER_NAME = "user_name";

    // 定义关联
    @RelationshipDeclaration
    private static final String HAVE_COMMENT = "bugzilla_have_comment";
    @RelationshipDeclaration
    private static final String IS_REPORTER_OF_ISSUE = "bugzilla_is_reporter_of_issue";
    @RelationshipDeclaration
    private static final String IS_ASSIGNEE_OF_ISSUE = "bugzilla_is_assignee_of_issue";
    @RelationshipDeclaration
    private static final String IS_CREATOR_OF_COMMENT = "bugzilla_is_creator_of_comment";

    private GraphDatabaseService db = null;

    // 文件路径
    private String folderPath = null;

    // 用户名 - 图节点
    private Map<String, Node> userNodeMap = new HashMap<>();

    @Override
    public void config(String[] args) {
        folderPath=args[0];
    }

    @Override
    public void run(GraphDatabaseService db) {
        this.db = db;
        File bugFolder = new File(folderPath);

        // 文件夹中每一个 .xml 文件为一份 bug 报告
        for (File oneBugFile : bugFolder.listFiles()){
            if (oneBugFile.getName().endsWith(".xml")) {

                // 调试语句
                // System.out.println(oneBugFile.getName());

                // 解析 .xml 文件，返回 BugInfo 对象
                // 由于xml解析库与neo4j库有冲突，解析与建图需要分开。
                BugInfo bugInfo = BugzillaParser.getBugInfo(folderPath + "/" + oneBugFile.getName());

                // 开始建图
                try (Transaction tx = db.beginTx()) {

                    // 为 BugInfo 对象创建一个 bugzilla issue 节点
                    Node node = db.createNode();
                    BugzillaUtils.creatBugzillaIssueNode(bugInfo, node);

                    // 建立 bugzilla issue 与 reporter 之间的关联
                    if (userNodeMap.containsKey(bugInfo.getReporter())) {
                        userNodeMap.get(bugInfo.getReporter()).createRelationshipTo(node, RelationshipType.withName(IS_REPORTER_OF_ISSUE));
                    } else {
                        Node userNode = db.createNode();
                        BugzillaUtils.creatBugzillaUserNode(bugInfo.getReporter(), bugInfo.getReporterName(), userNode);
                        userNodeMap.put(bugInfo.getReporter(), userNode);
                        userNode.createRelationshipTo(node, RelationshipType.withName(IS_REPORTER_OF_ISSUE));
                    }

                    // 建立 bugzilla issue 与 assignee 之间的关联
                    if (userNodeMap.containsKey(bugInfo.getAssignedTo())) {
                        userNodeMap.get(bugInfo.getAssignedTo()).createRelationshipTo(node, RelationshipType.withName(IS_ASSIGNEE_OF_ISSUE));
                    } else {
                        Node userNode = db.createNode();
                        BugzillaUtils.creatBugzillaUserNode(bugInfo.getAssignedTo(), bugInfo.getAssignedToName(), userNode);
                        userNodeMap.put(bugInfo.getAssignedTo(), userNode);
                        userNode.createRelationshipTo(node, RelationshipType.withName(IS_ASSIGNEE_OF_ISSUE));
                    }


                    // 遍历 bug 中每一个 comment
                    for (BugCommentInfo comment : bugInfo.getComment()) {

                        // 为 BugCommentInfo 对象创建一个 bugzilla comment 节点
                        Node commentNode = db.createNode();
                        BugzillaUtils.creatIssueCommentNode(comment, commentNode);
                        // 建立 bugzilla issue 与 comment 之间的关联
                        node.createRelationshipTo(commentNode, RelationshipType.withName(HAVE_COMMENT));

                        // 建立 bugzilla comment 与 commentator 之间的关联
                        if (userNodeMap.containsKey(comment.getWho())) {
                            userNodeMap.get(comment.getWho()).createRelationshipTo(commentNode, RelationshipType.withName(IS_CREATOR_OF_COMMENT));
                        } else {
                            Node userNode = db.createNode();
                            BugzillaUtils.creatBugzillaUserNode(comment.getWho(), comment.getWhoName(), userNode);
                            userNodeMap.put(comment.getWho(), userNode);
                            userNode.createRelationshipTo(commentNode, RelationshipType.withName(IS_CREATOR_OF_COMMENT));
                        }

                    }
                    tx.success();
                }
            }
        }
    }
}
