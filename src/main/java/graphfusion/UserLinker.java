package graphfusion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import pfr.plugins.parsers.mail.entity.MailUserInfo;
import utils.MultimapUtil;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class UserLinker extends Linker{
	public UserLinker(String dbPath) {
		super(dbPath);
	}

	public static void main(String[] args) throws Exception{
		Linker linker = new UserLinker("data/lucene/graphDb(Basic SKG)/full");
		linker.link();
	}
	
	@Override
	public void link() {
		List<QaUserSchema> qaUsers = new ArrayList<>();
		List<IssueUserSchema> issueUsers = new ArrayList<>();
		List<MailUserInfo> mailUsers = new ArrayList<>();
		
		try(Transaction tx = graphDb.beginTx()){
			/********************Get All Users (QA Users, Issue Users and Mail Users)****************/
			//get all QA Users
			ResourceIterator<Node> nodes = graphDb.findNodes(ManageElements.Labels.QA_USER);
			while(nodes.hasNext()){
				Node node = nodes.next();
				QaUserSchema schema = new QaUserSchema(node);
				qaUsers.add(schema);
			}

			//get all Issue users
			nodes = graphDb.findNodes(ManageElements.Labels.ISSUE_USER);
			while(nodes.hasNext()){
				Node node = nodes.next();
				IssueUserSchema schema = new IssueUserSchema(node);
				issueUsers.add(schema);
			}
			
			//get all Mail users
			nodes = graphDb.findNodes(ManageElements.Labels.MAIL_USER);
			while(nodes.hasNext()){
				Node node = nodes.next();
				MailUserInfo schema = new MailUserInfo(node);
				mailUsers.add(schema);
			}
			
			/**********************Get the mapping from mail, name to users***********************/
			//get the mapping from name to QA users
			Multimap<String, QaUserSchema> name2QaUserMultimap = ArrayListMultimap.create();
			for(QaUserSchema schema: qaUsers){
				String name = schema.getDisplayName();
				name2QaUserMultimap.put(name,schema);
			}
			//remove all the keys that with multiple values
			MultimapUtil.removeAllKeysWithMultiValues(name2QaUserMultimap);
			
			//get the mapping from mail, name to Issue users
			HashMap<String,IssueUserSchema> mail2IssueUserMap = new HashMap<>();
			Multimap<String,IssueUserSchema> name2IssueUserMultimap = ArrayListMultimap.create();
			for(IssueUserSchema schema: issueUsers){
				String mail = schema.getIssueUserInfo().getEmailAddress();
				String name = schema.getIssueUserInfo().getDisplayName();
				
				mail2IssueUserMap.put(mail, schema);
				name2IssueUserMultimap.put(name, schema);
			}
			//remove all the keys that with multiple values
			MultimapUtil.removeAllKeysWithMultiValues(name2IssueUserMultimap);
			
			//get the mapping from mail, name to Mail users
			HashMap<String,MailUserInfo> mail2MailUserMap = new HashMap<>();
			Multimap<String,MailUserInfo> name2MailUserMultimap = ArrayListMultimap.create();
			for(MailUserInfo schema: mailUsers){
				String mail = schema.getMail();
				String name = schema.getName();
				
				mail2MailUserMap.put(mail, schema);
				name2MailUserMultimap.put(name, schema);
			}
			//remove all the keys that with multiple values
			MultimapUtil.removeAllKeysWithMultiValues(name2MailUserMultimap);
			
			int linkCount = 0;
			/**********************Link by Mail Address***********************/
			for(String issueMail: mail2IssueUserMap.keySet()){
				IssueUserSchema issueSchema = mail2IssueUserMap.get(issueMail);
				
				MailUserInfo mailSchema = mail2MailUserMap.get(issueMail);
				if(mailSchema != null){
					linkCount++;
					issueSchema.getNode().createRelationshipTo(mailSchema.getNode(), ManageElements.RelTypes.SAME_USER);

					System.out.printf("Mail Linker: from %s -[SAME_USER]- %s\n", issueMail,issueMail);
				}
			}
			
			/**********************Link by Unique Name***********************/
			for(String name: name2QaUserMultimap.keySet()){
				QaUserSchema qaUserSchema = MultimapUtil.getUniqueValue(name2QaUserMultimap,name);
				if(qaUserSchema == null){
					continue;
				}
				
				IssueUserSchema issueUserSchema = MultimapUtil.getUniqueValue(name2IssueUserMultimap,name);
				MailUserInfo mailUserSchema = MultimapUtil.getUniqueValue(name2MailUserMultimap,name);
				
				if(issueUserSchema != null){
					linkCount++;
					qaUserSchema.getNode().createRelationshipTo(issueUserSchema.getNode(), ManageElements.RelTypes.SAME_USER);
					
					System.out.printf("Name Linker: from %s -[SAME_USER]- %s\n", name,name);
				}
				if(mailUserSchema != null){
					linkCount++;
					qaUserSchema.getNode().createRelationshipTo(mailUserSchema.getNode(), ManageElements.RelTypes.SAME_USER);

					System.out.printf("Name Linker: from %s -[SAME_USER]- %s\n", name,name);
				}
			}
			
			System.out.println("Total Link count:" + linkCount);
			tx.success();
		}
		
		graphDb.shutdown();
	}
}