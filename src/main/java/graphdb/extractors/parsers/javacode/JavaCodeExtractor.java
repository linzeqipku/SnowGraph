package graphdb.extractors.parsers.javacode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graphdb.extractors.parsers.javacode.astparser.JavaParser;
import graphdb.extractors.parsers.javacode.entity.InterfaceInfo;

import graphdb.extractors.parsers.word.corpus.Dictionary;
import graphdb.extractors.parsers.word.utils.Config;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphdb.framework.Extractor;
import graphdb.framework.annotations.EntityDeclaration;
import graphdb.framework.annotations.PropertyDeclaration;
import graphdb.framework.annotations.RelationshipDeclaration;
import graphdb.extractors.parsers.javacode.entity.ClassInfo;
import graphdb.extractors.parsers.javacode.entity.FieldInfo;
import graphdb.extractors.parsers.javacode.entity.MethodInfo;
import graphdb.extractors.parsers.javacode.astparser.ElementInfoPool;

public class JavaCodeExtractor implements Extractor {

    @EntityDeclaration
    public static final String CLASS = "Class";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_NAME = "name";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_FULLNAME = "fullName";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_SUPERCLASS = "superClass";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_IMPLEMENTS = "implements";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_IS_ABSTRACT = "isAbstract";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_IS_FINAL = "isFinal";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_ACCESS = "access";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_COMMENT = "comment";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_CONTENT = "content";
    @PropertyDeclaration(parent = CLASS)
    public static final String CLASS_CHINESE_TOKENS = "tokensCN";

    @EntityDeclaration
    public static final String INTERFACE = "Interface";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_NAME = "name";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_FULLNAME = "fullName";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_SUPERINTERFACES = "extends";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_ACCESS = "access";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_COMMENT = "comment";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_CONTENT = "content";
    @PropertyDeclaration(parent = INTERFACE)
    public static final String INTERFACE_CHINESE_TOKENS = "tokensCN";

    @EntityDeclaration
    public static final String METHOD = "Method";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_NAME = "name";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_BELONGTO = "belongTo";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_PARAMS = "params";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_RETURN = "rt";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_THROWS = "throws";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_ACCESS = "access";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_IS_ABSTRACT = "isAbstract";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_IS_STATIC = "isStatic";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_IS_FINAL = "isFinal";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_IS_SYNCHRONIZED = "isSynchronized";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_IS_CONSTRUCTOR = "isConstructor";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_COMMENT = "comment";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_CONTENT = "content";
    @PropertyDeclaration(parent = METHOD)
    public static final String METHOD_CHINESE_TOKENS = "tokensCN";

    @EntityDeclaration
    public static final String FIELD = "Field";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_NAME = "name";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_TYPE = "type";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_BELONGTO = "belongTo";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_IS_STATIC = "isStatic";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_IS_FINAL = "isFinal";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_ACCESS = "access";
    @PropertyDeclaration(parent = FIELD)
    public static final String FIELD_COMMENT = "comment";

    @PropertyDeclaration
    public static final String SIGNATURE= "signature";

    @RelationshipDeclaration
    public static final String EXTEND = "extend";
    @RelationshipDeclaration
    public static final String IMPLEMENT = "implement";
    @RelationshipDeclaration
    public static final String THROW = "throw";
    @RelationshipDeclaration
    public static final String PARAM = "param";
    @RelationshipDeclaration
    public static final String RT = "rt";
    @RelationshipDeclaration
    public static final String HAVE_METHOD = "have_method";
    @RelationshipDeclaration
    public static final String HAVE_FIELD = "have_field";
    @RelationshipDeclaration
    public static final String CALL_METHOD = "call_method";
    @RelationshipDeclaration
    public static final String CALL_FIELD = "call_field";
    @RelationshipDeclaration
    public static final String TYPE = "type";
    @RelationshipDeclaration
    public static final String VARIABLE = "variable";

    ElementInfoPool elementInfoPool = null;
    private Map<String, Pair<ClassInfo, Node>> classDecMap = new HashMap<String, Pair<ClassInfo, Node>>();
    private Map<String, Pair<InterfaceInfo, Node>> interfaceDecMap = new HashMap<String, Pair<InterfaceInfo, Node>>();
    private Map<String, Pair<MethodInfo, Node>> methodDecMap = new HashMap<String, Pair<MethodInfo, Node>>();
    private Map<String, Pair<FieldInfo, Node>> fieldDecMap = new HashMap<String, Pair<FieldInfo, Node>>();
    private Map<IMethodBinding, Pair<MethodInfo, Node>> methodBindingMap = new HashMap<IMethodBinding, Pair<MethodInfo, Node>>();

    String srcPath = "";
    GraphDatabaseService db = null;
    public static Dictionary dictionary = new Dictionary();
    
