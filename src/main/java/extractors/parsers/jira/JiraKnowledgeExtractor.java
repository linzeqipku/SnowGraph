package extractors.parsers.jira;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import framework.KnowledgeExtractor;
import framework.annotations.EntityDeclaration;
import framework.annotations.PropertyDeclaration;
import framework.annotations.RelationshipDeclaration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import extractors.parsers.jira.entity.IssueCommentInfo;
import extractors.parsers.jira.entity.IssueInfo;
import extractors.parsers.jira.entity.IssueUserInfo;
import extractors.parsers.jira.entity.PatchInfo;
import extractors.parsers.mail.utils.EmailAddressDecoder;

public class JiraKnowledgeExtractor implements KnowledgeExtractor {

    @EntityDeclaration
    public static final String ISSUE = "JiraIssue";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_ID = "id";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_NAME = "name";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_SUMMARY = "summary";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_TYPE = "type";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_STATUS = "status";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_PRIORITY = "priority";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_RESOLUTION = "resolution";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_VERSIONS = "versions";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_FIX_VERSIONS = "fixVersions";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_COMPONENTS = "components";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_LABELS = "labels";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_DESCRIPTION = "description";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_CREATOR_NAME = "crearorName";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_ASSIGNEE_NAME = "assigneeName";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_REPORTER_NAME = "reporterName";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_CREATED_DATE = "createdDate";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_UPDATED_DATE = "updatedDate";
    @PropertyDeclaration(parent = ISSUE)
    public static final String ISSUE_RESOLUTION_DATE = "resolutionDate";

    @EntityDeclaration
    public static final String PATCH = "JiraPatch";
    @PropertyDeclaration(parent = PATCH)
    public static final String PATCH_ISSUE_ID = "issueId";
    @PropertyDeclaration(parent = PATCH)
    public static final String PATCH_ID = "id";
    @PropertyDeclaration(parent = PATCH)
    public static final String PATCH_NAME = "name";
    @PropertyDeclaration(parent = PATCH)
    public static final String PATCH_CONTENT = "content";
    @PropertyDeclaration(parent = PATCH)
    public static final String PATCH_CREATOR_NAME = "creatorName";
    @PropertyDeclaration(parent = PATCH)
    public static final String PATCH_CREATED_DATE = "createdDate";

    @EntityDeclaration
    public static final String ISSUECOMMENT = "JiraIssueComment";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String ISSUECOMMENT_ID = "id";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String ISSUECOMMENT_BODY = "body";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String ISSUECOMMENT_CREATOR_NAME = "creatorName";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String ISSUECOMMENT_UPDATER_NAME = "updaterName";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String ISSUECOMMENT_CREATED_DATE = "createdDate";
    @PropertyDeclaration(parent = ISSUECOMMENT)
    public static final String ISSUECOMMENT_UPDATED_DATE = "updatedDate";

    @EntityDeclaration
    public static final String ISSUEUSER = "JiraIssueUser";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_NAME = "name";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_EMAIL_ADDRESS = "emailAddress";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_DISPLAY_NAME = "displayName";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_ACTIVE = "active";

    @RelationshipDeclaration
    public static final String HAVE_PATCH = "jira_have_patch";
    @RelationshipDeclaration
    public static final String HAVE_ISSUE_COMMENT = "jira_have_issue_comment";
    @RelationshipDeclaration
    public static final String ISSUE_DUPLICATE = "jira_issue_duplicate";
    @RelationshipDeclaration
    public static final String IS_ASSIGNEE_OF_ISSUE = "jira_is_assignee_of_issue";
    @RelationshipDeclaration
    public static final String IS_CREATOR_OF_ISSUE = "jira_is_creator_of_issue";
    @RelationshipDeclaration
    public static final String IS_REPORTER_OF_ISSUE = "jira_is_reporter_of_issue";
    @RelationshipDeclaration
    public static final String IS_CREATOR_OF_ISSUECOMMENT = "jira_is_creator_of_issueComment";
    @RelationshipDeclaration
    public static final String IS_UPDATER_OF_ISSUECOMMENT = "jira_is_updater_of_issueComment";
    @RelationshipDeclaration
    public static final String IS_CREATOR_OF_PATCH = "jira_is_creator_of_patch";

