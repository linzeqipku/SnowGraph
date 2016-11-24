package pfr.plugins.parsers.javacode;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.javacode.entity.ClassInfo;
import pfr.plugins.parsers.javacode.entity.FieldInfo;
import pfr.plugins.parsers.javacode.entity.InterfaceInfo;
import pfr.plugins.parsers.javacode.entity.MethodInfo;

public class JavaCodeUtils
{

	public static void createClassNode(ClassInfo classInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.CLASS));
		node.setProperty(PfrPluginForJavaCode.CLASS_NAME, classInfo.name);
		node.setProperty(PfrPluginForJavaCode.CLASS_FULLNAME, classInfo.fullName);
		node.setProperty(PfrPluginForJavaCode.CLASS_SUPERCLASS, classInfo.superClassType);
		node.setProperty(PfrPluginForJavaCode.CLASS_IMPLEMENTS, String.join(", ", classInfo.superInterfaceTypeList));
		node.setProperty(PfrPluginForJavaCode.CLASS_IS_ABSTRACT, classInfo.isAbstract);
		node.setProperty(PfrPluginForJavaCode.CLASS_IS_FINAL, classInfo.isFinal);
		node.setProperty(PfrPluginForJavaCode.CLASS_ACCESS, classInfo.visibility);
		node.setProperty(PfrPluginForJavaCode.CLASS_COMMENT, classInfo.comment);
		node.setProperty(PfrPluginForJavaCode.CLASS_CONTENT, classInfo.content);
	}
	
	public static void createInterfaceNode(InterfaceInfo interfaceInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.INTERFACE));
		node.setProperty(PfrPluginForJavaCode.INTERFACE_NAME, interfaceInfo.name);
		node.setProperty(PfrPluginForJavaCode.INTERFACE_FULLNAME, interfaceInfo.fullName);
		node.setProperty(PfrPluginForJavaCode.INTERFACE_SUPERINTERFACES, String.join(", ", interfaceInfo.superInterfaceTypeList));
		node.setProperty(PfrPluginForJavaCode.INTERFACE_ACCESS, interfaceInfo.visibility);
		node.setProperty(PfrPluginForJavaCode.INTERFACE_COMMENT, interfaceInfo.comment);
		node.setProperty(PfrPluginForJavaCode.INTERFACE_CONTENT, interfaceInfo.content);
	}
	
	public static void createMethodNode(MethodInfo methodInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.METHOD));
		node.setProperty(PfrPluginForJavaCode.METHOD_NAME, methodInfo.name);
		node.setProperty(PfrPluginForJavaCode.METHOD_RETURN, methodInfo.returnString);
		node.setProperty(PfrPluginForJavaCode.METHOD_ACCESS, methodInfo.visibility);
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_CONSTRUCTOR, methodInfo.isConstruct);
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_ABSTRACT, methodInfo.isAbstract);
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_FINAL, methodInfo.isFinal);
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_STATIC, methodInfo.isStatic);
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_SYNCHRONIZED, methodInfo.isSynchronized);
		node.setProperty(PfrPluginForJavaCode.METHOD_CONTENT, methodInfo.content);
		node.setProperty(PfrPluginForJavaCode.METHOD_COMMENT, methodInfo.comment);
		node.setProperty(PfrPluginForJavaCode.METHOD_BELONGTO, methodInfo.belongTo);
		node.setProperty(PfrPluginForJavaCode.METHOD_PARAMS, methodInfo.paramString);
		node.setProperty(PfrPluginForJavaCode.METHOD_THROWS, String.join(", ", methodInfo.throwSet));
	}
	
	public static void createFieldNode(FieldInfo fieldInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.FIELD));
		node.setProperty(PfrPluginForJavaCode.FIELD_NAME, fieldInfo.name);
		node.setProperty(PfrPluginForJavaCode.FIELD_TYPE, fieldInfo.typeString);
		node.setProperty(PfrPluginForJavaCode.FIELD_BELONGTO, fieldInfo.belongTo);
		node.setProperty(PfrPluginForJavaCode.FIELD_IS_STATIC, fieldInfo.isStatic);
		node.setProperty(PfrPluginForJavaCode.FIELD_IS_FINAL, fieldInfo.isFinal);
		node.setProperty(PfrPluginForJavaCode.FIELD_ACCESS, fieldInfo.visibility);
		node.setProperty(PfrPluginForJavaCode.FIELD_COMMENT, fieldInfo.comment);
	}
	
}
