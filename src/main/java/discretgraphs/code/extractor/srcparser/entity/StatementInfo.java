package discretgraphs.code.extractor.srcparser.entity;

import java.util.List;
import java.util.UUID;

public class StatementInfo extends CommonInfo{
	private String uuid;
	private String statementString;
	private int count;
	private String comment;
	private List<CommentInfo> commentInfoList;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getStatementString() {
		return statementString;
	}
	public void setStatementString(String statementString) {
		this.statementString = statementString;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public StatementInfo() {
		uuid = UUID.randomUUID().toString();
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<CommentInfo> getCommentInfoList() {
		return commentInfoList;
	}
	public void setCommentInfoList(List<CommentInfo> commentInfoList) {
		this.commentInfoList = commentInfoList;
	}
	@Override
	public String getHashName() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
