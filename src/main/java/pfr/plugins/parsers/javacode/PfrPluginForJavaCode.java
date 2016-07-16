package pfr.plugins.parsers.javacode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import pfr.PFR;
import pfr.annotations.ConceptDeclaration;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;
import pfr.plugins.parsers.javacode.extractor.binparser.Analyzer;
import pfr.plugins.parsers.javacode.extractor.binparser.BinCodeInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.ByteCodePool;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinClassInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinFieldInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinInterfaceInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinMethodInfo;
import pfr.plugins.parsers.javacode.extractor.srcparser.ElementInfoPool;
import pfr.plugins.parsers.javacode.extractor.srcparser.JavaReverser;
import pfr.plugins.parsers.javacode.extractor.srcparser.entity.ClassInfo;
import pfr.plugins.parsers.javacode.extractor.srcparser.entity.InterfaceInfo;
import pfr.plugins.parsers.javacode.extractor.srcparser.entity.MethodInfo;
import pfr.plugins.parsers.javacode.extractor.srcparser.entity.ParameterInfo;

public class PfrPluginForJavaCode implements PFR
{
	
	@ConceptDeclaration public static final String CLASS="Class";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_NAME="name";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_FULLNAME="fullName";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_TEMPLATE="template";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_SUPERCLASS="superClass";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_IMPLEMENTS="implements";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_IS_ABSTRACT="isAbstract";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_IS_FINAL="isFinal";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_ACCESS="access";
	@PropertyDeclaration(parent = CLASS)public static final String CLASS_COMMENT="comment";
	
	@ConceptDeclaration public static final String INTERFACE="Interface";
	@PropertyDeclaration(parent = INTERFACE)public static final String INTERFACE_NAME="name";
	@PropertyDeclaration(parent = INTERFACE)public static final String INTERFACE_FULLNAME="fullName";
	@PropertyDeclaration(parent = INTERFACE)public static final String INTERFACE_TEMPLATE="template";
	@PropertyDeclaration(parent = INTERFACE)public static final String INTERFACE_COMMENT="comment";
	
	@ConceptDeclaration public static final String METHOD="Method";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_NAME="name";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_BELONGTO="belongTo";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_PARAMS="params";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_RETURN="rt";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_THROWS="throws";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_ACCESS="access";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_IS_ABSTRACT="isAbstract";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_IS_STATIC="isStatic";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_IS_FINAL="isFinal";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_IS_NATIVE="isNative";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_IS_SYNCHRONIZED="isSynchronized";
	@PropertyDeclaration(parent = METHOD)public static final String METHOD_COMMENT="comment";
	
	@ConceptDeclaration public static final String FIELD="Field";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_NAME="name";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_TYPE="type";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_BELONGTO="belongTo";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_IS_STATIC="isStatic";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_IS_FINAL="isFinal";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_IS_VOLATILE="isVolatile";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_IS_TRANSIENT="isTransient";
	@PropertyDeclaration(parent = FIELD)public static final String FIELD_ACCESS="access";
	
	@RelationDeclaration public static final String EXTEND="extend";
	@RelationDeclaration public static final String IMPLEMENT="implement";
	@RelationDeclaration public static final String THROW="throw";
	@RelationDeclaration public static final String PARAM="param";
	@RelationDeclaration public static final String RT="rt";
	@RelationDeclaration public static final String HAVE_METHOD="have_method";
	@RelationDeclaration public static final String HAVE_FIELD="have_field";
	@RelationDeclaration public static final String CALL_METHOD="call_method";
	@RelationDeclaration public static final String CALL_FIELD="call_field";
	@RelationDeclaration public static final String TYPE="type";

	private List<Pair<BinClassInfo, Node>> classList = new ArrayList<Pair<BinClassInfo, Node>>();
	private List<Pair<BinInterfaceInfo, Node>> interfaceList = new ArrayList<Pair<BinInterfaceInfo, Node>>();
	private List<Pair<BinMethodInfo, Node>> methodList = new ArrayList<Pair<BinMethodInfo, Node>>();
	private List<Pair<BinFieldInfo, Node>> fieldList = new ArrayList<Pair<BinFieldInfo, Node>>();

	String srcPath = "";
	String binPath = "";

	public void setSrcPath(String srcPath)
	{
		this.srcPath = srcPath;
	}

	public void setBinPath(String binPath)
	{
		this.binPath = binPath;
	}

