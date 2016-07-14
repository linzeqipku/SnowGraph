package utils;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;
import graphmodel.entity.code.ClassSchema;
import graphmodel.entity.code.FieldSchema;
import graphmodel.entity.code.InterfaceSchema;
import graphmodel.entity.code.MethodSchema;
import graphmodel.entity.issuetracker.IssueCommentSchema;
import graphmodel.entity.issuetracker.IssueSchema;
import graphmodel.entity.issuetracker.IssueUserSchema;
import graphmodel.entity.issuetracker.PatchSchema;
import graphmodel.entity.qa.AnswerSchema;
import graphmodel.entity.qa.QaCommentSchema;
import graphmodel.entity.qa.QaUserSchema;
import graphmodel.entity.qa.QuestionSchema;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import pfr.plugins.parsers.mail.entity.MailSchema;
import pfr.plugins.parsers.mail.entity.MailUserInfo;
import javassist.Modifier;

/*
 * Utility for working with Fields by reflection.
 */
public class FieldUtil {
	/*
	 * Get all static String fields' values including parents' static fields.
	 */
	public static List<String> getAllStaticFieldValues(Class<?> clazz){
		if(clazz == null){
			return Collections.emptyList();
		}
		
		List<String> staticFieldValues = new ArrayList<>();
		
		getAllStaticFieldValues(clazz,staticFieldValues);
		
		return staticFieldValues;
	}
	
	/*
	 * Get all static String fields' values including parents' static fields recursively.
	 */
	private static void getAllStaticFieldValues(Class<?> clazz, List<String> values){
		try{
			Field[] declaredFields = clazz.getDeclaredFields();
			for(Field field: declaredFields){
				//static field
				if(Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)){
					if(!field.isAccessible()){
						field.setAccessible(true);
					}
					
					Object objValue = field.get(null);
					if(objValue == null){
						continue;
					}
					
					String value = objValue.toString();
					values.add(value);
				}
			}
		}catch(IllegalAccessException e){
			e.printStackTrace();
		}
		
		Class<?> parentClazz = clazz.getSuperclass();
		if(parentClazz != null){
			getAllStaticFieldValues(parentClazz,values);
		}
	}
	
	public static void main(String[] args) {	
		List<Class<? extends Schema>> schemaClassList = new ArrayList<>();
		List<String> schemaLabelList = new ArrayList<>();
		
		/*********************Issue Graph**********************/
		//Issue Schema
		schemaClassList.add(IssueSchema.class);
		schemaLabelList.add(ManageElements.Labels.ISSUE.toString());
		
		//Patch Schema
		schemaClassList.add(PatchSchema.class);
		schemaLabelList.add(ManageElements.Labels.PATCH.toString());
		
		//Issue Comment Schema
		schemaClassList.add(IssueCommentSchema.class);
		schemaLabelList.add(ManageElements.Labels.ISSUE_COMMENT.toString());
		
		//Issue User Schema
		schemaClassList.add(IssueUserSchema.class);
		schemaLabelList.add(ManageElements.Labels.ISSUE_USER.toString());
	
		
		
		/*********************Mail Graph**********************/
		//Mail Schema
		schemaClassList.add(MailSchema.class);
		schemaLabelList.add(ManageElements.Labels.MAIL.toString());
		
		//Mail User Schema
		schemaClassList.add(MailUserInfo.class);
		schemaLabelList.add(ManageElements.Labels.MAIL_USER.toString());
		
		
		/*********************So Graph**********************/
		//Question Schema
		schemaClassList.add(QuestionSchema.class);
		schemaLabelList.add(ManageElements.Labels.QUESTION.toString());
		
		//Answer Schema
		schemaClassList.add(AnswerSchema.class);
		schemaLabelList.add(ManageElements.Labels.ANSWER.toString());
		
		//Qa Comment Schema
		schemaClassList.add(QaCommentSchema.class);
		schemaLabelList.add(ManageElements.Labels.QA_COMMENT.toString());
		
		//Qa User Schema
		schemaClassList.add(QaUserSchema.class);
		schemaLabelList.add(ManageElements.Labels.QA_USER.toString());
		
				
		/*********************Code Graph**********************/
		//Class Schema
		schemaClassList.add(ClassSchema.class);
		schemaLabelList.add(ManageElements.Labels.CLASS.toString());
		
		//Interface Schema
		schemaClassList.add(InterfaceSchema.class);
		schemaLabelList.add(ManageElements.Labels.INTERFACE.toString());
		
		//Field Schema
		schemaClassList.add(FieldSchema.class);
		schemaLabelList.add(ManageElements.Labels.FIELD.toString());
		
		//Method Schema
		schemaClassList.add(MethodSchema.class);
		schemaLabelList.add(ManageElements.Labels.METHOD.toString());
		
		int schemaNum = schemaClassList.size();
		for(int i=0;i<schemaNum;i++){
			Class<? extends Schema> schemaClass = schemaClassList.get(i);
			String schemaLabel = schemaLabelList.get(i);
			
			List<String> schemaProperties = new ArrayList<>();
			getAllStaticFieldValues(schemaClass,schemaProperties);
			
			for(String schemaProperty: schemaProperties){
				//对于长度小于2的属性，无需在Cypher中进行补全提示
				if(schemaProperty.length() < 2){
					continue;
				}else if(schemaProperty.equals("uuid")){//对uuid暂不进行补全，不准备对Cypher提供
					continue;
				}
				System.out.printf("insert ignore into neo4jschema values('%s','%s');\n",schemaLabel,schemaProperty);
			}
		}
	}
}
