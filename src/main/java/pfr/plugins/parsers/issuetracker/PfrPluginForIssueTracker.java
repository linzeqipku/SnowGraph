package pfr.plugins.parsers.issuetracker;

import org.neo4j.graphdb.GraphDatabaseService;
import pfr.PFR;
import pfr.annotations.ConceptDeclaration;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;

public class PfrPluginForIssueTracker implements PFR{
	
	@ConceptDeclaration public static final String ISSUE="Issue";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_ID = "id";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_NAME = "name";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_SUMMARY = "summary";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_TYPE = "type";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_STATUS = "status";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_PRIORITY = "priority";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_RESOLUTION = "resolution";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_VERSIONS = "versions";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_FIX_VERSIONS = "fixVersions";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_COMPONENTS = "components";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_LABELS = "labels";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_DESCRIPTION = "description";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_CREATOR_NAME = "crearorName";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_ASSIGNEE_NAME = "assigneeName";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_REPORTER_NAME = "reporterName";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_CREATED_DATE = "createdDate";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_UPDATED_DATE = "updatedDate";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_RESOLUTION_DATE = "resolutionDate";
	
	@ConceptDeclaration public static final String PATCH="Patch";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_ISSUE_ID = "issueId";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_ID = "id";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_NAME = "name";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_CONTENT = "content";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_CREATOR_NAME = "creatorName";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_CREATED_DATE = "createdDate";
	
	@ConceptDeclaration public static final String ISSUECOMMENT="IssueComment";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_ID = "id";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_BODY = "body";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_CREATOR_NAME = "creatorName";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_UPDATER_NAME = "updaterName";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_CREATED_DATE = "createdDate";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_UPDATED_DATE = "updatedDate";
	
	@ConceptDeclaration public static final String ISSUEUSER="IssueUser";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_NAME = "name";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_EMAIL_ADDRESS = "emailAddress";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_DISPLAY_NAME = "displayName";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_ACTIVE = "active";
	
	@RelationDeclaration public static final String HAVE_PATCH="have_patch";
	@RelationDeclaration public static final String HAVE_ISSUE_COMMENT="have_issue_comment";
	@RelationDeclaration public static final String ISSUE_DUPLICATE="issue_duplicate";
	@RelationDeclaration public static final String IS_ASSIGNEE_OF_ISSUE="is_assignee_of_issue";
	@RelationDeclaration public static final String IS_CREATOR_OF_ISSUE="is_creator_of_issue";
	@RelationDeclaration public static final String IS_REPORTER_OF_ISSUE="is_reporter_of_issue";
	@RelationDeclaration public static final String IS_CREATOR_OF_ISSUECOMMENT="is_creator_of_issueComment";
	@RelationDeclaration public static final String IS_UPDATER_OF_ISSUECOMMENT="is_updater_of_issueComment";
	@RelationDeclaration public static final String IS_CREATOR_OF_PATCH="is_creator_of_patch";
	
	private IssueTrackerExtractor extractor=null;
	
	public void setExtractor(IssueTrackerExtractor extractor){
		this.extractor=extractor;
	}

	public void run(GraphDatabaseService graphDb) {
		extractor.extract(graphDb);
	}

}
