package discretgraphs.code.extractor.srcparser.entity;

import java.util.UUID;

import org.eclipse.jdt.core.dom.Comment;

public class CommentInfo extends CommonInfo implements Comparable<CommentInfo>{

	private Comment comment;
	private int startLineNum;
	private int endLineNum;
	private String commentString;
	private String uuid;
	private int startPosition;
	
	public CommentInfo() {
		uuid = UUID.randomUUID().toString();
	}
	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public int getStartLineNum() {
		return startLineNum;
	}

	public void setStartLineNum(int startLineNum) {
		this.startLineNum = startLineNum;
	}

	public int getEndLineNum() {
		return endLineNum;
	}

	public void setEndLineNum(int endLineNum) {
		this.endLineNum = endLineNum;
	}

	public String getCommentString() {
		return commentString;
	}

	public void setCommentString(String commentString) {
		this.commentString = commentString;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	@Override
	public String getHashName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 @Override
	public int compareTo(CommentInfo arg0) { 
	    return this.getStartLineNum() - arg0.getStartLineNum(); 
	 } 

	
	
}
