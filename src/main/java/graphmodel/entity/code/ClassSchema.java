package graphmodel.entity.code;

import graphmodel.ManageElements;

import java.lang.reflect.Modifier;

import org.neo4j.graphdb.Node;

import discretgraphs.code.extractor.binparser.entity.BinClassInfo;

/**
 * 图数据库中的类结点，结点属性定义详情见codegraph.extractor.binparser.entity.BinClassInfo
 * @author Zeqi Lin
 *
 */

public class ClassSchema extends CodeSchema{

	public static String NAME="name";
	public static String FULLNAME="fullName";
	public static String TEMPLATE="template";
	public static String SUPERCLASS="superClass";
	public static String IMPLEMENTS="implements";
	
	public static String IS_ABSTRACT="isAbstract";
	public static String IS_FINAL="isFinal";
	public static String ACCESS="access";
	
	private BinClassInfo classInfo;
	
	public ClassSchema(BinClassInfo classInfo, Node node){
		this.node=node;
		this.classInfo=classInfo;
		node.addLabel(ManageElements.Labels.CLASS);
		
		node.setProperty(NAME, classInfo.getName());
		node.setProperty(FULLNAME, classInfo.getFullName());
		node.setProperty(TEMPLATE, classInfo.getTemplate());
		node.setProperty(SUPERCLASS, classInfo.getSuperClass());
		node.setProperty(IMPLEMENTS, line(classInfo.getInterfaces()));
		
		node.setProperty(IS_ABSTRACT, Modifier.isAbstract(classInfo.getModifiers()));
		node.setProperty(IS_FINAL, Modifier.isFinal(classInfo.getModifiers()));
		node.setProperty(ACCESS, getAccess(classInfo.getModifiers()));
	}
	
	public BinClassInfo getClassInfo(){
		return classInfo;
	}
	
}
