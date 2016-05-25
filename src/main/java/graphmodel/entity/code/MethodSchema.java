package graphmodel.entity.code;

import graphmodel.ManageElements;

import java.lang.reflect.Modifier;

import org.neo4j.graphdb.Node;

import discretgraphs.code.extractor.binparser.entity.BinMethodInfo;

/**
 * 图数据库中的方法结点，结点属性定义详情见codegraph.extractor.binparser.entity.BinMethodInfo
 * @author Zeqi Lin
 *
 */

public class MethodSchema extends CodeSchema{
	
	public static String NAME="name";
	public static String BELONGTO="belongTo";
	public static String PARAMS="params";
	public static String RETURN="rt";
	public static String THROWS="throws";
	
	public static String ACCESS="access";
	public static String IS_ABSTRACT="isAbstract";
	public static String IS_STATIC="isStatic";
	public static String IS_FINAL="isFinal";
	public static String IS_NATIVE="isNative";
	public static String IS_SYNCHRONIZED="isSynchronized";
	
	private BinMethodInfo methodInfo;
	
	public MethodSchema(BinMethodInfo methodInfo,Node node){
		this.methodInfo=methodInfo;
		this.node=node;
		node.addLabel(ManageElements.Labels.METHOD);
		
		node.setProperty(NAME, methodInfo.getName());
		node.setProperty(BELONGTO, methodInfo.getBelongTo());
		node.setProperty(PARAMS, line(methodInfo.getParams()));
		node.setProperty(RETURN, methodInfo.getRt());
		node.setProperty(THROWS, line(methodInfo.getExceptions()));
		
		node.setProperty(IS_ABSTRACT, Modifier.isAbstract(methodInfo.getModifiers()));
		node.setProperty(IS_STATIC, Modifier.isStatic(methodInfo.getModifiers()));
		node.setProperty(IS_FINAL, Modifier.isFinal(methodInfo.getModifiers()));
		node.setProperty(IS_NATIVE, Modifier.isNative(methodInfo.getModifiers()));
		node.setProperty(IS_SYNCHRONIZED, Modifier.isSynchronized(methodInfo.getModifiers()));
		node.setProperty(ACCESS, getAccess(methodInfo.getModifiers()));
	}
	
	public BinMethodInfo getMethodInfo(){
		return methodInfo;
	}

}
