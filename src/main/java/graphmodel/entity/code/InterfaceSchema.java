package graphmodel.entity.code;

import graphmodel.ManageElements;

import org.neo4j.graphdb.Node;

import discretgraphs.code.extractor.binparser.entity.BinInterfaceInfo;

/**
 * 图数据库中的接口结点，结点属性定义详情见codegraph.extractor.binparser.entity.BinInterfaceInfo
 * @author Zeqi Lin
 *
 */

public class InterfaceSchema extends CodeSchema{

	public static String NAME="name";
	public static String FULLNAME="fullName";
	public static String TEMPLATE="template";
	
	private BinInterfaceInfo interfaceInfo;
	
	public InterfaceSchema(BinInterfaceInfo interfaceInfo, Node node){
		this.interfaceInfo=interfaceInfo;
		this.node=node;
		node.addLabel(ManageElements.Labels.INTERFACE);
		
		node.setProperty(NAME, interfaceInfo.getName());
		node.setProperty(FULLNAME, interfaceInfo.getFullName());
		node.setProperty(TEMPLATE, interfaceInfo.getTemplate());
	}
	
	public BinInterfaceInfo getInterfaceInfo(){
		return interfaceInfo;
	}

}