	public void run(GraphDatabaseService db)
	{
		ByteCodePool pool = new ByteCodePool(new File(binPath));
		Analyzer analyzer = new Analyzer(pool);
		BinCodeInfo codeInfo = analyzer.buildCodeInfo();
		ElementInfoPool elementInfoPool = JavaReverser.reverse(srcPath, "");

		try (Transaction tx = db.beginTx())
		{
			// 构建图数据库中的结点
			for (BinClassInfo classInfo : codeInfo.getClassInfoList())
			{
				Node node = db.createNode();
				JavaCodeUtils.createClassNode(classInfo, node);
				classList.add(new ImmutablePair<BinClassInfo, Node>(classInfo, node));
			}
			for (BinInterfaceInfo interfaceInfo : codeInfo.getInterfaceInfoList())
			{
				Node node = db.createNode();
				JavaCodeUtils.createInterfaceNode(interfaceInfo, node);
				interfaceList.add(new ImmutablePair<BinInterfaceInfo, Node>(interfaceInfo, node));
			}
			for (BinMethodInfo methodInfo : codeInfo.getMethodInfoList())
			{
				Node node = db.createNode();
				JavaCodeUtils.createMethodNode(methodInfo, node);
				methodList.add(new ImmutablePair<BinMethodInfo, Node>(methodInfo, node));
			}
			for (BinFieldInfo fieldInfo : codeInfo.getFieldInfoList())
			{
				Node node = db.createNode();
				JavaCodeUtils.createFieldNode(fieldInfo, node);
				fieldList.add(new ImmutablePair<BinFieldInfo, Node>(fieldInfo, node));
			}
			System.out.println("结点构建完毕.");
			// 构建图数据库中的边
			buildRelationships();
			System.out.println("边构建完毕.");
			// 添加注释
			addComments(elementInfoPool);
			System.out.println("注释添加完毕.");
			tx.success();
		}

	}

	/**
	 * 构建图数据库中的边
	 */
	private void buildRelationships()
	{

		// extend
		for (Pair<BinClassInfo, Node> classSchema : classList)
		{
			Pair<BinClassInfo, Node> superClass = findClass(classSchema.getLeft().getSuperClass());
			if (superClass == null)
				continue;
			classSchema.getRight().createRelationshipTo(superClass.getRight(), RelationshipType.withName(EXTEND));
		}

		// implement
		for (Pair<BinClassInfo, Node> classSchema : classList)
		{
			List<String> interfaceNames = classSchema.getLeft().getInterfaces();
			for (String interfaceName : interfaceNames)
			{
				Pair<BinInterfaceInfo, Node> interfaceSchema = findInterface(interfaceName);
				if (interfaceSchema == null)
					continue;
				classSchema.getRight().createRelationshipTo(interfaceSchema.getRight(), RelationshipType.withName(IMPLEMENT));
			}
		}

		// throw
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			List<String> exceptions = methodSchema.getLeft().getExceptions();
			for (String exceptionName : exceptions)
			{
				Pair<BinClassInfo, Node> exception = findClass(exceptionName);
				if (exception == null)
					continue;
				methodSchema.getRight().createRelationshipTo(exception.getRight(), RelationshipType.withName(THROW));
			}
		}

