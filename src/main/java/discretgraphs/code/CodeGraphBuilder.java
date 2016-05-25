package discretgraphs.code;

import graphmodel.ManageElements;
import graphmodel.entity.code.ClassSchema;
import graphmodel.entity.code.FieldSchema;
import graphmodel.entity.code.InterfaceSchema;
import graphmodel.entity.code.MethodSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import discretgraphs.GraphBuilder;
import discretgraphs.code.extractor.binparser.Analyzer;
import discretgraphs.code.extractor.binparser.BinCodeInfo;
import discretgraphs.code.extractor.binparser.ByteCodePool;
import discretgraphs.code.extractor.binparser.entity.BinClassInfo;
import discretgraphs.code.extractor.binparser.entity.BinFieldInfo;
import discretgraphs.code.extractor.binparser.entity.BinInterfaceInfo;
import discretgraphs.code.extractor.binparser.entity.BinMethodInfo;
import discretgraphs.code.extractor.srcparser.ElementInfoPool;
import discretgraphs.code.extractor.srcparser.JavaReverser;
import discretgraphs.code.extractor.srcparser.entity.ClassInfo;
import discretgraphs.code.extractor.srcparser.entity.InterfaceInfo;
import discretgraphs.code.extractor.srcparser.entity.MethodInfo;
import discretgraphs.code.extractor.srcparser.entity.ParameterInfo;

public class CodeGraphBuilder extends GraphBuilder
{

	private List<ClassSchema> classList = new ArrayList<ClassSchema>();
	private List<InterfaceSchema> interfaceList = new ArrayList<InterfaceSchema>();
	private List<MethodSchema> methodList = new ArrayList<MethodSchema>();
	private List<FieldSchema> fieldList = new ArrayList<FieldSchema>();

	String srcPath = "";
	String binPath = "";

	public CodeGraphBuilder(String dbPath, String srcPath, String binPath)
	{
		super(dbPath);
		this.srcPath = srcPath;
		this.binPath = binPath;
		name = "CodeGraphBuilder";
	}

	public void run()
	{
		ByteCodePool pool = new ByteCodePool(new File(binPath));
		Analyzer analyzer = new Analyzer(pool);
		BinCodeInfo codeInfo = analyzer.buildCodeInfo();
		ElementInfoPool elementInfoPool = JavaReverser.reverse(srcPath, "");

		// delete existed database before creating a new one
		File dbFile = new File(dbPath);
		if (dbFile.exists())
		{
			dbFile.delete();
		}
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);

