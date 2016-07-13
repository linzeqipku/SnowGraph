package pfr.plugins.parsers.javacode.extractor.srcparser;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pfr.plugins.parsers.javacode.extractor.srcparser.entity.*;

/**
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-25 下午04:32:56
 */
/**
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class ElementInfoPool {
/*	private static final String JDBC_URL = "jdbc:mysql://192.168.4.181:3307/mikedb";
	
	private static final String JDBC_USER = "root";
	
	private static final String JDBC_PWD = "woxnsk";*/
	
	/**
	 * filteredTypeSet: 过滤的类型集，用于LDA输入的词的过滤，与java代码解析本身无关系
	 */
	//public HashSet<String> filteredTypeSet;
	
	/**
	 * projectInfo: 一个项目的信息
	 */
	public ProjectInfo projectInfo;

	/**
	 * packageInfoMap: 包含所有包的信息
	 */
	public HashMap<String, PackageInfo> packageInfoMap;
	
	/**
	 * 有个默认包，没有包声明的类归于该包下
	 */
	public PackageInfo defaultPackageInfo;

	//public PackageInfo currentPackageInfo;

	/**
	 * group对应一个java文件，groupInfoMap: 包含所有文件的信息
	 */
	public HashMap<String, GroupInfo> groupInfoMap;

	public GroupInfo currentGroupInfo;

	/**
	 * interfaceInfoMap: 包含所有接口的信息
	 */
	public HashMap<String, InterfaceInfo> interfaceInfoMap;

	//public InterfaceInfo currentInterfaceInfo;

	/**
	 * classInfoMap: 包含所有类的信息
	 */
	public HashMap<String, ClassInfo> classInfoMap;

	//public ClassInfo currentClassInfo;

	/**
	 * fieldInfoMap: 包含所有域的信息
	 */
	public HashMap<String, FieldInfo> fieldInfoMap;

	//public FieldInfo currentFieldInfo;

	/**
	 * methodInfoMap: 包含所有方法的信息
	 */
	public HashMap<String, MethodInfo> methodInfoMap;

	//public MethodInfo currentMethodInfo;

	/**
	 * parameterInfoMap: 包含所有方法参数的信息
	 */
	public HashMap<String, ParameterInfo> parameterInfoMap;

	//public ParameterInfo currentParameterInfo;
	
	public HashMap<String, Set<ClassInfo>> classInfoIndex = new HashMap<String, Set<ClassInfo>>();
	public HashMap<String, Set<InterfaceInfo>> interfaceInfoIndex = new HashMap<String, Set<InterfaceInfo>>();
	
	public ElementInfoPool() {
		packageInfoMap = new HashMap<String, PackageInfo>();
		groupInfoMap = new HashMap<String, GroupInfo>();
		interfaceInfoMap = new HashMap<String, InterfaceInfo>();
		classInfoMap = new HashMap<String, ClassInfo>();
		fieldInfoMap = new HashMap<String, FieldInfo>();
		methodInfoMap = new HashMap<String, MethodInfo>();
		parameterInfoMap = new HashMap<String, ParameterInfo>();
	}
	
	public ElementInfoPool(String projectDir) {
		this(projectDir, new File(projectDir).getName());
	}
	
	public ElementInfoPool(String projectDir, String projectName) {
		projectInfo = new ProjectInfo();
		projectInfo.setDirPath(projectDir);
		projectInfo.setName(projectName);
		packageInfoMap = new HashMap<String, PackageInfo>();
		defaultPackageInfo = new PackageInfo();
		defaultPackageInfo.setName("");
		defaultPackageInfo.setProjectInfo(projectInfo);
		if (!packageInfoMap.containsKey(defaultPackageInfo.getHashName())) {
			packageInfoMap.put(defaultPackageInfo.getHashName(),
					defaultPackageInfo);
		}
		defaultPackageInfo = packageInfoMap.get(defaultPackageInfo.getHashName());
		
		if (!projectInfo.getPackageInfoList().contains(defaultPackageInfo)) {
			projectInfo.getPackageInfoList().add(defaultPackageInfo);
		}
		groupInfoMap = new HashMap<String, GroupInfo>();
		interfaceInfoMap = new HashMap<String, InterfaceInfo>();
		classInfoMap = new HashMap<String, ClassInfo>();
		fieldInfoMap = new HashMap<String, FieldInfo>();
		methodInfoMap = new HashMap<String, MethodInfo>();
		parameterInfoMap = new HashMap<String, ParameterInfo>();
	}
	
	public ElementInfoPool(ProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
		packageInfoMap = new HashMap<String, PackageInfo>();
		defaultPackageInfo = new PackageInfo();
		defaultPackageInfo.setName("");
		defaultPackageInfo.setProjectInfo(this.projectInfo);
		if (!packageInfoMap.containsKey(defaultPackageInfo.getHashName())) {
			packageInfoMap.put(defaultPackageInfo.getHashName(),
					defaultPackageInfo);
		}
		defaultPackageInfo = packageInfoMap.get(defaultPackageInfo.getHashName());
		
		if (!this.projectInfo.getPackageInfoList().contains(defaultPackageInfo)) {
			this.projectInfo.getPackageInfoList().add(defaultPackageInfo);
		}
		groupInfoMap = new HashMap<String, GroupInfo>();
		interfaceInfoMap = new HashMap<String, InterfaceInfo>();
		classInfoMap = new HashMap<String, ClassInfo>();
		fieldInfoMap = new HashMap<String, FieldInfo>();
		methodInfoMap = new HashMap<String, MethodInfo>();
		parameterInfoMap = new HashMap<String, ParameterInfo>();
	}

	/*
	 * author: hzb
	 * 生成类和接口的索引，
	 */
	public void generateIndex() {
		for (String key : classInfoMap.keySet()) {
			ClassInfo classInfo = classInfoMap.get(key);
			String className = classInfo.getName();
			if (!classInfoIndex.containsKey(className)) {
				classInfoIndex.put(className, new HashSet<ClassInfo>());
			}
			classInfoIndex.get(className).add(classInfo);
		}
		for (String key : interfaceInfoMap.keySet()) {
			InterfaceInfo interfaceInfo = interfaceInfoMap.get(key);
			String interfaceName = interfaceInfo.getName();
			if (!interfaceInfoIndex.containsKey(interfaceName)) {
				interfaceInfoIndex.put(interfaceName, new HashSet<InterfaceInfo>());
			}
			interfaceInfoIndex.get(interfaceName).add(interfaceInfo);
		}
	}
	public Set<ClassInfo> findClassInfoByName(String name) {
		return classInfoIndex.get(name);
	}
	public Set<InterfaceInfo> findInterfaceInfoByName(String name) {
		return interfaceInfoIndex.get(name);
	}
	public void assignFilteredTypeSet(List<String> filteredSources, String jarDir) {
		/*DbEnv dbEnv = new DbEnv();
		dbEnv.setup(true);
		List<FilteredType> filteredTypeList = new ArrayList<FilteredType>();
		if(filteredSources.contains(SourcePool.ALL)) {
			filteredTypeList = DbAccess.getAll(dbEnv);
		}
		else {
			filteredTypeList = DbAccess.getBySources(filteredSources, dbEnv);
		}
		for(int i = 0; i < filteredTypeList.size(); i++) {
			filteredTypeSet.add(filteredTypeList.get(i).getSimpleName());
		}
		dbEnv.close();
		if(jarDir != null && !jarDir.equals("")) {
			JARParser jarParser = new JARParser();
			List<String> filteredStrList;
			try {
				filteredStrList = jarParser.parseClassNamesInDirectory(jarDir);
				for(int i = 0; i < filteredStrList.size(); i++) {
					filteredTypeSet.add(filteredStrList.get(i));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}
}