    public static void main(String[] args){
    	try {
			FileUtils.deleteDirectory(new File("E:\\test\\graph"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:\\test\\graph"));
    	JavaCodeExtractor p=new JavaCodeExtractor();
        p.setSrcPath("E:\\SnowGraphData\\lucene\\sourcecode");
    	p.run(db);
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public void run(GraphDatabaseService db) {
        this.db = db;
        try {
            System.out.println("MUST SEE THIS");
            if(Config.getProjectType().equals("Chinese")) dictionary.init();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        elementInfoPool = JavaParser.parse(srcPath);
        //System.out.println("源代码解析完毕...");
        //System.out.println("开始构建图数据库中的结点...");
        try (Transaction tx = db.beginTx()) {
            for (ClassInfo classInfo : elementInfoPool.classInfoMap.values()) {
                Node node = db.createNode();
                JavaCodeUtils.createClassNode(classInfo, node);
                classDecMap.put(classInfo.fullName, new ImmutablePair<ClassInfo, Node>(classInfo, node));
            }
            tx.success();
        }
        try (Transaction tx = db.beginTx()) {
            for (InterfaceInfo interfaceInfo : elementInfoPool.interfaceInfoMap.values()) {
                Node node = db.createNode();
                JavaCodeUtils.createInterfaceNode(interfaceInfo, node);
                interfaceDecMap.put(interfaceInfo.fullName, new ImmutablePair<InterfaceInfo, Node>(interfaceInfo, node));
            }
            tx.success();
        }
        try (Transaction tx = db.beginTx()) {
            for (MethodInfo methodInfo : elementInfoPool.methodInfoMap.values()) {
                Node node = db.createNode();
                JavaCodeUtils.createMethodNode(methodInfo, node);
                Pair<MethodInfo, Node> methodSchema = new ImmutablePair<MethodInfo, Node>(methodInfo, node);
                methodDecMap.put(methodInfo.hashName(), methodSchema);
                methodBindingMap.put(methodInfo.methodBinding, methodSchema);
            }
            tx.success();
        }
        try (Transaction tx = db.beginTx()) {
            for (FieldInfo fieldInfo : elementInfoPool.fieldInfoMap.values()) {
                Node node = db.createNode();
                JavaCodeUtils.createFieldNode(fieldInfo, node);
                fieldDecMap.put(fieldInfo.hashName(), new ImmutablePair<FieldInfo, Node>(fieldInfo, node));
            }
            tx.success();
        }
        //System.out.println("结点构建完毕...");
        //System.out.println("开始构建图数据库中的边...");
        buildRelationships();
        //System.out.println("边构建完毕.");

    }

    /**
     * 构建图数据库中的边
     */
    private void buildRelationships() {

        // extend
        try (Transaction tx = db.beginTx()) {
            for (Pair<ClassInfo, Node> classSchema : classDecMap.values()) {
                Pair<ClassInfo, Node> superClass = classDecMap.get(classSchema.getLeft().superClassType);
                if (superClass != null)
                    classSchema.getRight().createRelationshipTo(superClass.getRight(), RelationshipType.withName(EXTEND));
            }
            tx.success();
        }

        // implement
        try (Transaction tx = db.beginTx()) {
            for (Pair<ClassInfo, Node> classSchema : classDecMap.values()) {
                List<String> interfaceNames = classSchema.getLeft().superInterfaceTypeList;
                for (String interfaceName : interfaceNames) {
                    Pair<InterfaceInfo, Node> interfaceSchema = interfaceDecMap.get(interfaceName);
                    if (interfaceSchema != null)
                        classSchema.getRight().createRelationshipTo(interfaceSchema.getRight(), RelationshipType.withName(IMPLEMENT));
                }
            }
            tx.success();
        }

        // throw
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                Set<String> exceptions = methodSchema.getLeft().throwSet;
                for (String exceptionName : exceptions) {
                    Pair<ClassInfo, Node> exception = classDecMap.get(exceptionName);
                    if (exception != null)
                        methodSchema.getRight().createRelationshipTo(exception.getRight(), RelationshipType.withName(THROW));
                    Pair<InterfaceInfo, Node> iException = interfaceDecMap.get(exceptionName);
                    if (iException != null)
                        methodSchema.getRight().createRelationshipTo(iException.getRight(), RelationshipType.withName(THROW));
                }
            }
            tx.success();
        }

        //params
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                Set<String> params = methodSchema.getLeft().paramTypes;
                for (String param : params) {
                    Pair<ClassInfo, Node> paramSchema = classDecMap.get(param);
                    if (paramSchema != null)
                        methodSchema.getRight().createRelationshipTo(paramSchema.getRight(), RelationshipType.withName(PARAM));
                    Pair<InterfaceInfo, Node> iParamSchema = interfaceDecMap.get(param);
                    if (iParamSchema != null)
                        methodSchema.getRight().createRelationshipTo(iParamSchema.getRight(), RelationshipType.withName(PARAM));
                }
            }
            tx.success();
        }

        // return
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                Set<String> rts = methodSchema.getLeft().returnTypes;
                for (String rt : rts) {
                    Pair<ClassInfo, Node> rtSchema = classDecMap.get(rt);
                    if (rtSchema != null)
                        methodSchema.getRight().createRelationshipTo(rtSchema.getRight(), RelationshipType.withName(RT));
                    Pair<InterfaceInfo, Node> irtSchema = interfaceDecMap.get(rt);
                    if (irtSchema != null)
                        methodSchema.getRight().createRelationshipTo(irtSchema.getRight(), RelationshipType.withName(RT));
                }
            }
            tx.success();
        }

