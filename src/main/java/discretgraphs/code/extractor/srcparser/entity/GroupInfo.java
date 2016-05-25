package discretgraphs.code.extractor.srcparser.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-24 下午01:38:53
 * @version 0.1 2012-12-15
 * GroupInfo对象存储一个java文件的信息
 */

public class GroupInfo extends CommonInfo{
	/**
	 * 文件名
	 */
	private String name;
	
	/**
	 * 文件存储路径
	 */
	private String path;
	
	/**
	 * 文件中的代码行数
	 */
	private int normalLines;
	
	/**
	 * 文件中的注释行数，这里的注释包括3种类型的注释
	 */
	private int commentLines;
	
	/**
	 * 文件中的空行
	 */
	private int whiteLines;
	
	/**
	 * 文件所属的包
	 */
	private PackageInfo packageInfo;
	
	/**
	 * 文件开头的import信息
	 */
	private List<String> importList;
	
	/**
	 * 文件所包含的类
	 */
	private List<ClassInfo> classInfoList;
	
	/**
	 * 文件所包含的接口
	 */
	private List<InterfaceInfo> interfaceInfoList;
	private List<CommentInfo> commentInfoList;
	/**
	 * 文件注释
	 */
	private String comment;
	
	private String uuid;

	private ArrayList<Integer> lineNumber2Position;
	public GroupInfo() {
		importList = new ArrayList<String>();
		classInfoList = new ArrayList<ClassInfo>();
		interfaceInfoList = new ArrayList<InterfaceInfo>();
		uuid = UUID.randomUUID().toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the normalLines
	 */
	public int getNormalLines() {
		return normalLines;
	}

	/**
	 * @param normalLines the normalLines to set
	 */
	public void setNormalLines(int normalLines) {
		this.normalLines = normalLines;
	}

	/**
	 * @return the commentLines
	 */
	public int getCommentLines() {
		return commentLines;
	}

	/**
	 * @param commentLines the commentLines to set
	 */
	public void setCommentLines(int commentLines) {
		this.commentLines = commentLines;
	}

	/**
	 * @return the whiteLines
	 */
	public int getWhiteLines() {
		return whiteLines;
	}

	/**
	 * @param whiteLines the whiteLines to set
	 */
	public void setWhiteLines(int whiteLines) {
		this.whiteLines = whiteLines;
	}

	public void setPackageInfo(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}

	public PackageInfo getPackageInfo() {
		return packageInfo;
	}

	public void setImportList(List<String> importList) {
		this.importList = importList;
	}

	public List<String> getImportList() {
		return importList;
	}

	public void setClassInfoList(List<ClassInfo> classInfoList) {
		this.classInfoList = classInfoList;
	}

	public List<ClassInfo> getClassInfoList() {
		return classInfoList;
	}

	public void setInterfaceInfoList(List<InterfaceInfo> interfaceInfoList) {
		this.interfaceInfoList = interfaceInfoList;
	}

	public List<InterfaceInfo> getInterfaceInfoList() {
		return interfaceInfoList;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getHashName() {
		if(packageInfo != null)
		{
			return packageInfo.getHashName() + "|#|" + name + "@" + path;
		}
		else {
			return "|#||#|" + name + "@" + path;
		}
	}
	
	public boolean equals(Object o) {
		if(this.getHashName().equals(((GroupInfo) o).getHashName())) {
			return true;
		}
		else {
			return false;
		}
	}
	public int getLineNumber(int position) {
		
		int left = 0, right = lineNumber2Position.size() - 1;
	//	System.out.println("start at " + position);
		int mid = (left + right) / 2;
		
	//	System.out.println(lineNumber2Position.get(0) + "  " + lineNumber2Position.get(1) + " " + lineNumber2Position.get(2));
		while (mid != lineNumber2Position.size() - 1 && (!(position >= lineNumber2Position.get(mid) && position < lineNumber2Position.get(mid + 1)))) {
			if (lineNumber2Position.get(mid) < position) {
				left = mid + 1;
			}
			else {
				right = mid - 1;
			}
			mid = (left + right) / 2;
		//	System.out.println(left + " " + right + " " + mid);
		}
		return mid + 1;
		
//		for (int i = 0; i < lineNumber2Position.size(); i++) {
//			if (i == lineNumber2Position.size() - 1 || (startPosition >= lineNumber2Position.get(i) && startPosition < lineNumber2Position.get(i + 1))) {
//				return i + 1;
//			}
//		}
//		return -1;
			
		
	}

	public List<CommentInfo> getCommentInfoList() {
		return commentInfoList;
	}

	public void setCommentInfoList(List<CommentInfo> commentInfoList) {
		this.commentInfoList = commentInfoList;
	}

	public ArrayList<Integer> getLineNumber2Position() {
		return lineNumber2Position;
	}

	public void setLineNumber2Position(ArrayList<Integer> lineNumber2Position) {
		this.lineNumber2Position = lineNumber2Position;
	}
	
	/**
	 * @author hzb-gb
	 * 为一个开始位置为position的方法、 域、 某一行代码等任一种类型的代码结构查找其匹配的注释
	 * 现在采用简单的匹配策略，就是找代码同行或前一行的注释
	 */
	public CommentInfo getRelatedCommentInfo(int position) {
		int minDifference = 9999999; //相差的行号
		int currentLineNum = getLineNumber(position);
		CommentInfo retCommentInfo = null;
		for (CommentInfo commentInfo : commentInfoList) {
			if (currentLineNum >= commentInfo.getEndLineNum() && currentLineNum - commentInfo.getEndLineNum() < minDifference) {
				 minDifference = currentLineNum - commentInfo.getEndLineNum();
				 retCommentInfo = commentInfo;
			}
		}
		if (minDifference == 0 || minDifference == 1) {
			return retCommentInfo;
		}
		else {
			return null;	
		}
	}
	
	/**
	 * @author hzb-gb
	 * 为一个开始位置为startPosition，结束位置为endPosision的的方法、 域、 某一行代码等任一种类型的代码结构查找其匹配的注释
	 * 现在采用简单的匹配策略，就是找代码同行或前一行的注释
	 */
	public List<CommentInfo> getRelatedCommentInfo(int startPosition, int endPosition) {
	//	System.out.println("startpos=" + startPosition + "  endpos=" + endPosition);
		List<CommentInfo> tempCommentInfoList = new ArrayList<CommentInfo>();
		int minDifference = 9999999; //相差的行号
		int startLineNum = getLineNumber(startPosition);
		int endLineNum = getLineNumber(endPosition);
	//	System.out.println("startline=" + startLineNum + "  endline=" + endLineNum);
		CommentInfo retCommentInfo = null;
		for (CommentInfo commentInfo : commentInfoList) {
			//System.out.println(commentInfo.getCommentString() + ";;;" + commentInfo.getEndLineNum());
			if (startLineNum <= commentInfo.getEndLineNum() && endLineNum >= commentInfo.getEndLineNum()) {
				tempCommentInfoList.add(commentInfo);
				minDifference = 0;
			}
			else if (startLineNum >= commentInfo.getEndLineNum() && startLineNum - commentInfo.getEndLineNum() < minDifference) {
				 minDifference = startLineNum - commentInfo.getEndLineNum();
				 retCommentInfo = commentInfo;
			}
		}
		if (retCommentInfo != null && minDifference == 0 || minDifference == 1) {
			tempCommentInfoList.add(0, retCommentInfo);
		}
		return tempCommentInfoList;
		
		//下面是合并成一个comment的做法
		//if (tempCommentInfoList.size() == 0 && minDifference == 0 || minDifference == 1) {
		//	return retCommentInfo;
		//}
//		if (tempCommentInfoList.size() == 1) {
//			return tempCommentInfoList.get(0);
//		}
//		else if (tempCommentInfoList.size() > 1) {
//			CommentInfo mergedCommentInfo = new CommentInfo();
//			int tempStartLine = 999999;
//			int tempEndLine = -1;
//			String commentString = "";
//			for (CommentInfo tempCommentInfo : tempCommentInfoList) {
//				if (tempCommentInfo.getStartLineNum() < tempStartLine) {
//					tempStartLine = tempCommentInfo.getStartLineNum();
//				}
//				if (tempCommentInfo.getEndLineNum() > tempEndLine) {
//					tempEndLine = tempCommentInfo.getEndLineNum();
//				}
//				if (tempCommentInfo.getCommentString() != null && tempCommentInfo.getCommentString().length() > 0) {
//					commentString = commentString + "; " + tempCommentInfo.getCommentString();
//				}
//			}
//			mergedCommentInfo.setStartLineNum(tempStartLine);
//			mergedCommentInfo.setEndLineNum(tempEndLine);
//			mergedCommentInfo.setCommentString(commentString);
//			return mergedCommentInfo;
//		}
//		else {
//			return null;	
//		}
	}
	
	
	
	/**
	 * @author hzb-gb
	 * 依据注释的开始位置找注释
	 * 主要用于为javadoc找CommentInfo
	 */
	public CommentInfo getCommentInfoByStartPosition(int javaDocStartPos) {
		for (CommentInfo commentInfo : commentInfoList) {
			if (javaDocStartPos == commentInfo.getStartPosition()) {
				 return commentInfo;
			}
		}
		return null;
	}
	
	
}
