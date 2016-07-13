/**
 * File-Name:JavaParser.java
 * 
 * Created on 2012-3-23 下午8:06:14
 * 
 * @author: Neo (neolimeng@gmail.com) Co Written by: Jin Jing Software
 *          Engineering Institute, Peking University, China
 * 
 *          Copyright (c) 2009, Peking University
 * 
 * 
 */
package pfr.plugins.parsers.javacode.extractor.srcparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



// import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;

import pfr.plugins.parsers.javacode.extractor.srcparser.entity.*;

/**
 * Description: Parse a source code file written in Java.
 * 
 * @author: Jin Jing
 * 
 * @version 0.1 2012-12-26
 * 
 */
public class JavaParser {
	// private static Logger log = Logger.getLogger(JavaParser.class);

	/**
	 * @param elementInfoPool
	 *            存储解析后的结果
	 * @param javaFiles
	 *            待解析的java文件列表
	 */
	public static void parse(ElementInfoPool elementInfoPool, List<File> javaFiles) {
		/**
		 * 依次解析每一个java文件
		 */
		for (int i = 0; i < javaFiles.size(); i++) {
			parse(elementInfoPool, javaFiles.get(i));
		}
	}

	/**
	 * @param elementInfoPool
	 *            存储解析前后的结果
	 * @param javaFile
	 *            待解析的java文件
	 */
	public static void parse(ElementInfoPool elementInfoPool, File javaFile) {
		try {
			/**
			 * 每个java文件都对应一个GroupInfo对象
			 */
			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setName(javaFile.getName());
			groupInfo.setPath(javaFile.getCanonicalPath());

			CodeLineAnalyser codeLineAnalyser = new CodeLineAnalyser();
			codeLineAnalyser.getCodelines(javaFile);
			groupInfo.setNormalLines(codeLineAnalyser.getNormalLines());
			groupInfo.setCommentLines(codeLineAnalyser.getCommentLines());
			groupInfo.setWhiteLines(codeLineAnalyser.getWhiteLines());

			/**
			 * 一个java文件在未被解析前，默认属于一个defaultPackage
			 */
			groupInfo.setPackageInfo(elementInfoPool.defaultPackageInfo);

			/**
			 * 一个新的groupInfo被添加到elementInfoPool，如果原来有，则不被添加
			 */
			if (!elementInfoPool.groupInfoMap.containsKey(groupInfo.getHashName())) {
				elementInfoPool.groupInfoMap.put(groupInfo.getHashName(), groupInfo);
			}
			elementInfoPool.currentGroupInfo = elementInfoPool.groupInfoMap.get(groupInfo
					.getHashName());

			/**
			 * 添加完groupInfo后，还要对它所属包的文件列表进行更新
			 */
			if (!elementInfoPool.currentGroupInfo.getPackageInfo().getGroupInfoList()
					.contains(elementInfoPool.currentGroupInfo)) {
				elementInfoPool.currentGroupInfo.getPackageInfo().getGroupInfoList()
						.add(elementInfoPool.currentGroupInfo);
			}

			/**
			 * 正式进入到对一个java文件内容的解析
			 */
			// parse(elementInfoPool, FileUtils.readFileToString(javaFile),
			// parseMethod);
			parse(elementInfoPool, UTF8.getContent(javaFile.getAbsolutePath()));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected static void mergeCommentInfoList(List<CommentInfo> commentInfoList) {
		Collections.sort(commentInfoList);
		int firstIndex = -1;
		int firstLine = -1, lastLine = -1;
		for (int i = 0; i < commentInfoList.size(); i++) {
			CommentInfo commentInfo = commentInfoList.get(i);
			if (lastLine == -1) {
				firstIndex = i;
				lastLine = commentInfo.getStartLineNum();
			}
			else {
				if (commentInfo.getStartLineNum() == lastLine + 1) {
					lastLine++;
				}
				else {
					// 存在多个连续的单行注释
					if (i > firstIndex + 1) {
						CommentInfo mergeCommentInfo = new CommentInfo();
						String mergeCommentString = "";
						for (int j = firstIndex; j < i; j++) {
							mergeCommentString += commentInfoList.get(j).getCommentString() + "\n";
						}
						mergeCommentInfo.setComment(null);
						mergeCommentInfo.setCommentString(mergeCommentString);
						mergeCommentInfo.setEndLineNum(commentInfoList.get(i - 1).getEndLineNum());
						mergeCommentInfo.setStartLineNum(commentInfoList.get(firstIndex)
								.getStartLineNum());
						commentInfoList.set(firstIndex, mergeCommentInfo);
						for (int j = firstIndex + 1; j < i; j++) {
							commentInfoList.set(j, null);
						}
					}
					firstIndex = i;
					lastLine = commentInfo.getStartLineNum();

				}
			}

		}
		// 结尾处，也存在多个
		if (firstIndex < commentInfoList.size() - 1) {
			CommentInfo mergeCommentInfo = new CommentInfo();
			String mergeCommentString = "";
			for (int j = firstIndex; j < commentInfoList.size(); j++) {
				mergeCommentString += commentInfoList.get(j).getCommentString() + "\n";
			}
			mergeCommentInfo.setComment(null);
			mergeCommentInfo.setCommentString(mergeCommentString);
			mergeCommentInfo.setEndLineNum(commentInfoList.get(commentInfoList.size() - 1)
					.getEndLineNum());
			mergeCommentInfo.setStartLineNum(commentInfoList.get(firstIndex).getStartLineNum());
			commentInfoList.set(firstIndex, mergeCommentInfo);
			for (int j = firstIndex + 1; j < commentInfoList.size(); j++) {
				commentInfoList.set(j, null);
			}
		}
		for (int i = 0; i < commentInfoList.size(); i++) {
			if (commentInfoList.get(i) == null) {
				commentInfoList.remove(i);
				i = i - 1;
			}
		}
		// for (CommentInfo commentInfo : commentInfoList) {
		// System.out.println("Line:" + commentInfo.getStartLineNum() +
		// "  Line end" + commentInfo.getEndLineNum() + "   Content:" +
		// commentInfo.getCommentString());
		// }
	}
	/**
	 * @param elementInfoPool
	 * @param javaFile
	 *            正式进入到对一个java文件内容的解析
	 * @see http
	 *      ://www.eclipse.org/articles/Article-JavaCodeManipulation_AST/index
	 *      .html
	 */
	public static void parse(ElementInfoPool elementInfoPool, String javaFile) {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(javaFile.toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

			@SuppressWarnings("unchecked")
			List<Comment> commentList = cu.getCommentList();
			ArrayList<Integer> lineNumber2Position = new ArrayList<Integer>();
			String lines[] = javaFile.split("\n");
			int posCount = 0;
			for (int i = 0; i < lines.length; i++) {
				lineNumber2Position.add(posCount);
				posCount += lines[i].toCharArray().length + "\n".toCharArray().length;
			}
			elementInfoPool.currentGroupInfo.setLineNumber2Position(lineNumber2Position);
			// System.out.println(lines.length);
			ArrayList<CommentInfo> commentInfoList = new ArrayList<CommentInfo>();
			for (Comment comment : commentList) {
				CommentInfo commentInfo = new CommentInfo();
				commentInfo.setComment(comment);
				commentInfo.setStartPosition(comment.getStartPosition());
				commentInfo.setStartLineNum(elementInfoPool.currentGroupInfo.getLineNumber(comment
						.getStartPosition()));
				commentInfo.setEndLineNum(elementInfoPool.currentGroupInfo.getLineNumber(comment
						.getStartPosition() + comment.getLength() - 1));
				String commentString = javaFile.substring(comment.getStartPosition(),
						comment.getStartPosition() + comment.getLength());
				if (CommentUtils.isUnusedCodeComment(commentString))
					continue;
				/*
				 * System.out.println("First Line #" +
				 * commentInfo.getStartLineNum());
				 * System.out.println("Last Line #" +
				 * commentInfo.getEndLineNum());
				 * System.out.println(commentString); System.out.println("" +
				 * comment.isBlockComment() + comment.isDocComment() +
				 * comment.isLineComment());
				 * System.out.println("----------------father:--------");
				 * System.out.println(comment.getParent());
				 * System.out.println("------------trimed!----------------");
				 */
				commentString = CommentUtils.trimComment(commentString);
				commentInfo.setCommentString(commentString);
				// System.out.println(commentString);

				commentInfoList.add(commentInfo);

			}
			mergeCommentInfoList(commentInfoList);
			elementInfoPool.currentGroupInfo.setCommentInfoList(commentInfoList);
			if (commentList.size() > 0) {
				Comment comment = commentList.get(0);

				// TODO 需要check
				if (comment.isDocComment() && comment.getParent() == null) {
					elementInfoPool.currentGroupInfo.setComment(comment.toString());
				}
			}

			/**
			 * 以访问者模式解析java文件内容
			 */
			JavaASTVisitor javaASTVisitor = new JavaASTVisitor();
			javaASTVisitor.setElementInfoPool(elementInfoPool);
			javaASTVisitor.setParseMethod(true);
			try {
				cu.accept(javaASTVisitor);
			}
			catch (Exception e) {
				System.out.println(e);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