    GraphDatabaseService db = null;

    String issueFolderPath = null;

    private Map<String, Node> userNodeMap = new HashMap<>();
    private List<String> duplicateList = new ArrayList<>();// "a b"代表a指向b
    private Map<String, Node> issueNodeMap = new HashMap<String, Node>();
    private Map<String, Node> patchNodeMap = new HashMap<String, Node>();

    public void setIssueFolderPath(String path) {
        this.issueFolderPath = path;
    }

    @Override
    public void run(GraphDatabaseService db) {
        this.db = db;
        File issuesFolder = new File(issueFolderPath);

        for (File oneIssueFolder : issuesFolder.listFiles()) {
            for (File issueFileOrPatchesFolder : oneIssueFolder.listFiles()) {
                String fileName = issueFileOrPatchesFolder.getName();
                if (fileName.endsWith(".json")) {
                    try (Transaction tx = db.beginTx()) {
                        jsonHandler(issueFileOrPatchesFolder);
                        tx.success();
                    }
                }
            }
        }
        //System.out.println("json文件处理完毕.");

        for (File oneIssueFolder : issuesFolder.listFiles()) {
            for (File issueFileOrPatchesFolder : oneIssueFolder.listFiles()) {
                String fileName = issueFileOrPatchesFolder.getName();
                if (fileName.equals("patches")) {
                    for (File onePatchFolder : issueFileOrPatchesFolder.listFiles()) {
                        String patchId = onePatchFolder.getName();
                        for (File patchFile : onePatchFolder.listFiles()) {
                            if (patchNodeMap.containsKey(patchId))
                                try {
                                    try (Transaction tx = db.beginTx()) {
                                        patchNodeMap.get(patchId).setProperty(JiraKnowledgeExtractor.PATCH_CONTENT, FileUtils.readFileToString(patchFile));
                                        tx.success();
                                    }
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                        }
                    }
                }
            }
        }
        //System.out.println("patch文件处理完毕.");

        try (Transaction tx = db.beginTx()) {
            // 建立DUPLICATE关联
            for (String line : duplicateList) {
                String[] eles = line.trim().split("\\s+");
                String id1 = eles[0];
                String id2 = eles[1];
                if (issueNodeMap.containsKey(id1) && issueNodeMap.containsKey(id2))
                    issueNodeMap.get(id1).createRelationshipTo(issueNodeMap.get(id2), RelationshipType.withName(JiraKnowledgeExtractor.ISSUE_DUPLICATE));
            }
            tx.success();
        }
        //System.out.println("Duplicate关联处理完毕.");

    }

    private void jsonHandler(File issueFile) {
        String jsonContent = null;
        try {
            jsonContent = FileUtils.readFileToString(issueFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonContent == null) {
            return;
        }

        // 建立Issue实体
        IssueInfo issueInfo = getIssueInfo(jsonContent);
        Node node = db.createNode();
        issueNodeMap.put(issueInfo.getIssueId(), node);
        JiraUtils.createIssueNode(issueInfo, node);

        // 建立用户实体
        JSONObject fields = new JSONObject(jsonContent).getJSONObject("fields");
        Pair<String, Node> assignee = createUserNode(fields, "assignee");
        Pair<String, Node> creator = createUserNode(fields, "creator");
        Pair<String, Node> reporter = createUserNode(fields, "reporter");
        // 建立用户实体与Issue实体之间的关联
        if (assignee != null) {
            node.setProperty(JiraKnowledgeExtractor.ISSUE_ASSIGNEE_NAME, assignee.getLeft());
            assignee.getRight().createRelationshipTo(node, RelationshipType.withName(JiraKnowledgeExtractor.IS_ASSIGNEE_OF_ISSUE));
        }
        if (creator != null) {
            node.setProperty(JiraKnowledgeExtractor.ISSUE_CREATOR_NAME, creator.getLeft());
            creator.getRight().createRelationshipTo(node, RelationshipType.withName(JiraKnowledgeExtractor.IS_CREATOR_OF_ISSUE));
        }
        if (reporter != null) {
            node.setProperty(JiraKnowledgeExtractor.ISSUE_REPORTER_NAME, reporter.getLeft());
            reporter.getRight().createRelationshipTo(node, RelationshipType.withName(JiraKnowledgeExtractor.IS_REPORTER_OF_ISSUE));
        }

        // 记录DUPLICATE关系
        JSONArray jsonIssueLinks = fields.getJSONArray("issuelinks");
        int issueLinkNum = jsonIssueLinks.length();
        for (int i = 0; i < issueLinkNum; i++) {
            JSONObject jsonIssueLink = jsonIssueLinks.getJSONObject(i);
            if (jsonIssueLink.has("inwardIssue")) {
                String linkIssueId = jsonIssueLink.getJSONObject("inwardIssue").getString("id");
                duplicateList.add(linkIssueId + " " + issueInfo.getIssueId());
            }
        }

        // 建立评论实体并关联到ISSUE
        JSONArray jsonCommentArr;
        if (!fields.isNull("comment")) {
            jsonCommentArr = fields.getJSONObject("comment").optJSONArray("comments");
            if (jsonCommentArr != null) {
                int len = jsonCommentArr.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jsonComment = jsonCommentArr.getJSONObject(i);
                    String id = jsonComment.optString("id");
                    String body = jsonComment.optString("body");
                    Pair<String, Node> author = createUserNode(jsonComment, "author");
                    Pair<String, Node> updateAuthor = createUserNode(jsonComment, "updateAuthor");
                    String createdDate = jsonComment.optString("created");
                    String updatedDate = jsonComment.optString("updated");
                    if (author==null)
                        continue;
                    IssueCommentInfo comment = new IssueCommentInfo(id, body, author.getLeft(), updateAuthor.getLeft(), createdDate, updatedDate);
                    Node commentNode = db.createNode();
                    JiraUtils.createIssueCommentNode(comment, commentNode);
                    node.createRelationshipTo(commentNode, RelationshipType.withName(JiraKnowledgeExtractor.HAVE_ISSUE_COMMENT));
                    commentNode.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_CREATOR_NAME, author.getLeft());
                    author.getRight().createRelationshipTo(commentNode, RelationshipType.withName(JiraKnowledgeExtractor.IS_CREATOR_OF_ISSUECOMMENT));
                    if (updateAuthor != null) {
                        commentNode.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_UPDATER_NAME, updateAuthor.getLeft());
                        updateAuthor.getRight().createRelationshipTo(commentNode, RelationshipType.withName(JiraKnowledgeExtractor.IS_UPDATER_OF_ISSUECOMMENT));
                    }
                }
            }
        }

