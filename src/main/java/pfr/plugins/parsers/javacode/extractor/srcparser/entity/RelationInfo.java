package pfr.plugins.parsers.javacode.extractor.srcparser.entity;

public class RelationInfo {
	public static final int CLASSTYPE = 0;
	public static final int INTERFACETYPE = 1;
	public static final int METHODTYPE = 2;
	public static final int COMMENTTYPE = 3;
	public static final int FIELDTYPE = 4;
	public static final int STATEMENTTYPE = 5;
	public static final int PACKAGETYPE = 6;
	
	private int sourceType;
	private String sourceUuid;
	private int targetType;
	private String targetUuid;
	private String relationName;
	private String projectUuid;
	
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public String getSourceUuid() {
		return sourceUuid;
	}
	public void setSourceUuid(String sourceUuid) {
		this.sourceUuid = sourceUuid;
	}
	public int getTargetType() {
		return targetType;
	}
	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}
	public String getTargetUuid() {
		return targetUuid;
	}
	public void setTargetUuid(String targetUuid) {
		this.targetUuid = targetUuid;
	}
	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}
	public String getRelationName() {
		return relationName;
	}
	public String getProjectUuid() {
		return projectUuid;
	}
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
	
	
}
