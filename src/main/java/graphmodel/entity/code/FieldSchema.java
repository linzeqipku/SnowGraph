package graphmodel.entity.code;

import graphmodel.ManageElements;

import java.lang.reflect.Modifier;

import org.neo4j.graphdb.Node;

import discretgraphs.code.extractor.binparser.entity.BinFieldInfo;

/**
 * 图数据库中的域结点，结点属性定义详情见codegraph.extractor.binparser.entity.BinFieldInfo
 * @author Zeqi Lin
 *
 */

public class FieldSchema extends CodeSchema{
	
	public static String NAME="name";
	public static String TYPE="type";
	public static String BELONGTO="belongTo";
	
	public static String IS_STATIC="isStatic";
	public static String IS_FINAL="isFinal";
	public static String IS_VOLATILE="isVolatile";
	public static String IS_TRANSIENT="isTransient";
	public static String ACCESS="access";

	private BinFieldInfo fieldInfo;
	
	public FieldSchema(BinFieldInfo fieldInfo, Node node){
		this.fieldInfo=fieldInfo;
		this.node=node;
		node.addLabel(ManageElements.Labels.FIELD);
		
		node.setProperty(NAME, fieldInfo.getName());
		node.setProperty(TYPE, fieldInfo.getType());
		node.setProperty(BELONGTO, fieldInfo.getBelongTo());
		
		node.setProperty(IS_STATIC, Modifier.isStatic(fieldInfo.getModifiers()));
		node.setProperty(IS_FINAL, Modifier.isFinal(fieldInfo.getModifiers()));
		node.setProperty(IS_VOLATILE, Modifier.isVolatile(fieldInfo.getModifiers()));
		node.setProperty(IS_TRANSIENT, Modifier.isTransient(fieldInfo.getModifiers()));
		node.setProperty(ACCESS, getAccess(fieldInfo.getModifiers()));
	}
	
	public BinFieldInfo getFieldInfo(){
		return fieldInfo;
	}
	
}