        // 建立补丁实体并关联到ISSUE
        JSONArray jsonHistoryArr;
        JSONObject root = new JSONObject(jsonContent);
        if (!root.isNull("changelog")) {
            jsonHistoryArr = root.getJSONObject("changelog").optJSONArray("histories");
            if (jsonHistoryArr != null) {
                int hisNum = jsonHistoryArr.length();
                for (int i = 0; i < hisNum; i++) {
                    JSONObject history = jsonHistoryArr.getJSONObject(i);
                    JSONArray items = history.optJSONArray("items");
                    if (items == null)
                        continue;
                    int itemNum = items.length();
                    for (int j = 0; j < itemNum; j++) {
                        JSONObject item = items.getJSONObject(j);
                        String to = item.optString("to");
                        String toString = item.optString("toString");
                        // not a patch
                        if (!to.matches("^\\d{1,19}$") || !toString.endsWith(".patch")) {
                            continue;
                        }
                        String patchName;
                        patchName = toString;
                        Pair<String, Node> author = createUserNode(history, "author");
                        String createdDate = history.optString("created");
                        if (createdDate == null)
                            createdDate = "";

                        PatchInfo patchInfo = new PatchInfo(to, patchName, "", createdDate);
                        Node patchNode = db.createNode();
                        patchNodeMap.put(to, patchNode);
                        JiraUtils.createPatchNode(patchInfo, patchNode);
                        node.createRelationshipTo(patchNode, RelationshipType.withName(JiraKnowledgeExtractor.HAVE_PATCH));
                        if (author != null) {
                            patchNode.setProperty(JiraKnowledgeExtractor.PATCH_CREATOR_NAME, author.getLeft());
                            author.getRight().createRelationshipTo(patchNode, RelationshipType.withName(JiraKnowledgeExtractor.IS_CREATOR_OF_PATCH));
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析.json文件，返回IssueInfo。 返回的IssueInfo中不包含crearorName, assigneeName,
     * reporterName
     */
    private IssueInfo getIssueInfo(String jsonContent) {

        IssueInfo issueInfo = new IssueInfo();

        JSONObject root = new JSONObject(jsonContent);
        String issueId = root.getString("id");
        String issueName = root.getString("key");

        JSONObject fields = root.getJSONObject("fields");

        String type = "";
        if (!fields.isNull("issuetype")) {
            type = fields.getJSONObject("issuetype").optString("name");
        }

        String fixVersions = getVersions(fields, "fixVersions");
        String versions = getVersions(fields, "versions");
        String resolution = "";
        if (!fields.isNull("resolution")) {
            resolution = fields.getJSONObject("resolution").optString("name");
        }

        String priority = "";
        if (!fields.isNull("priority")) {
            priority = fields.getJSONObject("priority").optString("name");
        }

        String status = "";
        if (!fields.isNull("status")) {
            status = fields.getJSONObject("status").optString("name");
        }

        String description = fields.optString("description");
        String summary = fields.optString("summary");

        String resolutionDate = fields.optString("resolutiondate");
        String createDate = fields.optString("created");
        String updateDate = fields.optString("updated");

        // labels
        String labels = "";
        JSONArray jsonLabels = fields.optJSONArray("labels");
        if (jsonLabels != null) {
            int len = jsonLabels.length();
            for (int i = 0; i < len; i++) {
                String label = jsonLabels.optString(i);
                labels += label;
                if (i != len - 1) {
                    labels += ",";
                }
            }
        }

        // components
        String components = "";
        JSONArray jsonComponents = fields.optJSONArray("components");
        if (jsonComponents != null) {
            int len = jsonComponents.length();
            for (int i = 0; i < len; i++) {
                String component = jsonComponents.getJSONObject(i).optString("name");
                components += component;
                if (i != len - 1) {
                    components += ",";
                }
            }
        }

        issueInfo.setIssueId(issueId);
        issueInfo.setIssueName(issueName);
        issueInfo.setType(type);
        issueInfo.setFixVersions(fixVersions);
        issueInfo.setResolution(resolution);
        issueInfo.setResolutionDate(resolutionDate);
        issueInfo.setPriority(priority);
        issueInfo.setLabels(labels);
        issueInfo.setVersions(versions);
        issueInfo.setStatus(status);
        issueInfo.setComponents(components);
        issueInfo.setDescription(description);
        issueInfo.setSummary(summary);
        issueInfo.setCreatedDate(createDate);
        issueInfo.setUpdatedDate(updateDate);
        return issueInfo;
    }

    private String getVersions(JSONObject jsonObj, String key) {
        String versions = "";
        JSONArray jsonVersions = jsonObj.optJSONArray(key);
        if (jsonVersions == null) {
            return versions;
        }

        int versionNum = jsonVersions.length();
        for (int i = 0; i < versionNum; i++) {
            JSONObject fixVersion = jsonVersions.getJSONObject(i);
            String version = fixVersion.optString("name");
            versions += version;

            if (i != versionNum - 1) {
                versions += ",";
            }
        }
        return versions;
    }

    private Pair<String, Node> createUserNode(JSONObject jsonObj, String key) {
        if (jsonObj.isNull(key)) {
            return null;
        }

        JSONObject userJsonObj = jsonObj.getJSONObject(key);
        String name = userJsonObj.optString("name");
        String emailAddress = userJsonObj.optString("emailAddress");
        String displayName = userJsonObj.optString("displayName");
        boolean active = userJsonObj.optBoolean("active");

        IssueUserInfo user = new IssueUserInfo(name, EmailAddressDecoder.decode(emailAddress), displayName, active);
        if (userNodeMap.containsKey(name))
            return new ImmutablePair<String, Node>(name, userNodeMap.get(name));
        Node node = db.createNode();
        JiraUtils.createIssueUserNode(user, node);
        userNodeMap.put(name, node);
        return new ImmutablePair<String, Node>(name, userNodeMap.get(name));
    }

}