		// param
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			List<String> params = methodSchema.getLeft().getParams();
			for (String paramName : params)
			{
				Set<Pair<BinClassInfo, Node>> refClasses = findRefClasses(paramName);
				for (Pair<BinClassInfo, Node> param : refClasses)
					methodSchema.getRight().createRelationshipTo(param.getRight(), RelationshipType.withName(PARAM));
				Set<Pair<BinInterfaceInfo, Node>> refInterfaces = findRefInterfaces(paramName);
				for (Pair<BinInterfaceInfo, Node> param : refInterfaces)
					methodSchema.getRight().createRelationshipTo(param.getRight(), RelationshipType.withName(PARAM));
			}
		}

		// return
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			String rt = methodSchema.getLeft().getRt();
			Set<Pair<BinClassInfo, Node>> refClasses = findRefClasses(rt);
			for (Pair<BinClassInfo, Node> param : refClasses)
				methodSchema.getRight().createRelationshipTo(param.getRight(), RelationshipType.withName(RT));
			Set<Pair<BinInterfaceInfo, Node>> refInterfaces = findRefInterfaces(rt);
			for (Pair<BinInterfaceInfo, Node> param : refInterfaces)
				methodSchema.getRight().createRelationshipTo(param.getRight(), RelationshipType.withName(RT));
		}

		// have_method
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			String belongTo = methodSchema.getLeft().getBelongTo();
			Pair<BinClassInfo, Node> owner = findClass(belongTo);
			if (owner != null)
			{
				owner.getRight().createRelationshipTo(methodSchema.getRight(), RelationshipType.withName(HAVE_METHOD));
				continue;
			}
			Pair<BinInterfaceInfo, Node> iOwner = findInterface(belongTo);
			if (iOwner != null)
			{
				iOwner.getRight().createRelationshipTo(methodSchema.getRight(), RelationshipType.withName(HAVE_METHOD));
				continue;
			}
		}

		// have_field
		for (Pair<BinFieldInfo, Node> fieldSchema : fieldList)
		{
			String belongTo = fieldSchema.getLeft().getBelongTo();
			Pair<BinClassInfo, Node> owner = findClass(belongTo);
			if (owner == null)
				continue;
			owner.getRight().createRelationshipTo(fieldSchema.getRight(), RelationshipType.withName(HAVE_FIELD));
			Pair<BinInterfaceInfo, Node> iOwner = findInterface(belongTo);
			if (iOwner == null)
				continue;
			iOwner.getRight().createRelationshipTo(fieldSchema.getRight(), RelationshipType.withName(HAVE_FIELD));
		}

		// call_method
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			HashSet<String> methodCalls = methodSchema.getLeft().getMethodCalls();
			for (String methodCall : methodCalls)
			{
				Pair<BinMethodInfo, Node> targetMethod = findMethod(methodCall);
				if (targetMethod == null)
					continue;
				methodSchema.getRight().createRelationshipTo(targetMethod.getRight(), RelationshipType.withName(CALL_METHOD));
			}
		}

		// call_field
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			HashSet<String> fieldCalls = methodSchema.getLeft().getUses();
			for (String fieldCall : fieldCalls)
			{
				Pair<BinFieldInfo, Node> targetField = findField(fieldCall);
				if (targetField == null)
					continue;
				methodSchema.getRight().createRelationshipTo(targetField.getRight(), RelationshipType.withName(CALL_FIELD));
			}
		}

		// type
		for (Pair<BinFieldInfo, Node> fieldSchema : fieldList)
		{
			String about = fieldSchema.getLeft().getType();
			Set<Pair<BinClassInfo, Node>> aboutClasses = findRefClasses(about);
			for (Pair<BinClassInfo, Node> aboutClass : aboutClasses)
				fieldSchema.getRight().createRelationshipTo(aboutClass.getRight(), RelationshipType.withName(TYPE));
			Set<Pair<BinInterfaceInfo, Node>> aboutInterfaces = findRefInterfaces(about);
			for (Pair<BinInterfaceInfo, Node> aboutInterface : aboutInterfaces)
				fieldSchema.getRight().createRelationshipTo(aboutInterface.getRight(), RelationshipType.withName(TYPE));
		}

	}

	private void addComments(ElementInfoPool elementInfoPool)
	{
		// 为类添加注释
		Map<String, Pair<BinClassInfo, Node>> classMap = new HashMap<String, Pair<BinClassInfo, Node>>();
		for (Pair<BinClassInfo, Node> classSchema : classList)
			classMap.put(classSchema.getLeft().getFullName(), classSchema);
		for (ClassInfo classInfo : elementInfoPool.classInfoMap.values())
		{
			String name = classInfo.getFullName();
			Pair<BinClassInfo, Node> classSchema = classMap.get(name);
			if (classSchema == null)
				continue;
			String comment = "";
			if (classInfo.getCommentInfo() != null && classInfo.getCommentInfo().getCommentString() != null)
				comment = classInfo.getCommentInfo().getCommentString();
			classSchema.getRight().setProperty(CLASS_COMMENT, comment);
		}
		// System.out.println("为类添加注释完毕");
		// 为接口添加注释
		Map<String, Pair<BinInterfaceInfo, Node>> interfaceMap = new HashMap<String, Pair<BinInterfaceInfo, Node>>();
		for (Pair<BinInterfaceInfo, Node> interfaceSchema : interfaceList)
			interfaceMap.put(interfaceSchema.getLeft().getFullName(), interfaceSchema);
		for (InterfaceInfo interfaceInfo : elementInfoPool.interfaceInfoMap.values())
		{
			String name = interfaceInfo.getFullName();
			Pair<BinInterfaceInfo, Node> interfaceSchema = interfaceMap.get(name);
			if (interfaceSchema == null)
				continue;
			String comment = "";
			if (interfaceInfo.getCommentInfo() != null && interfaceInfo.getCommentInfo().getCommentString() != null)
				comment = interfaceInfo.getCommentInfo().getCommentString();
			interfaceSchema.getRight().setProperty(INTERFACE_COMMENT, comment);
		}
		// System.out.println("为接口添加注释完毕");
		// 为方法添加注释
		Map<String, Set<Pair<BinMethodInfo, Node>>> methodMap = new HashMap<String, Set<Pair<BinMethodInfo, Node>>>(); // key形如"java.lang.String.toString"
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
		{
			String name = methodSchema.getLeft().getBelongTo() + "." + methodSchema.getLeft().getName();
			if (!methodMap.containsKey(name))
				methodMap.put(name, new HashSet<Pair<BinMethodInfo, Node>>());
			methodMap.get(name).add(methodSchema);
		}
		for (MethodInfo methodInfo : elementInfoPool.methodInfoMap.values())
		{
			String name = "";
			if (methodInfo.getClassInfo() != null)
				name = methodInfo.getClassInfo().getFullName();
			if (methodInfo.getInterfaceInfo() != null)
				name = methodInfo.getInterfaceInfo().getFullName();
			name += "." + methodInfo.getShortName();
			String comment = "";
			if (methodInfo.getCommentInfo() != null && methodInfo.getCommentInfo().getCommentString() != null)
				comment = methodInfo.getCommentInfo().getCommentString();
			Set<Pair<BinMethodInfo, Node>> candidateMethodSet = methodMap.get(name);
			if (candidateMethodSet == null)
				continue;
			if (candidateMethodSet.size() == 1)
				for (Pair<BinMethodInfo, Node> methodSchema : candidateMethodSet)
					methodSchema.getRight().setProperty(METHOD_COMMENT, comment);
			List<ParameterInfo> params = methodInfo.getParameterInfoList();
			List<String> types = new ArrayList<String>();
			for (ParameterInfo paramInfo : params)
				types.addAll(paramInfo.getSimpleTypes());
			if (types.size() == 0)
			{
				for (Pair<BinMethodInfo, Node> methodSchema : candidateMethodSet)
					if (methodSchema.getLeft().getFullName().endsWith("()"))
						methodSchema.getRight().setProperty(METHOD_COMMENT, comment);
				continue;
			}
			// System.out.println("Source code: "+methodInfo.getName());
			String re = "";
			for (String type : types)
				re += type + "\\W.*";
			re = re.substring(0, re.length() - 4);
			re += "[^,]*$";
			// System.out.println("         Re: "+re);
			for (Pair<BinMethodInfo, Node> methodSchema : candidateMethodSet)
			{
				// System.out.println("             "+methodSchema.getLeft().getFullName());
				if (Pattern.compile(re).matcher(methodSchema.getLeft().getFullName()).find())
				{
					methodSchema.getRight().setProperty(METHOD_COMMENT, comment);
					// System.out.println("             chosen.");
					break;
				}
			}
		}
	}

	// 根据完整的类名寻找相应的图数据库结点
	private Pair<BinClassInfo, Node> findClass(String classFullName)
	{
		for (Pair<BinClassInfo, Node> classSchema : classList)
			if (classSchema.getLeft().getFullName().equals(classFullName))
				return classSchema;
		return null;
	}

	private Set<Pair<BinClassInfo, Node>> findRefClasses(String classFullName)
	{
		Set<Pair<BinClassInfo, Node>> r = new HashSet<Pair<BinClassInfo, Node>>();
		for (Pair<BinClassInfo, Node> classSchema : classList)
		{
			String name = classSchema.getLeft().getFullName();
			if (name.equals(classFullName))
				r.add(classSchema);
			if (classFullName.matches(".*(?<![\\w\\.\\$])" + name + "(?![\\w\\.\\$]).*"))
				r.add(classSchema);
		}
		return r;
	}

	private Set<Pair<BinInterfaceInfo, Node>> findRefInterfaces(String interfaceFullName)
	{
		Set<Pair<BinInterfaceInfo, Node>> r = new HashSet<Pair<BinInterfaceInfo, Node>>();
		for (Pair<BinInterfaceInfo, Node> interfaceSchema : interfaceList)
		{
			String name = interfaceSchema.getLeft().getFullName();
			if (name.equals(interfaceFullName))
				r.add(interfaceSchema);
			if (interfaceFullName.matches(".*(?<![\\w\\.\\$])" + name + "(?![\\w\\.\\$]).*"))
				r.add(interfaceSchema);
		}
		return r;
	}

	// 根据方法的全名（例如"javassist.CtMethod.setBody(java.lang.String)"）寻找相应的图数据库结点
	private Pair<BinMethodInfo, Node> findMethod(String methodFullName)
	{
		for (Pair<BinMethodInfo, Node> methodSchema : methodList)
			if (methodSchema.getLeft().getFullName().equals(methodFullName))
				return methodSchema;
		return null;
	}
	
	// 根据域的全名寻找相应的图数据库结点
		private Pair<BinFieldInfo, Node> findField(String fieldFullName)
		{
			for (Pair<BinFieldInfo, Node> fieldSchema : fieldList)
				if (fieldSchema.getLeft().getFullName().equals(fieldFullName))
					return fieldSchema;
			return null;
		}

	// 根据完整的接口名寻找相应的图数据库结点
	private Pair<BinInterfaceInfo, Node> findInterface(String interfaceFullName)
	{
		for (Pair<BinInterfaceInfo, Node> interfaceSchema : interfaceList)
			if (interfaceSchema.getLeft().getFullName().equals(interfaceFullName))
				return interfaceSchema;
		return null;
	}

}
