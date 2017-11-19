package graphdb.extractors.parsers.javacode;

import graphdb.extractors.parsers.javacode.entity.ClassInfo;
import graphdb.extractors.parsers.javacode.entity.InterfaceInfo;
import graphdb.extractors.parsers.javacode.entity.MethodInfo;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import graphdb.extractors.parsers.javacode.entity.FieldInfo;

public class JavaCodeUtils {

    public static void createClassNode(ClassInfo classInfo, Node node) {
        node.addLabel(Label.label(JavaCodeExtractor.CLASS));
        node.setProperty(JavaCodeExtractor.CLASS_NAME, classInfo.name);
        node.setProperty(JavaCodeExtractor.CLASS_FULLNAME, classInfo.fullName);
        node.setProperty(JavaCodeExtractor.CLASS_SUPERCLASS, classInfo.superClassType);
        node.setProperty(JavaCodeExtractor.CLASS_IMPLEMENTS, String.join(", ", classInfo.superInterfaceTypeList));
        node.setProperty(JavaCodeExtractor.CLASS_IS_ABSTRACT, classInfo.isAbstract);
        node.setProperty(JavaCodeExtractor.CLASS_IS_FINAL, classInfo.isFinal);
        node.setProperty(JavaCodeExtractor.CLASS_ACCESS, classInfo.visibility);
        node.setProperty(JavaCodeExtractor.CLASS_COMMENT, classInfo.comment);
        node.setProperty(JavaCodeExtractor.CLASS_CONTENT, classInfo.content);
        node.setProperty(JavaCodeExtractor.SIGNATURE, classInfo.fullName);
    }

    public static void createInterfaceNode(InterfaceInfo interfaceInfo, Node node) {
        node.addLabel(Label.label(JavaCodeExtractor.INTERFACE));
        node.setProperty(JavaCodeExtractor.INTERFACE_NAME, interfaceInfo.name);
        node.setProperty(JavaCodeExtractor.INTERFACE_FULLNAME, interfaceInfo.fullName);
        node.setProperty(JavaCodeExtractor.INTERFACE_SUPERINTERFACES, String.join(", ", interfaceInfo.superInterfaceTypeList));
        node.setProperty(JavaCodeExtractor.INTERFACE_ACCESS, interfaceInfo.visibility);
        node.setProperty(JavaCodeExtractor.INTERFACE_COMMENT, interfaceInfo.comment);
        node.setProperty(JavaCodeExtractor.INTERFACE_CONTENT, interfaceInfo.content);
        node.setProperty(JavaCodeExtractor.SIGNATURE, interfaceInfo.fullName);
    }

    public static void createMethodNode(MethodInfo methodInfo, Node node) {
        node.addLabel(Label.label(JavaCodeExtractor.METHOD));
        node.setProperty(JavaCodeExtractor.METHOD_NAME, methodInfo.name);
        node.setProperty(JavaCodeExtractor.METHOD_RETURN, methodInfo.returnString);
        node.setProperty(JavaCodeExtractor.METHOD_ACCESS, methodInfo.visibility);
        node.setProperty(JavaCodeExtractor.METHOD_IS_CONSTRUCTOR, methodInfo.isConstruct);
        node.setProperty(JavaCodeExtractor.METHOD_IS_ABSTRACT, methodInfo.isAbstract);
        node.setProperty(JavaCodeExtractor.METHOD_IS_FINAL, methodInfo.isFinal);
        node.setProperty(JavaCodeExtractor.METHOD_IS_STATIC, methodInfo.isStatic);
        node.setProperty(JavaCodeExtractor.METHOD_IS_SYNCHRONIZED, methodInfo.isSynchronized);
        node.setProperty(JavaCodeExtractor.METHOD_CONTENT, methodInfo.content);
        node.setProperty(JavaCodeExtractor.METHOD_COMMENT, methodInfo.comment);
        node.setProperty(JavaCodeExtractor.METHOD_BELONGTO, methodInfo.belongTo);
        node.setProperty(JavaCodeExtractor.METHOD_PARAMS, methodInfo.paramString);
        node.setProperty(JavaCodeExtractor.METHOD_THROWS, String.join(", ", methodInfo.throwSet));
        node.setProperty(JavaCodeExtractor.SIGNATURE, methodInfo.belongTo+"."+methodInfo.name+"("+methodInfo.paramString+")");
    }

    public static void createFieldNode(FieldInfo fieldInfo, Node node) {
        node.addLabel(Label.label(JavaCodeExtractor.FIELD));
        node.setProperty(JavaCodeExtractor.FIELD_NAME, fieldInfo.name);
        node.setProperty(JavaCodeExtractor.FIELD_TYPE, fieldInfo.typeString);
        node.setProperty(JavaCodeExtractor.FIELD_BELONGTO, fieldInfo.belongTo);
        node.setProperty(JavaCodeExtractor.FIELD_IS_STATIC, fieldInfo.isStatic);
        node.setProperty(JavaCodeExtractor.FIELD_IS_FINAL, fieldInfo.isFinal);
        node.setProperty(JavaCodeExtractor.FIELD_ACCESS, fieldInfo.visibility);
        node.setProperty(JavaCodeExtractor.FIELD_COMMENT, fieldInfo.comment);
        node.setProperty(JavaCodeExtractor.SIGNATURE, fieldInfo.belongTo+"."+fieldInfo.name);
    }

}
