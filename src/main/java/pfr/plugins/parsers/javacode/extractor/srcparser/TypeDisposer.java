/**
 * 
 */
package pfr.plugins.parsers.javacode.extractor.srcparser;


import java.util.ArrayList;
import java.util.List;

import pfr.plugins.parsers.javacode.extractor.srcparser.entity.*;










/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-6-24 下午10:55:57
 * @version 0.1 2012-12-21
 */
public class TypeDisposer {
	//private static final String[] FILTERED_COLLECTION = {};
	
	/**
	 * 去掉数组，容器等结构，返回基本类型列表，返回的基本类型可重复,如List<List>将返回两个List
	 * @param complexType
	 * @return
	 */
	public static List<String> inferSimpleType(String complexType) {
		List<String> simpleTypes = new ArrayList<String>();
		
		String temp = complexType.replace("<", "|").replace(">", "|").replace("[", "|").replace("]", "|").replace(",", "|");
		String[] typeArray = temp.split("\\|");
		for(int i = 0; i < typeArray.length; i++) {
			if(!"".equals(typeArray[i].trim())) {
				simpleTypes.add(typeArray[i].trim());
			}
		}
		return simpleTypes;
	}
	
	/**
	 * 寻找简单类型名的全名
	 * @param groupInfo
	 * @param simpleType
	 * @return
	 * 执行该方法之前，必须对JavaLangPackage初始化
	 */
	public static String findQualifiedType(GroupInfo groupInfo, String simpleType) {
		if("int".equals(simpleType) || "float".equals(simpleType) || "double".equals(simpleType) || 
				"long".equals(simpleType) || "byte".equals(simpleType) || "short".equals(simpleType) || 
				"char".equals(simpleType) || "boolean".equals(simpleType) || "void".equals(simpleType))
			return "";
		
		// TODO 未考虑".*"的情况和内部类的情况，可通过运行test.parse.java。TypeDisposerTest了解情况
		if(groupInfo.getClassInfoList() != null) {
			for(ClassInfo classInfo : groupInfo.getClassInfoList()) {
				if(simpleType.equals(classInfo.getName())) {
					return groupInfo.getPackageInfo().getName() + "." + classInfo.getName();
				}
			}
		}
		if(groupInfo.getInterfaceInfoList() != null) {
			for(InterfaceInfo interfaceInfo : groupInfo.getInterfaceInfoList()) {
				if(simpleType.equals(interfaceInfo.getName())) {
					return groupInfo.getPackageInfo().getName() + "." + interfaceInfo.getName();
				}
			}
		}
		for(String importPackage : groupInfo.getImportList()) {
			if(importPackage.endsWith(simpleType)) {
				return importPackage;
			}
		}
		List<GroupInfo> groupInfoList = groupInfo.getPackageInfo().getGroupInfoList();
		for(GroupInfo groupInfo2 : groupInfoList) {
			if(groupInfo2.getClassInfoList() != null) {
				for(ClassInfo classInfo : groupInfo2.getClassInfoList()) {
					if((groupInfo2.getPackageInfo().getName() + "." + classInfo.getName()).endsWith(simpleType)) {
						return groupInfo2.getPackageInfo().getName() + "." + classInfo.getName();
					}
				}
			}
			if(groupInfo2.getInterfaceInfoList() != null) {
				for(InterfaceInfo interfaceInfo : groupInfo2.getInterfaceInfoList()) {
					if((groupInfo2.getPackageInfo().getName() + "." + interfaceInfo.getName()).endsWith(simpleType)) {
						return groupInfo2.getPackageInfo().getName() + "." + interfaceInfo.getName();
					}
				}
			}
		}
		return JavaLangPackage.findQualifiedType(simpleType);
	}
	
	public static String findType(String variableName, ClassInfo classInfo, InterfaceInfo interfaceInfo, MethodInfo methodInfo, ElementInfoPool elementInfoPool) {
		if(variableName == null) {
			return null;
		}
		if(variableName.contains("[") && variableName.contains("]")) {
			variableName = variableName.substring(0, variableName.indexOf("[")) + variableName.substring(variableName.lastIndexOf("]") + 1);
			return findType(variableName, classInfo, interfaceInfo, methodInfo, elementInfoPool);
		}
		if(variableName.contains("<") && variableName.contains(">")) {
			variableName = variableName.substring(variableName.indexOf("<") + 1, variableName.lastIndexOf(">"));
			return findType(variableName, classInfo, interfaceInfo, methodInfo, elementInfoPool);
		}
		
		if(variableName.equals("this")) {
			if(classInfo != null)
				return classInfo.getName();
			if(interfaceInfo != null)
				return interfaceInfo.getName();
			return null;
		}
		if(methodInfo != null) {
			for(int i = 0; i < methodInfo.getVariableInfoList().size(); i++) {
				if(variableName.equals(methodInfo.getVariableInfoList().get(i).getName()))
				{
					return findType(methodInfo.getVariableInfoList().get(i).getType(), classInfo, interfaceInfo, methodInfo, elementInfoPool);
				}
				
			}
			for(int i = 0; i < methodInfo.getParameterInfoList().size(); i++) {
				if(variableName.equals(methodInfo.getParameterInfoList().get(i).getName()))
				{
					return findType(methodInfo.getParameterInfoList().get(i).getType(), classInfo, interfaceInfo, methodInfo, elementInfoPool);
				}
			}
		}
		if(classInfo != null) {
			for(int i = 0; i < classInfo.getFieldInfoList().size(); i++) {
				if(variableName.equals(classInfo.getFieldInfoList().get(i).getName())) {
					return findType(classInfo.getFieldInfoList().get(i).getType(), classInfo, interfaceInfo, methodInfo, elementInfoPool);
				}
			}		
		}
		if(interfaceInfo != null) {
			for(int i = 0; i < interfaceInfo.getFieldInfoList().size(); i++) {
				if(variableName.equals(interfaceInfo.getFieldInfoList().get(i).getName())) {
					return findType(interfaceInfo.getFieldInfoList().get(i).getType(), classInfo, interfaceInfo, methodInfo, elementInfoPool);
				}
			}
		}
		/*Iterator<String> it = elementInfoPool.classInfoMap.keySet()
				 .iterator();
		while (it.hasNext()) {
			classInfo = elementInfoPool.classInfoMap.get(it.next());
			if(variableName.equals(classInfo.getName())) {
				return classInfo.getName();
			}
		}
		it = elementInfoPool.interfaceInfoMap.keySet().iterator();
		while(it.hasNext()) {
			interfaceInfo = elementInfoPool.interfaceInfoMap.get(it.next());
			if(variableName.equals(interfaceInfo.getName())) {
				return interfaceInfo.getName();
			}
		}*/
		return variableName;
		//return null;
	}
	public static void main(String[] args) {
		//System.out.println("abc".substring(3));

	}

}
