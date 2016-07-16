package pfr.plugins.parsers.javacode;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinClassInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinFieldInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinInterfaceInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinMethodInfo;

public class JavaCodeUtils
{

	public static void createClassNode(BinClassInfo classInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.CLASS));
		node.setProperty(PfrPluginForJavaCode.CLASS_NAME, classInfo.getName());
		node.setProperty(PfrPluginForJavaCode.CLASS_FULLNAME, classInfo.getFullName());
		node.setProperty(PfrPluginForJavaCode.CLASS_TEMPLATE, classInfo.getTemplate());
		node.setProperty(PfrPluginForJavaCode.CLASS_SUPERCLASS, classInfo.getSuperClass());
		node.setProperty(PfrPluginForJavaCode.CLASS_IMPLEMENTS, line(classInfo.getInterfaces()));
		node.setProperty(PfrPluginForJavaCode.CLASS_IS_ABSTRACT, Modifier.isAbstract(classInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.CLASS_IS_FINAL, Modifier.isFinal(classInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.CLASS_ACCESS, getAccess(classInfo.getModifiers()));
	}
	
	public static void createInterfaceNode(BinInterfaceInfo interfaceInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.INTERFACE));
		node.setProperty(PfrPluginForJavaCode.INTERFACE_NAME, interfaceInfo.getName());
		node.setProperty(PfrPluginForJavaCode.INTERFACE_FULLNAME, interfaceInfo.getFullName());
		node.setProperty(PfrPluginForJavaCode.INTERFACE_TEMPLATE, interfaceInfo.getTemplate());
	}
	
	public static void createMethodNode(BinMethodInfo methodInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.METHOD));
		node.setProperty(PfrPluginForJavaCode.METHOD_NAME, methodInfo.getName());
		node.setProperty(PfrPluginForJavaCode.METHOD_BELONGTO, methodInfo.getBelongTo());
		node.setProperty(PfrPluginForJavaCode.METHOD_PARAMS, line(methodInfo.getParams()));
		node.setProperty(PfrPluginForJavaCode.METHOD_RETURN, methodInfo.getRt());
		node.setProperty(PfrPluginForJavaCode.METHOD_THROWS, line(methodInfo.getExceptions()));
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_ABSTRACT, Modifier.isAbstract(methodInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_STATIC, Modifier.isStatic(methodInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_FINAL, Modifier.isFinal(methodInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_NATIVE, Modifier.isNative(methodInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.METHOD_IS_SYNCHRONIZED, Modifier.isSynchronized(methodInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.METHOD_ACCESS, getAccess(methodInfo.getModifiers()));
	}
	
	public static void createFieldNode(BinFieldInfo fieldInfo, Node node){
		node.addLabel(Label.label(PfrPluginForJavaCode.FIELD));
		node.setProperty(PfrPluginForJavaCode.FIELD_NAME, fieldInfo.getName());
		node.setProperty(PfrPluginForJavaCode.FIELD_TYPE, fieldInfo.getType());
		node.setProperty(PfrPluginForJavaCode.FIELD_BELONGTO, fieldInfo.getBelongTo());
		node.setProperty(PfrPluginForJavaCode.FIELD_IS_STATIC, Modifier.isStatic(fieldInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.FIELD_IS_FINAL, Modifier.isFinal(fieldInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.FIELD_IS_VOLATILE, Modifier.isVolatile(fieldInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.FIELD_IS_TRANSIENT, Modifier.isTransient(fieldInfo.getModifiers()));
		node.setProperty(PfrPluginForJavaCode.FIELD_ACCESS, getAccess(fieldInfo.getModifiers()));
	}
	
	public static String getAccess(int modifier){
		boolean p1=Modifier.isPrivate(modifier);
		boolean p2=Modifier.isProtected(modifier);
		boolean p3=Modifier.isPublic(modifier);
		if (p1&&p2)
			return "private protected";
		if (p1)
			return "private";
		if (p2)
			return "protected";
		if (p3)
			return "public";
		return "package";
	}
	
	static String line(Collection<String> v){
		String SEPERATOR=",";
		String r="";
		Iterator<String> iter=v.iterator();
		String seperator="";
		while (iter.hasNext()){
			r+=seperator+iter.next();
			seperator=SEPERATOR;
		}
		return r;
	}
	
}
