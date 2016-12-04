package extractors.parsers.javacode;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import extractors.parsers.javacode.entity.ClassInfo;
import extractors.parsers.javacode.entity.FieldInfo;
import extractors.parsers.javacode.entity.InterfaceInfo;
import extractors.parsers.javacode.entity.MethodInfo;

public class JavaCodeUtils {

    public static void createClassNode(ClassInfo classInfo, Node node) {
        node.addLabel(Label.label(JavaCodeKnowledgeExtractor.CLASS));
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_NAME, classInfo.name);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_FULLNAME, classInfo.fullName);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_SUPERCLASS, classInfo.superClassType);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_IMPLEMENTS, String.join(", ", classInfo.superInterfaceTypeList));
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_IS_ABSTRACT, classInfo.isAbstract);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_IS_FINAL, classInfo.isFinal);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_ACCESS, classInfo.visibility);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_COMMENT, classInfo.comment);
        node.setProperty(JavaCodeKnowledgeExtractor.CLASS_CONTENT, classInfo.content);
    }

    public static void createInterfaceNode(InterfaceInfo interfaceInfo, Node node) {
        node.addLabel(Label.label(JavaCodeKnowledgeExtractor.INTERFACE));
        node.setProperty(JavaCodeKnowledgeExtractor.INTERFACE_NAME, interfaceInfo.name);
        node.setProperty(JavaCodeKnowledgeExtractor.INTERFACE_FULLNAME, interfaceInfo.fullName);
        node.setProperty(JavaCodeKnowledgeExtractor.INTERFACE_SUPERINTERFACES, String.join(", ", interfaceInfo.superInterfaceTypeList));
        node.setProperty(JavaCodeKnowledgeExtractor.INTERFACE_ACCESS, interfaceInfo.visibility);
        node.setProperty(JavaCodeKnowledgeExtractor.INTERFACE_COMMENT, interfaceInfo.comment);
        node.setProperty(JavaCodeKnowledgeExtractor.INTERFACE_CONTENT, interfaceInfo.content);
    }

    public static void createMethodNode(MethodInfo methodInfo, Node node) {
        node.addLabel(Label.label(JavaCodeKnowledgeExtractor.METHOD));
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_NAME, methodInfo.name);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_RETURN, methodInfo.returnString);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_ACCESS, methodInfo.visibility);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_IS_CONSTRUCTOR, methodInfo.isConstruct);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_IS_ABSTRACT, methodInfo.isAbstract);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_IS_FINAL, methodInfo.isFinal);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_IS_STATIC, methodInfo.isStatic);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_IS_SYNCHRONIZED, methodInfo.isSynchronized);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_CONTENT, methodInfo.content);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_COMMENT, methodInfo.comment);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_BELONGTO, methodInfo.belongTo);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_PARAMS, methodInfo.paramString);
        node.setProperty(JavaCodeKnowledgeExtractor.METHOD_THROWS, String.join(", ", methodInfo.throwSet));
    }

    public static void createFieldNode(FieldInfo fieldInfo, Node node) {
        node.addLabel(Label.label(JavaCodeKnowledgeExtractor.FIELD));
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_NAME, fieldInfo.name);
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_TYPE, fieldInfo.typeString);
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_BELONGTO, fieldInfo.belongTo);
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_IS_STATIC, fieldInfo.isStatic);
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_IS_FINAL, fieldInfo.isFinal);
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_ACCESS, fieldInfo.visibility);
        node.setProperty(JavaCodeKnowledgeExtractor.FIELD_COMMENT, fieldInfo.comment);
    }

}