        // have_method
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                String belongTo = methodSchema.getLeft().belongTo;
                Pair<ClassInfo, Node> owner = classDecMap.get(belongTo);
                if (owner != null)
                    owner.getRight().createRelationshipTo(methodSchema.getRight(), RelationshipType.withName(HAVE_METHOD));
                Pair<InterfaceInfo, Node> iOwner = interfaceDecMap.get(belongTo);
                if (iOwner != null)
                    iOwner.getRight().createRelationshipTo(methodSchema.getRight(), RelationshipType.withName(HAVE_METHOD));
            }
            tx.success();
        }

        // have_field
        try (Transaction tx = db.beginTx()) {
            for (Pair<FieldInfo, Node> fieldSchema : fieldDecMap.values()) {
                String belongTo = fieldSchema.getLeft().belongTo;
                Pair<ClassInfo, Node> owner = classDecMap.get(belongTo);
                if (owner != null)
                    owner.getRight().createRelationshipTo(fieldSchema.getRight(), RelationshipType.withName(HAVE_FIELD));
                Pair<InterfaceInfo, Node> iOwner = interfaceDecMap.get(belongTo);
                if (iOwner != null)
                    iOwner.getRight().createRelationshipTo(fieldSchema.getRight(), RelationshipType.withName(HAVE_FIELD));
            }
            tx.success();
        }

        // call_method
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                Set<IMethodBinding> methodCalls = methodSchema.getLeft().methodCalls;
                for (IMethodBinding methodCall : methodCalls) {
                    Pair<MethodInfo, Node> targetMethod = methodBindingMap.get(methodCall);
                    if (targetMethod != null)
                        methodSchema.getRight().createRelationshipTo(targetMethod.getRight(), RelationshipType.withName(CALL_METHOD));
                }
            }
            tx.success();
        }

        // call_field
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                Set<String> fieldCalls = methodSchema.getLeft().fieldUsesSet;
                for (String fieldCall : fieldCalls) {
                    Pair<FieldInfo, Node> targetField = fieldDecMap.get(fieldCall);
                    if (targetField != null)
                        methodSchema.getRight().createRelationshipTo(targetField.getRight(), RelationshipType.withName(CALL_FIELD));
                }
            }
            tx.success();
        }

        // type
        try (Transaction tx = db.beginTx()) {
            for (Pair<FieldInfo, Node> fieldSchema : fieldDecMap.values()) {
                Set<String> types = fieldSchema.getLeft().types;
                for (String type : types) {
                    Pair<ClassInfo, Node> typeSchema = classDecMap.get(type);
                    if (typeSchema != null)
                        fieldSchema.getRight().createRelationshipTo(typeSchema.getRight(), RelationshipType.withName(TYPE));
                    Pair<InterfaceInfo, Node> iTypeSchema = interfaceDecMap.get(type);
                    if (iTypeSchema != null)
                        fieldSchema.getRight().createRelationshipTo(iTypeSchema.getRight(), RelationshipType.withName(TYPE));
                }
            }
            tx.success();
        }

        // variable
        try (Transaction tx = db.beginTx()) {
            for (Pair<MethodInfo, Node> methodSchema : methodDecMap.values()) {
                Set<String> types = methodSchema.getLeft().variableTypes;
                for (String type : types) {
                    Pair<ClassInfo, Node> typeSchema = classDecMap.get(type);
                    if (typeSchema != null)
                        methodSchema.getRight().createRelationshipTo(typeSchema.getRight(), RelationshipType.withName(VARIABLE));
                    Pair<InterfaceInfo, Node> iTypeSchema = interfaceDecMap.get(type);
                    if (iTypeSchema != null)
                        methodSchema.getRight().createRelationshipTo(iTypeSchema.getRight(), RelationshipType.withName(VARIABLE));
                }
            }
            tx.success();
        }

    }

}
