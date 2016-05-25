package graphfusion;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;
import graphmodel.entity.issuetracker.IssueSchema;
import graphmodel.entity.issuetracker.PatchSchema;
import graphmodel.entity.mail.MailSchema;
import graphmodel.entity.qa.AnswerSchema;
import graphmodel.entity.qa.QaCommentSchema;
import graphmodel.entity.qa.QuestionSchema;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class ReferenceLinker extends Linker {

	public ReferenceLinker(String dbPath) {
		super(dbPath);
	}

	@Override
	public void link() {
		try(Transaction tx = graphDb.beginTx()){
			ResourceIterable<Node> allNodes = GlobalGraphOperations.at(graphDb).getAllNodes();
			
			Map<String,Node> questionId2NodeMap = new HashMap<>();
			Map<String,Node> answerId2NodeMap = new HashMap<>();
			Map<String,Node> qaCommentId2NodeMap = new HashMap<>();
			Map<String,Node> issueName2NodeMap = new HashMap<>();
			Map<String,Node> patchId2NodeMap = new HashMap<>();
			Map<String,Node> mailId2NodeMap = new HashMap<>();
			
			//Question: questionId2NodeMap
			ResourceIterator<Node> nodes = graphDb.findNodes(ManageElements.Labels.QUESTION);
			while(nodes.hasNext()){
				Node questionNode = nodes.next();
				String questionId = questionNode.getProperty(QuestionSchema.QUESTION_ID).toString();
				questionId2NodeMap.put(questionId,questionNode);
			}
			
			//Answer: answerId2NodeMap
			nodes = graphDb.findNodes(ManageElements.Labels.ANSWER);
			while(nodes.hasNext()){
				Node node = nodes.next();
				String answerId = node.getProperty(AnswerSchema.ANSWER_ID).toString();
				answerId2NodeMap.put(answerId, node);
			}
			
			//Qa Comment: qaCommentId2NodeMap
			nodes = graphDb.findNodes(ManageElements.Labels.QA_COMMENT);
			while(nodes.hasNext()){
				Node node = nodes.next();
				String commentId = node.getProperty(QaCommentSchema.COMMENT_ID).toString();
				qaCommentId2NodeMap.put(commentId, node);
			}
			
			//Issue: issueName2NodeMap
			nodes = graphDb.findNodes(ManageElements.Labels.ISSUE);
			while(nodes.hasNext()){
				Node node = nodes.next();
				String issueName = node.getProperty(IssueSchema.ISSUE_NAME).toString();
				issueName2NodeMap.put(issueName, node);
			}
			
			//Patch: patchId2NodeMap
			nodes = graphDb.findNodes(ManageElements.Labels.PATCH);
			while(nodes.hasNext()){
				Node node = nodes.next();
				String patchId = node.getProperty(PatchSchema.PATCH_ID).toString();
				patchId2NodeMap.put(patchId, node);
			}
			
			//Mail: mailId2NodeMap
			nodes = graphDb.findNodes(ManageElements.Labels.MAIL);
			while(nodes.hasNext()){
				Node node = nodes.next();
				//mailId demo: <1993311460041066@web19m.yandex.ru>
				String mailId = node.getProperty(MailSchema.MAIL_ID).toString();
				mailId2NodeMap.put(mailId, node);
			}
			
			for(Node node:allNodes){
				String content = Schema.getContent(node);
				if(content.isEmpty()){
					continue;
				}
				
				Set<String> urlSet = findURLs(content);
				Set<Node> referenceNodeSet = new HashSet<>();
				
				for(String url:urlSet){
					//Stack Overflow
					if(url.startsWith("http://stackoverflow.com/")){
						String relativePath = url.substring("http://stackoverflow.com/".length());
						int len = relativePath.length();
						if(relativePath.startsWith("q/")){
							int beginIndex = "q/".length();
							int endIndex = beginIndex;
							while(endIndex < len && Character.isDigit(relativePath.charAt(endIndex))){
								endIndex++;
							}
							String questionId = relativePath.substring(beginIndex,endIndex);
							Node referenceNode = questionId2NodeMap.get(questionId);
							if(referenceNode != null){
								referenceNodeSet.add(referenceNode);
							}
						}else if(relativePath.startsWith("a/")){
							int beginIndex = "a/".length();
							int endIndex = beginIndex;
							while(endIndex < len && Character.isDigit(relativePath.charAt(endIndex))){
								endIndex++;
							}
							String answerId = relativePath.substring(beginIndex,endIndex);
							Node referenceNode = answerId2NodeMap.get(answerId);
							if(referenceNode != null){
								referenceNodeSet.add(referenceNode);
							}
						}else if(relativePath.startsWith("questions/")){
							if(relativePath.contains("#comment")){//Comment
								int commentIndex = relativePath.indexOf("#comment");
								int commentIdBeginIndex = commentIndex + "#comment".length();
								//for the case: #comment-40382219 of "http://stackoverflow.com/questions/510462/is-system-nanotime-completely-useless#comment-40382219"
								if(relativePath.charAt(commentIdBeginIndex) == '-'){
									commentIdBeginIndex++;
								}
								
								int commentIdEndIndex = commentIdBeginIndex;
								while(commentIdEndIndex < len && Character.isDigit(relativePath.charAt(commentIdEndIndex))){
									commentIdEndIndex++;
								}
								String commentId = relativePath.substring(commentIdBeginIndex,commentIdEndIndex);
								
								Node referenceNode = qaCommentId2NodeMap.get(commentId);
								if(referenceNode != null){
									referenceNodeSet.add(referenceNode);
								}
							}else if(relativePath.contains("#")){//Answer
								int answerIndex = relativePath.indexOf("#");
								int answerIdBeginIndex = answerIndex + "#".length();
								int answerIdEndIndex = answerIdBeginIndex;
								while(answerIdEndIndex < len && Character.isDigit(relativePath.charAt(answerIdEndIndex))){
									answerIdEndIndex++;
								}
								String answerId = relativePath.substring(answerIdBeginIndex,answerIdEndIndex);
								Node referenceNode = answerId2NodeMap.get(answerId);
								if(referenceNode != null){
									referenceNodeSet.add(referenceNode);
								}
							}else{//Question
								int questionIdBeginIndex = "questions/".length();
								int questionIdEndIndex = questionIdBeginIndex;
								while(questionIdEndIndex < len && Character.isDigit(relativePath.charAt(questionIdEndIndex))){
									questionIdEndIndex++;
								}
								String questionId = relativePath.substring(questionIdBeginIndex,questionIdEndIndex);
								Node referenceNode = questionId2NodeMap.get(questionId);
								if(referenceNode != null){
									referenceNodeSet.add(referenceNode);
								}
							}
						}
					}else if(url.startsWith("https://issues.apache.org/jira/browse/")){//Issue link
						int len = url.length();
						int beginIndex = "https://issues.apache.org/jira/browse/".length();
						int endIndex = beginIndex;
						//Issue Name pattern: PROJECT_NAME-xxxx
						while(endIndex < len && Character.isUpperCase(url.charAt(endIndex))){
							endIndex++;
						}
						
						if(endIndex < len && url.charAt(endIndex) == '-'){
							endIndex++;
							
							while(endIndex < len && Character.isDigit(url.charAt(endIndex))){
								endIndex++;
							}
							
							String issueName = url.substring(beginIndex,endIndex);
							Node referenceNode = issueName2NodeMap.get(issueName);
							if(referenceNode != null){
								referenceNodeSet.add(referenceNode);
							}
						}
					}else if(url.startsWith("https://issues.apache.org/jira/secure/attachment/")){//Patch link
						int len = url.length();
						int beginIndex = "https://issues.apache.org/jira/secure/attachment/".length();
						int endIndex = beginIndex;
						while(endIndex < len && Character.isDigit(url.charAt(endIndex))){
							endIndex++;
						}
						String patchId = url.substring(beginIndex,endIndex);
						Node referenceNode = patchId2NodeMap.get(patchId);
						if(referenceNode != null){
							referenceNodeSet.add(referenceNode);
						}
					}else if(url.startsWith("http://mail-archives.apache.org/mod_mbox/")){//Mail Link
						String mailId = null;
						
						//%3cxxxx%3e
						int beginIndex = url.indexOf("%3c");
						if(beginIndex != -1){
							int endIndex = url.indexOf("%3e");
							if(endIndex != -1){
								mailId = "<" + url.substring(beginIndex + 3,endIndex) + ">";
							}
						}
						
						//%3Cxxxxx%3E
						beginIndex = url.indexOf("%3C");
						if(beginIndex != -1){
							int endIndex = url.indexOf("%3E");
							if(endIndex != -1){
								mailId = "<" + url.substring(beginIndex + 3,endIndex) + ">";
							}
						}
						
						//"<" + xxxxx + ">"
						beginIndex = url.indexOf("<");
						if(beginIndex != -1){
							int endIndex = url.indexOf(">", beginIndex);
							if(endIndex != -1){
								mailId = url.substring(beginIndex,endIndex+1);
							}
						}
						
						if(mailId != null){
							Node referenceNode = mailId2NodeMap.get(mailId);
							if(referenceNode != null){
								referenceNodeSet.add(referenceNode);
							}
						}
					}
				}
				
				for(Node referenceNode: referenceNodeSet){
					node.createRelationshipTo(referenceNode, ManageElements.RelTypes.DOC_LEVEL_REFER);
					System.out.printf("Reference Link: %s -[DOC_LEVEL_REFER]-> %s\n",node,referenceNode);
				}
			}	
			tx.success();
		}
	}
	
	public static void main(String[] args) {
		Linker linker = new ReferenceLinker("data/lucene/graphDb(Basic SKG with User Connections)/full");
		linker.link();
		linker.graphDb.shutdown();
		System.out.println("Finished!");
	}
	
	/*
	 * 找到Content中包含的所有URL （以“http://" 或 “https://” 开头的字符串）
	 */
	private static Set<String> findURLs(String content){
		if(content == null){
			return Collections.emptySet();
		}
		
		int prefixLen = "https://".length();
		
		Set<String> urlSet = new HashSet<String>();
		int beginIndex = 0, endIndex = 0;
		int len = content.length();
		while(beginIndex < len){
			if(beginIndex + prefixLen < len && content.charAt(beginIndex) == 'h' 
											&& content.charAt(beginIndex+1) == 't'
											&& content.charAt(beginIndex+2) == 't'
											&& content.charAt(beginIndex+3) == 'p')
			{
				int index = beginIndex + 4;
				if(content.charAt(index) == 's'){
					index++;
				}
				
				//begins with "http://" or "https://"
				if(content.charAt(index) == ':' && content.charAt(index+1) == '/' && content.charAt(index+2) == '/'){
					endIndex = index;
					while(endIndex < len && content.charAt(endIndex) != ' ' 
										 && content.charAt(endIndex) != '"' 
										 && content.charAt(endIndex) != '\r'
										 && content.charAt(endIndex) != '\n'){
						endIndex++;
					}
					
					String url = content.substring(beginIndex,endIndex);
					urlSet.add(url);
					beginIndex = endIndex + 1;
				}else{
					beginIndex++;
				}
			}else{
				beginIndex++;
			}
		}
		return urlSet;
	}
}
