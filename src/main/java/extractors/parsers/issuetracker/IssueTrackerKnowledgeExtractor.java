package extractors.parsers.issuetracker;

import org.neo4j.graphdb.GraphDatabaseService;

import framework.KnowledgeExtractor;
import framework.annotations.EntityDeclaration;
import framework.annotations.PropertyDeclaration;
import framework.annotations.RelationshipDeclaration;

public class IssueTrackerKnowledgeExtractor implements KnowledgeExtractor {

    @EntityDeclaration
    public static final String ISSUE = "Issue";
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
    public static final String PATCH = "Patch";
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
    public static final String ISSUECOMMENT = "IssueComment";
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
    public static final String ISSUEUSER = "IssueUser";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_NAME = "name";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_EMAIL_ADDRESS = "emailAddress";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_DISPLAY_NAME = "displayName";
    @PropertyDeclaration(parent = ISSUEUSER)
    public static final String ISSUEUSER_ACTIVE = "active";

    @RelationshipDeclaration
    public static final String HAVE_PATCH = "have_patch";
    @RelationshipDeclaration
    public static final String HAVE_ISSUE_COMMENT = "have_issue_comment";
    @RelationshipDeclaration
    public static final String ISSUE_DUPLICATE = "issue_duplicate";
    @RelationshipDeclaration
    public static final String IS_ASSIGNEE_OF_ISSUE = "is_assignee_of_issue";
    @RelationshipDeclaration
    public static final String IS_CREATOR_OF_ISSUE = "is_creator_of_issue";
    @RelationshipDeclaration
    public static final String IS_REPORTER_OF_ISSUE = "is_reporter_of_issue";
    @RelationshipDeclaration
    public static final String IS_CREATOR_OF_ISSUECOMMENT = "is_creator_of_issueComment";
    @RelationshipDeclaration
    public static final String IS_UPDATER_OF_ISSUECOMMENT = "is_updater_of_issueComment";
    @RelationshipDeclaration
    public static final String IS_CREATOR_OF_PATCH = "is_creator_of_patch";

    private IssueTrackerParser parser = null;

    public void setParser(IssueTrackerParser parser) {
        this.parser = parser;
    }

    public void run(GraphDatabaseService graphDb) {
        parser.parse(graphDb);
    }

}