		try (Transaction tx = db.beginTx())
		{
			// 构建图数据库中的结点
			for (BinClassInfo classInfo : codeInfo.getClassInfoList())
			{
				Node node = db.createNode();
				classList.add(new ClassSchema(classInfo, node));
			}
			for (BinInterfaceInfo interfaceInfo : codeInfo.getInterfaceInfoList())
			{
				Node node = db.createNode();
				interfaceList.add(new InterfaceSchema(interfaceInfo, node));
			}
			for (BinMethodInfo methodInfo : codeInfo.getMethodInfoList())
			{
				Node node = db.createNode();
				methodList.add(new MethodSchema(methodInfo, node));
			}
			for (BinFieldInfo fieldInfo : codeInfo.getFieldInfoList())
			{
				Node node = db.createNode();
				fieldList.add(new FieldSchema(fieldInfo, node));
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

		db.shutdown();

	}

	/**
	 * 构建图数据库中的边
	 */
	private void buildRelationships()
	{

		// extend
		for (ClassSchema classSchema : classList)
		{
			ClassSchema superClass = findClass(classSchema.getClassInfo().getSuperClass());
			if (superClass == null)
				continue;
			classSchema.getNode().createRelationshipTo(superClass.getNode(), ManageElements.RelTypes.EXTEND);
		}

		// implement
		for (ClassSchema classSchema : classList)
		{
			List<String> interfaceNames = classSchema.getClassInfo().getInterfaces();
			for (String interfaceName : interfaceNames)
			{
				InterfaceSchema interfaceSchema = findInterface(interfaceName);
				if (interfaceSchema == null)
					continue;
				classSchema.getNode().createRelationshipTo(interfaceSchema.getNode(), ManageElements.RelTypes.IMPLEMENT);
			}
		}

		// throw
		for (MethodSchema methodSchema : methodList)
		{
			List<String> exceptions = methodSchema.getMethodInfo().getExceptions();
			for (String exceptionName : exceptions)
			{
				ClassSchema exception = findClass(exceptionName);
				if (exception == null)
					continue;
				methodSchema.getNode().createRelationshipTo(exception.getNode(), ManageElements.RelTypes.THROW);
			}
		}

		// param
		for (MethodSchema methodSchema : methodList)
		{
			List<String> params = methodSchema.getMethodInfo().getParams();
			for (String paramName : params)
			{
				HashSet<ClassSchema> refClasses = findRefClasses(paramName);
				for (ClassSchema param : refClasses)
					methodSchema.getNode().createRelationshipTo(param.getNode(), ManageElements.RelTypes.PARAM);
				HashSet<InterfaceSchema> refInterfaces = findRefInterfaces(paramName);
				for (InterfaceSchema param : refInterfaces)
					methodSchema.getNode().createRelationshipTo(param.getNode(), ManageElements.RelTypes.PARAM);
			}
		}

		// return
		for (MethodSchema methodSchema : methodList)
		{
			String rt = methodSchema.getMethodInfo().getRt();
			HashSet<ClassSchema> refClasses = findRefClasses(rt);
			for (ClassSchema param : refClasses)
				methodSchema.getNode().createRelationshipTo(param.getNode(), ManageElements.RelTypes.RT);
			HashSet<InterfaceSchema> refInterfaces = findRefInterfaces(rt);
			for (InterfaceSchema param : refInterfaces)
				methodSchema.getNode().createRelationshipTo(param.getNode(), ManageElements.RelTypes.RT);
		}

		// have_method
		for (MethodSchema methodSchema : methodList)
		{
			String belongTo = methodSchema.getMethodInfo().getBelongTo();
			ClassSchema owner = findClass(belongTo);
			if (owner != null)
			{
				owner.getNode().createRelationshipTo(methodSchema.getNode(), ManageElements.RelTypes.HAVE_METHOD);
				continue;
			}
			InterfaceSchema iOwner = findInterface(belongTo);
			if (iOwner != null)
			{
				iOwner.getNode().createRelationshipTo(methodSchema.getNode(), ManageElements.RelTypes.HAVE_METHOD);
				continue;
			}
		}

		// have_field
		for (FieldSchema fieldSchema : fieldList)
		{
			String belongTo = fieldSchema.getFieldInfo().getBelongTo();
			ClassSchema owner = findClass(belongTo);
			if (owner == null)
				continue;
			owner.getNode().createRelationshipTo(fieldSchema.getNode(), ManageElements.RelTypes.HAVE_FIELD);
			InterfaceSchema iOwner = findInterface(belongTo);
			if (iOwner == null)
				continue;
			iOwner.getNode().createRelationshipTo(fieldSchema.getNode(), ManageElements.RelTypes.HAVE_FIELD);
		}

		// call_method
		for (MethodSchema methodSchema : methodList)
		{
			HashSet<String> methodCalls = methodSchema.getMethodInfo().getMethodCalls();
			for (String methodCall : methodCalls)
			{
				MethodSchema targetMethod = findMethod(methodCall);
				if (targetMethod == null)
					continue;
				methodSchema.getNode().createRelationshipTo(targetMethod.getNode(), ManageElements.RelTypes.CALL_METHOD);
			}
		}

		// call_field
		for (MethodSchema methodSchema : methodList)
		{
			HashSet<String> fieldCalls = methodSchema.getMethodInfo().getUses();
			for (String fieldCall : fieldCalls)
			{
				FieldSchema targetField = findField(fieldCall);
				if (targetField == null)
					continue;
				methodSchema.getNode().createRelationshipTo(targetField.getNode(), ManageElements.RelTypes.CALL_FIELD);
			}
		}

		// type
		for (FieldSchema fieldSchema : fieldList)
		{
			String about = fieldSchema.getFieldInfo().getType();
			HashSet<ClassSchema> aboutClasses = findRefClasses(about);
			for (ClassSchema aboutClass : aboutClasses)
				fieldSchema.getNode().createRelationshipTo(aboutClass.getNode(), ManageElements.RelTypes.TYPE);
			HashSet<InterfaceSchema> aboutInterfaces = findRefInterfaces(about);
			for (InterfaceSchema aboutInterface : aboutInterfaces)
				fieldSchema.getNode().createRelationshipTo(aboutInterface.getNode(), ManageElements.RelTypes.TYPE);
		}

	}

	private void addComments(ElementInfoPool elementInfoPool)
	{
		// 为类添加注释
		Map<String, ClassSchema> classMap = new HashMap<String, ClassSchema>();
		for (ClassSchema classSchema : classList)
			classMap.put(classSchema.getClassInfo().getFullName(), classSchema);
		for (ClassInfo classInfo : elementInfoPool.classInfoMap.values())
		{
			String name = classInfo.getFullName();
			ClassSchema classSchema = classMap.get(name);
			if (classSchema == null)
				continue;
			String comment = "";
			if (classInfo.getCommentInfo() != null && classInfo.getCommentInfo().getCommentString() != null)
				comment = classInfo.getCommentInfo().getCommentString();
			classSchema.setComment(comment);
		}
		// System.out.println("为类添加注释完毕");
		// 为接口添加注释
		Map<String, InterfaceSchema> interfaceMap = new HashMap<String, InterfaceSchema>();
		for (InterfaceSchema interfaceSchema : interfaceList)
			interfaceMap.put(interfaceSchema.getInterfaceInfo().getFullName(), interfaceSchema);
		for (InterfaceInfo interfaceInfo : elementInfoPool.interfaceInfoMap.values())
		{
			String name = interfaceInfo.getFullName();
			InterfaceSchema interfaceSchema = interfaceMap.get(name);
			if (interfaceSchema == null)
				continue;
			String comment = "";
			if (interfaceInfo.getCommentInfo() != null && interfaceInfo.getCommentInfo().getCommentString() != null)
				comment = interfaceInfo.getCommentInfo().getCommentString();
			interfaceSchema.setComment(comment);
		}
		// System.out.println("为接口添加注释完毕");
		// 为方法添加注释
		Map<String, Set<MethodSchema>> methodMap = new HashMap<String, Set<MethodSchema>>(); // key形如"java.lang.String.toString"
		for (MethodSchema methodSchema : methodList)
		{
			String name = methodSchema.getMethodInfo().getBelongTo() + "." + methodSchema.getMethodInfo().getName();
			if (!methodMap.containsKey(name))
				methodMap.put(name, new HashSet<MethodSchema>());
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
			Set<MethodSchema> candidateMethodSet = methodMap.get(name);
			if (candidateMethodSet == null)
				continue;
			if (candidateMethodSet.size() == 1)
				for (MethodSchema methodSchema : candidateMethodSet)
					methodSchema.setComment(comment);
			List<ParameterInfo> params = methodInfo.getParameterInfoList();
			List<String> types = new ArrayList<String>();
			for (ParameterInfo paramInfo : params)
				types.addAll(paramInfo.getSimpleTypes());
			if (types.size() == 0)
			{
				for (MethodSchema methodSchema : candidateMethodSet)
					if (methodSchema.getMethodInfo().getFullName().endsWith("()"))
						methodSchema.setComment(comment);
				continue;
			}
			// System.out.println("Source code: "+methodInfo.getName());
			String re = "";
			for (String type : types)
				re += type + "\\W.*";
			re = re.substring(0, re.length() - 4);
			re += "[^,]*$";
			// System.out.println("         Re: "+re);
			for (MethodSchema methodSchema : candidateMethodSet)
			{
				// System.out.println("             "+methodSchema.getMethodInfo().getFullName());
				if (Pattern.compile(re).matcher(methodSchema.getMethodInfo().getFullName()).find())
				{
					methodSchema.setComment(comment);
					// System.out.println("             chosen.");
					break;
				}
			}
		}
	}

	// 根据完整的类名寻找相应的图数据库结点
	private ClassSchema findClass(String classFullName)
	{
		for (ClassSchema classSchema : classList)
			if (classSchema.getClassInfo().getFullName().equals(classFullName))
				return classSchema;
		return null;
	}

	private HashSet<ClassSchema> findRefClasses(String classFullName)
	{
		HashSet<ClassSchema> r = new HashSet<ClassSchema>();
		for (ClassSchema classSchema : classList)
		{
			String name = classSchema.getClassInfo().getFullName();
			if (name.equals(classFullName))
				r.add(classSchema);
			if (classFullName.matches(".*(?<![\\w\\.\\$])" + name + "(?![\\w\\.\\$]).*"))
				r.add(classSchema);
		}
		return r;
	}

	private HashSet<InterfaceSchema> findRefInterfaces(String interfaceFullName)
	{
		HashSet<InterfaceSchema> r = new HashSet<InterfaceSchema>();
		for (InterfaceSchema interfaceSchema : interfaceList)
		{
			String name = interfaceSchema.getInterfaceInfo().getFullName();
			if (name.equals(interfaceFullName))
				r.add(interfaceSchema);
			if (interfaceFullName.matches(".*(?<![\\w\\.\\$])" + name + "(?![\\w\\.\\$]).*"))
				r.add(interfaceSchema);
		}
		return r;
	}

	// 根据方法的全名（例如"javassist.CtMethod.setBody(java.lang.String)"）寻找相应的图数据库结点
	private MethodSchema findMethod(String methodFullName)
	{
		for (MethodSchema methodSchema : methodList)
			if (methodSchema.getMethodInfo().getFullName().equals(methodFullName))
				return methodSchema;
		return null;
	}
	
	// 根据域的全名寻找相应的图数据库结点
		private FieldSchema findField(String fieldFullName)
		{
			for (FieldSchema fieldSchema : fieldList)
				if (fieldSchema.getFieldInfo().getFullName().equals(fieldFullName))
					return fieldSchema;
			return null;
		}

	// 根据完整的接口名寻找相应的图数据库结点
	private InterfaceSchema findInterface(String interfaceFullName)
	{
		for (InterfaceSchema interfaceSchema : interfaceList)
			if (interfaceSchema.getInterfaceInfo().getFullName().equals(interfaceFullName))
				return interfaceSchema;
		return null;
	}

}
