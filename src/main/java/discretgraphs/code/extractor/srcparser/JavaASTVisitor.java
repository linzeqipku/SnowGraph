/** 
 * File-Name:JavaASTVisitor.java
 *
 * Created on 2012-3-23 下午8:21:16
 * 
 * @author: Neo (neolimeng@gmail.com)
 * 			Co Written by: Jin Jing
 * Software Engineering Institute, Peking University, China
 * 
 * Copyright (c) 2009, Peking University
 * 
 *
 */
package discretgraphs.code.extractor.srcparser;

import java.io.File;
import java.io.IOException;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import discretgraphs.code.extractor.srcparser.entity.ClassInfo;
import discretgraphs.code.extractor.srcparser.entity.CommentInfo;
import discretgraphs.code.extractor.srcparser.entity.FieldInfo;
import discretgraphs.code.extractor.srcparser.entity.GroupInfo;
import discretgraphs.code.extractor.srcparser.entity.InterfaceInfo;
import discretgraphs.code.extractor.srcparser.entity.MethodInfo;
import discretgraphs.code.extractor.srcparser.entity.PackageInfo;
import discretgraphs.code.extractor.srcparser.entity.ParameterInfo;

/**
 * Description: Visit each node needed to parse, and perform some arbitrary
 * operation. <br/>
 * 
 * {@link http://help.eclipse.org/indigo/index.jsp}
 * 
 * @author: Neo (neolimeng@gmail.com) Software Engineering Institute, Peking
 *          University, China
 * 
 *          Jin Jing
 * 
 * @version 0.1 2012-12-15
 */
public class JavaASTVisitor extends ASTVisitor {
	
	/**
	 * 在用visitor模式访问个语言单元结点之前，一定记得先对elementInfoPool和parseMethod赋值
	 */
	private ElementInfoPool elementInfoPool;
	
	private boolean parseMethod;

	/**
	 * 访问包声明结点
	 */
	@Override
	public boolean visit(PackageDeclaration node) {
		PackageInfo packageInfo = new PackageInfo();
		packageInfo.setName(node.getName().getFullyQualifiedName());
		packageInfo.setProjectInfo(elementInfoPool.projectInfo);
		CommentInfo commentInfo;
		if(node.getJavadoc() != null) {
			int javaDocStartPos = node.getJavadoc().getStartPosition();
			commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
		}
		else {
			commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(node.getStartPosition());
		}
		if (commentInfo != null) {
		//	System.out.println("package has comment");
			packageInfo.setCommentInfo(commentInfo);
			packageInfo.setComment(commentInfo.getCommentString());
		}

		if (packageInfo.getName() != null && packageInfo.getName() != "" && !elementInfoPool.packageInfoMap.containsKey(packageInfo
				.getHashName())) {
			elementInfoPool.packageInfoMap.put(packageInfo.getHashName(),
					packageInfo);
		}
		packageInfo = elementInfoPool.packageInfoMap.get(packageInfo
				.getHashName());

		if (!packageInfo.getProjectInfo().getPackageInfoList().contains(
				packageInfo)) {
			packageInfo.getProjectInfo().getPackageInfoList().add(packageInfo);
		}

		/**
		 * 如果有包声明，那么被解析的文件所属包不是defaultPackage，而是声明的包
		 */
		GroupInfo groupInfo = elementInfoPool.currentGroupInfo;
		groupInfo.getPackageInfo().getGroupInfoList().remove(groupInfo);

		elementInfoPool.groupInfoMap.remove(groupInfo.getHashName());

		groupInfo.setPackageInfo(packageInfo);
		if (!elementInfoPool.groupInfoMap.containsKey(groupInfo.getHashName())) {
			elementInfoPool.groupInfoMap
					.put(groupInfo.getHashName(), groupInfo);
		}
		elementInfoPool.currentGroupInfo = elementInfoPool.groupInfoMap
				.get(groupInfo.getHashName());

		if (!elementInfoPool.currentGroupInfo.getPackageInfo()
				.getGroupInfoList().contains(elementInfoPool.currentGroupInfo)) {
			elementInfoPool.currentGroupInfo.getPackageInfo()
					.getGroupInfoList().add(elementInfoPool.currentGroupInfo);
		}

		return true;
	}

	/**
	 * 访问import结点，对查找对象所属完整类型有帮助
	 */
	@Override
	public boolean visit(ImportDeclaration node) {
		if (!elementInfoPool.currentGroupInfo.getImportList().contains(
				node.getName().getFullyQualifiedName())) {
			elementInfoPool.currentGroupInfo.getImportList().add(
					node.getName().getFullyQualifiedName());
		}
		return true;
	}

	/**
	 * 访问类型声明结点，剩余所有解析都在此结点下直接完成
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TypeDeclaration node) {
		/**
		 * 接口声明
		 */
		if (node.isInterface()) {
			InterfaceInfo interfaceInfo = new InterfaceInfo();
			interfaceInfo.setName(node.getName().getFullyQualifiedName());
			interfaceInfo.setFilePath(elementInfoPool.currentGroupInfo.getPath());
			GroupInfo groupInfo = elementInfoPool.currentGroupInfo;
			PackageInfo packageInfo = groupInfo.getPackageInfo();
			if (packageInfo != null) {
				interfaceInfo.setFullName(packageInfo.getName() + "." + interfaceInfo.getName());
			}
			{
				String javaDoc = null;
				if(node.getJavadoc() != null) {
					javaDoc = node.getJavadoc().toString();
				}
				
				CommentInfo commentInfo;
				if(javaDoc != null) {
					int javaDocStartPos = node.getJavadoc().getStartPosition();
					commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
				}
				else {
					commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(node.getStartPosition());
				}
				interfaceInfo.setCommentInfo(commentInfo);
				if (commentInfo != null) {
					interfaceInfo.setComment(commentInfo.getCommentString());
				}
			}
			List<String> list = node.modifiers();
			for (int i = 0; i < list.size(); i++) {
				if ("public".equals(list.get(i))) {
					interfaceInfo.setVisibility(list.get(i));
				}
			}
			List<Type> simpleTypes = node.superInterfaceTypes();
			for(int i = 0; i < simpleTypes.size(); i++)
			{
				interfaceInfo.getSuperInterfaceTypeList().add(simpleTypes.get(i).toString());
			}
			interfaceInfo.setGroupInfo(elementInfoPool.currentGroupInfo);

			if (!elementInfoPool.interfaceInfoMap.containsKey(interfaceInfo
					.getHashName())) {
				elementInfoPool.interfaceInfoMap.put(interfaceInfo
						.getHashName(), interfaceInfo);
			}
			interfaceInfo = elementInfoPool.interfaceInfoMap.get(interfaceInfo
					.getHashName());

			if (!interfaceInfo.getGroupInfo().getInterfaceInfoList().contains(
					interfaceInfo)) {
				interfaceInfo.getGroupInfo().getInterfaceInfoList().add(
						interfaceInfo);
			}

			/**
			 * 域声明
			 */
			FieldDeclaration[] fieldDeclarations = node.getFields();
			if (fieldDeclarations != null) {
				for (int i = 0; i < fieldDeclarations.length; i++) {
					String javaDoc = null;
					if(fieldDeclarations[i].getJavadoc() != null) {
						javaDoc = fieldDeclarations[i].getJavadoc().toString();
					}
					CommentInfo commentInfo;
					if(javaDoc != null) {
						int javaDocStartPos = fieldDeclarations[i].getJavadoc().getStartPosition();
						commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
						//methodInfo.setComment(javaDoc);
						
					}
					else {
						commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(fieldDeclarations[i].getStartPosition());
					}
					
					
					String type = fieldDeclarations[i].getType().toString();
					
					//将例如List<String>的复杂类型拆分为List和String
					List<String> simpleTypeList = TypeDisposer.inferSimpleType(type);

//					System.out.println("FIELD!!!!!!");
//					for (Object modifiObject : fieldDeclarations[i].modifiers()) {
//						Modifier modify = (Modifier) modifiObject;
//						System.out.println(modify + "-");
//					}
//					System.out.println(fieldDeclarations[i].getType());
//					for (Object object : fieldDeclarations[i].fragments()) {
//						VariableDeclarationFragment frag = (VariableDeclarationFragment) object;
//						System.out.println("[FIELD_NAME:]" + frag.getName());
//					}
					
					List<VariableDeclaration> list2 = fieldDeclarations[i]
							.fragments();
					for (int j = 0; j < list2.size(); j++) {
						FieldInfo fieldInfo = new FieldInfo();
						fieldInfo.setName(list2.get(j).getName()
								.getFullyQualifiedName());
						if (commentInfo != null) {
//							System.out.println("has comment");
//							System.out.println(fieldInfo.getName());
//							System.out.println(commentInfo.getCommentString());
							
							fieldInfo.setCommentInfo(commentInfo);
							fieldInfo.setComment(commentInfo.getCommentString());
						}
						
						//System.out.println("%%%  " + list2.get(j));
						//System.out.println("^^^  " + list2.get(j).getName()
						//		.getFullyQualifiedName());
						fieldInfo.setType(type);
						fieldInfo.setSimpleTypes(simpleTypeList);
						fieldInfo.setVisibility("public");
						fieldInfo.setFinal(true);
						fieldInfo.setStatic(true);
						fieldInfo.setInterfaceInfo(interfaceInfo);
						if (!elementInfoPool.fieldInfoMap.containsKey(fieldInfo
								.getHashName())) {
							elementInfoPool.fieldInfoMap.put(fieldInfo
									.getHashName(), fieldInfo);
						}
						fieldInfo = elementInfoPool.fieldInfoMap.get(fieldInfo
								.getHashName());

						if (!fieldInfo.getInterfaceInfo().getFieldInfoList()
								.contains(fieldInfo)) {
							fieldInfo.getInterfaceInfo().getFieldInfoList()
									.add(fieldInfo);
						}
					}
				}
			}

			String fileContent = null;
			
			try {
				fileContent = FileUtils.readFileToString(new File(elementInfoPool.currentGroupInfo.getPath()));
			} catch (IOException e) {
				fileContent = null;
				e.printStackTrace();
			}
			
			/**
			 * 方法声明
			 */
			MethodDeclaration[] methodDeclarations = node.getMethods();
			if (methodDeclarations != null) {
				for (int i = 0; i < methodDeclarations.length; i++) {
					String javaDoc = null;
					if(methodDeclarations[i].getJavadoc()!= null) {
						javaDoc = methodDeclarations[i].getJavadoc().toString();
					}
					
					MethodInfo methodInfo = new MethodInfo();
					methodInfo.setShortName(methodDeclarations[i].getName()
							.getFullyQualifiedName());
					CommentInfo commentInfo;
					if(javaDoc != null) {
						int javaDocStartPos = methodDeclarations[i].getJavadoc().getStartPosition();
						commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
						//methodInfo.setComment(javaDoc);
						
					}
					else {
						commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(methodDeclarations[i].getStartPosition());
					}
					if (commentInfo != null) {
						//System.out.println(methodInfo.getShortName());
						//System.out.println(commentInfo.getCommentString());
						methodInfo.setCommentInfo(commentInfo);
						methodInfo.setComment(commentInfo.getCommentString());
					}
					//System.out.println("Method: " + methodInfo.getShortName());
					//System.out.println("Comment: " + methodInfo.getComment());
					methodInfo.setName(methodInfo.getShortName() + "(");
					List<SingleVariableDeclaration> list2 = methodDeclarations[i]
							.parameters();
					if (list2 != null) {
						for (int j = 0; j < list2.size(); j++) {
							String parameterName = list2.get(j).getName()
									.getFullyQualifiedName();
							String parameterType = list2.get(j).getType()
									.toString();
							
							boolean isParameterFinal = false;
							list = list2.get(j).modifiers();
							if (list != null && list.contains("final")) {
								isParameterFinal = true;
							}
							if (isParameterFinal) {
								methodInfo.setName(methodInfo.getName()
										+ "final " + parameterType + " "
										+ parameterName);
								if (j != list2.size() - 1) {
									methodInfo.setName(methodInfo.getName()
											+ ", ");
								}
							} else {
								methodInfo.setName(methodInfo.getName()
										+ parameterType + " " + parameterName);
								if (j != list2.size() - 1) {
									methodInfo.setName(methodInfo.getName()
											+ ", ");
								}
							}

						}
					}
					methodInfo.setName(methodInfo.getName() + ")");

					methodInfo.setConstruct(methodDeclarations[i]
							.isConstructor());
					if (methodDeclarations[i].getReturnType2() != null) {
						methodInfo.setReturnType(methodDeclarations[i]
								.getReturnType2().toString());
						List<String> simpleTypeList = TypeDisposer.inferSimpleType(methodInfo.getReturnType());
						methodInfo.setSimpleTypes(simpleTypeList);
					}
					
					if(fileContent != null) {
						methodInfo.setMethodContent(new String(fileContent.substring(methodDeclarations[i].getStartPosition(), methodDeclarations[i].getStartPosition() + methodDeclarations[i].getLength())));
					}
							
					methodInfo.setVisibility("public");
					methodInfo.setFinal(false);
					methodInfo.setAbstract(true);
					methodInfo.setStatic(false);
					methodInfo.setSynchronized(false);
					methodInfo.setInterfaceInfo(interfaceInfo);
					if (!elementInfoPool.methodInfoMap.containsKey(methodInfo
							.getHashName())) {
						elementInfoPool.methodInfoMap.put(methodInfo
								.getHashName(), methodInfo);
					}
					methodInfo = elementInfoPool.methodInfoMap.get(methodInfo
							.getHashName());

					if (!methodInfo.getInterfaceInfo().getMethodInfoList()
							.contains(methodInfo)) {
						methodInfo.getInterfaceInfo().getMethodInfoList().add(
								methodInfo);
					}

					list2 = methodDeclarations[i].parameters();
					if (list2 != null) {
						for (int j = 0; j < list2.size(); j++) {
							ParameterInfo parameterInfo = new ParameterInfo();
							parameterInfo.setName(list2.get(j).getName()
									.getFullyQualifiedName());

							parameterInfo.setType(list2.get(j).getType()
									.toString());
							List<String> simpleTypeList = TypeDisposer.inferSimpleType(parameterInfo.getType());
							parameterInfo.setSimpleTypes(simpleTypeList);
							
							list = list2.get(j).modifiers();
							if (list != null && list.contains("final")) {
								parameterInfo.setFinal(true);
							}

							parameterInfo.setMethodInfo(methodInfo);

							if (!elementInfoPool.parameterInfoMap
									.containsKey(parameterInfo.getHashName())) {
								elementInfoPool.parameterInfoMap.put(
										parameterInfo.getHashName(),
										parameterInfo);
							}
							parameterInfo = elementInfoPool.parameterInfoMap
									.get(parameterInfo.getHashName());

							if (!parameterInfo.getMethodInfo()
									.getParameterInfoList().contains(
											parameterInfo)) {
								parameterInfo.getMethodInfo()
										.getParameterInfoList().add(
												parameterInfo);
							}
						}

					}
					
					if (methodDeclarations[i].getBody() != null) {
						if(parseMethod)
						{
							MethodParser.parseMethodBody(methodInfo, methodDeclarations[i].getBody());
						}
					}
				}
			}
		}
		
		/**
		 * 类声明
		 */
		else {

//			System.out.println("=================" + node.getName().getFullyQualifiedName());
//			System.out.println(elementInfoPool.currentGroupInfo.getPath());
//			System.out.println(elementInfoPool.currentGroupInfo.getHashName());
			
			ClassInfo classInfo = new ClassInfo();
			classInfo.setName(node.getName().getFullyQualifiedName());
			classInfo.setFilePath(elementInfoPool.currentGroupInfo.getPath());
			GroupInfo groupInfo = elementInfoPool.currentGroupInfo;
			PackageInfo packageInfo = groupInfo.getPackageInfo();
			if (packageInfo != null) {
				classInfo.setFullName(packageInfo.getName() + "." + classInfo.getName());
			}
			String javaDoc = null;
			if(node.getJavadoc() != null) {
				javaDoc = node.getJavadoc().toString();
			}
			
			CommentInfo commentInfo;
			if(javaDoc != null) {
				int javaDocStartPos = node.getJavadoc().getStartPosition();
				commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
				//methodInfo.setComment(javaDoc);
				
			}
			else {
				commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(node.getStartPosition());
			}
			classInfo.setCommentInfo(commentInfo);
			List<String> list = node.modifiers();
			for (int i = 0; i < list.size(); i++) {
				if ("public".equals(list.get(i))) {
					classInfo.setVisibility(list.get(i));
					continue;
				}
				if ("final".equals(list.get(i))) {
					classInfo.setFinal(true);
					continue;
				}
				if ("abstract".equals(list.get(i))) {
					classInfo.setAbstract(true);
					continue;
				}
			}
			
			if (node.getSuperclassType() != null) {
				classInfo
						.setSuperClassType(node.getSuperclassType().toString());
			}
			List<Type> simpleTypes = node.superInterfaceTypes();
			for(int i = 0; i < simpleTypes.size(); i++)
			{
				classInfo.getSuperInterfaceTypeList().add(simpleTypes.get(i).toString());
			}

			classInfo.setGroupInfo(elementInfoPool.currentGroupInfo);

			if (!elementInfoPool.classInfoMap.containsKey(classInfo
					.getHashName())) {
				elementInfoPool.classInfoMap.put(classInfo.getHashName(),
						classInfo);
			}
			classInfo = elementInfoPool.classInfoMap.get(classInfo
					.getHashName());

			if (!classInfo.getGroupInfo().getClassInfoList()
					.contains(classInfo)) {
				classInfo.getGroupInfo().getClassInfoList().add(classInfo);
			}

			FieldDeclaration[] fieldDeclarations = node.getFields();
			if (fieldDeclarations != null) {
				for (int i = 0; i < fieldDeclarations.length; i++) {
					javaDoc = null;
					if(fieldDeclarations[i].getJavadoc() != null) {
						javaDoc = fieldDeclarations[i].getJavadoc().toString();
					}
					commentInfo = null;
					if(javaDoc != null) {
						int javaDocStartPos = fieldDeclarations[i].getJavadoc().getStartPosition();
						commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
						//methodInfo.setComment(javaDoc);
						
					}
					else {
						commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(fieldDeclarations[i].getStartPosition());
					}
					String type = fieldDeclarations[i].getType().toString();
					List<String> simpleTypeList = TypeDisposer.inferSimpleType(type);
					
//					System.out.println("FIELD!!!!!!");
//					for (Object modifiObject : fieldDeclarations[i].modifiers()) {
//						Modifier modify = (Modifier) modifiObject;
//						System.out.println(modify + "-");
//					}
//					System.out.println(fieldDeclarations[i].getType());
//					for (Object object : fieldDeclarations[i].fragments()) {
//						VariableDeclarationFragment frag = (VariableDeclarationFragment) object;
//						System.out.println("[FIELD_NAME:]" + frag.getName());
//					}
					
					List<VariableDeclaration> list2 = fieldDeclarations[i]
							.fragments();
					for (int j = 0; j < list2.size(); j++) {
						FieldInfo fieldInfo = new FieldInfo();
						fieldInfo.setName(list2.get(j).getName()
								.getFullyQualifiedName());
						if (commentInfo != null) {
							
							fieldInfo.setCommentInfo(commentInfo);
							fieldInfo.setComment(commentInfo.getCommentString());
						}
						//System.out.println("%%%  " + list2.get(j));
						//System.out.println("^^^  " + list2.get(j).getName()
						//		.getFullyQualifiedName());
						fieldInfo.setType(type);
						fieldInfo.setSimpleTypes(simpleTypeList);
						fieldInfo.setVisibility("private");
						fieldInfo.setFinal(false);
						fieldInfo.setStatic(false);
//						System.out.println("+++++++++++++++++++" + fieldInfo.getName() + " " + fieldInfo.getType());
						//System.out.println(commentInfo.getCommentString());
						list = fieldDeclarations[i].modifiers();
						for (int k = 0; k < list.size(); k++) {
							if ("public".equals(list.get(k))
									|| "package".equals(list.get(k))
									|| "protected".equals(list.get(k))) {
								fieldInfo.setVisibility(list.get(k));
								continue;
							}
							if ("final".equals(list.get(k))) {
								fieldInfo.setFinal(true);
								continue;
							}
							if ("static".equals(list.get(k))) {
								fieldInfo.setStatic(true);
								continue;
							}
						}
						fieldInfo.setClassInfo(classInfo);
						if (!elementInfoPool.fieldInfoMap.containsKey(fieldInfo
								.getHashName())) {
							elementInfoPool.fieldInfoMap.put(fieldInfo
									.getHashName(), fieldInfo);
						}
						fieldInfo = elementInfoPool.fieldInfoMap.get(fieldInfo
								.getHashName());

						if (!fieldInfo.getClassInfo().getFieldInfoList()
								.contains(fieldInfo)) {
							fieldInfo.getClassInfo().getFieldInfoList().add(
									fieldInfo);
						}
					}
				}
			}
			
			String fileContent = null;
			File currentFile = new File(elementInfoPool.currentGroupInfo.getPath());
			
			try {
				fileContent = FileUtils.readFileToString(currentFile);
			} catch (IOException e) {
				fileContent = null;
				e.printStackTrace();
			}


			
			MethodDeclaration[] methodDeclarations = node.getMethods();
//			for (MethodDeclaration methodDeclaration : methodDeclarations) {
//				System.out.println("-------------" + methodDeclaration.getName().getFullyQualifiedName());
//			}
			if (methodDeclarations != null) {
				for (int i = 0; i < methodDeclarations.length; i++) {
					javaDoc = null;
					if(methodDeclarations[i].getJavadoc()!= null) {
						javaDoc = methodDeclarations[i].getJavadoc().toString();
					}
					
					MethodInfo methodInfo = new MethodInfo();
					methodInfo.setShortName(methodDeclarations[i].getName()
							.getFullyQualifiedName());
					commentInfo = null;
					if(javaDoc != null) {
						int javaDocStartPos = methodDeclarations[i].getJavadoc().getStartPosition();
						commentInfo = elementInfoPool.currentGroupInfo.getCommentInfoByStartPosition(javaDocStartPos); 
						//methodInfo.setComment(javaDoc);
						
					}
					else {
						commentInfo = elementInfoPool.currentGroupInfo.getRelatedCommentInfo(methodDeclarations[i].getStartPosition());
					}
					if (commentInfo != null) {
				//		System.out.println(methodInfo.getShortName());
				//		System.out.println(commentInfo.getCommentString());
						methodInfo.setCommentInfo(commentInfo);
						methodInfo.setComment(commentInfo.getCommentString());
					}
//					System.out.println("Method: " + methodInfo.getShortName());
				//	System.out.println("Comment: " + methodInfo.getComment());

					
					methodInfo.setName(methodInfo.getShortName() + "(");
					List<SingleVariableDeclaration> list2 = methodDeclarations[i]
							.parameters();
					if (list2 != null) {
						for (int j = 0; j < list2.size(); j++) {
							String parameterName = list2.get(j).getName()
									.getFullyQualifiedName();
							String parameterType = list2.get(j).getType()
									.toString();
							boolean isParameterFinal = false;
							list = list2.get(j).modifiers();
							if (list != null && list.contains("final")) {
								isParameterFinal = true;
							}
							if (isParameterFinal) {
								methodInfo.setName(methodInfo.getName()
										+ "final " + parameterType + " "
										+ parameterName);
								if (j != list2.size() - 1) {
									methodInfo.setName(methodInfo.getName()
											+ ", ");
								}
							} else {
								methodInfo.setName(methodInfo.getName()
										+ parameterType + " " + parameterName);
								if (j != list2.size() - 1) {
									methodInfo.setName(methodInfo.getName()
											+ ", ");
								}
							}

						}
					}
					methodInfo.setName(methodInfo.getName() + ")");

					methodInfo.setConstruct(methodDeclarations[i]
							.isConstructor());
					if (methodDeclarations[i].getReturnType2() != null) {
						methodInfo.setReturnType(methodDeclarations[i]
								.getReturnType2().toString());
						List<String> simpleTypeList = TypeDisposer.inferSimpleType(methodInfo.getReturnType());
						methodInfo.setSimpleTypes(simpleTypeList);
					}
					
					String methodContent = fileContent.substring(methodDeclarations[i].getStartPosition(), methodDeclarations[i].getStartPosition() + methodDeclarations[i].getLength());
					if(fileContent != null) {
						methodInfo.setMethodContent(methodContent);
					}
//					System.out.println("***************method " + methodInfo.getName() + "************");
//					System.out.println(methodContent);
					
					methodInfo.setVisibility("private");
					methodInfo.setFinal(false);
					methodInfo.setAbstract(false);
					methodInfo.setSynchronized(false);

					list = methodDeclarations[i].modifiers();

					for (int j = 0; j < list.size(); j++) {
						if ("public".equals(list.get(j))
								|| "package".equals(list.get(j))
								|| "protected".equals(list.get(j))) {
							methodInfo.setVisibility(list.get(j));
							continue;
						}
						if ("final".equals(list.get(j))) {
							methodInfo.setFinal(true);
							continue;
						}
						if ("static".equals(list.get(j))) {
							methodInfo.setStatic(true);
							continue;
						}
						if ("abstract".equals(list.get(j))) {
							methodInfo.setAbstract(true);
							continue;
						}
						if ("synchronized".equals(list.get(j))) {
							methodInfo.setSynchronized(true);
						}
					}
					methodInfo.setClassInfo(classInfo);

					if (!elementInfoPool.methodInfoMap.containsKey(methodInfo
							.getHashName())) {
						elementInfoPool.methodInfoMap.put(methodInfo
								.getHashName(), methodInfo);
					}
					methodInfo = elementInfoPool.methodInfoMap.get(methodInfo
							.getHashName());

					if (!methodInfo.getClassInfo().getMethodInfoList()
							.contains(methodInfo)) {
						methodInfo.getClassInfo().getMethodInfoList().add(
								methodInfo);
					}

					list2 = methodDeclarations[i].parameters();
					if (list2 != null) {
						for (int j = 0; j < list2.size(); j++) {
							ParameterInfo parameterInfo = new ParameterInfo();
							parameterInfo.setName(list2.get(j).getName()
									.getFullyQualifiedName());
							parameterInfo.setType(list2.get(j).getType()
									.toString());
							List<String> simpleTypeList = TypeDisposer.inferSimpleType(parameterInfo.getType());
							parameterInfo.setSimpleTypes(simpleTypeList);
							
							list = list2.get(j).modifiers();
							if (list != null && list.contains("final")) {
								parameterInfo.setFinal(true);
							}
							parameterInfo.setMethodInfo(methodInfo);

							if (!elementInfoPool.parameterInfoMap
									.containsKey(parameterInfo.getHashName())) {
								elementInfoPool.parameterInfoMap.put(
										parameterInfo.getHashName(),
										parameterInfo);
							}
							parameterInfo = elementInfoPool.parameterInfoMap
									.get(parameterInfo.getHashName());

							if (!parameterInfo.getMethodInfo()
									.getParameterInfoList().contains(
											parameterInfo)) {
								parameterInfo.getMethodInfo()
										.getParameterInfoList().add(
												parameterInfo);
							}
						}
					}
					
					if (methodDeclarations[i].getBody() != null) {						
						if(parseMethod)
						{	
							MethodParser.parseMethodBody(methodInfo, methodDeclarations[i].getBody(), elementInfoPool);
						}
					}
				}
			}

		}
		return true;
	}


	public void setElementInfoPool(ElementInfoPool elementInfoPool) {
		this.elementInfoPool = elementInfoPool;
	}

	public ElementInfoPool getElementInfoPool() {
		return elementInfoPool;
	}

	/**
	 * @return the parseMethod
	 */
	public boolean isParseMethod() {
		return parseMethod;
	}

	/**
	 * @param parseMethod the parseMethod to set
	 */
	public void setParseMethod(boolean parseMethod) {
		this.parseMethod = parseMethod;
	}
}
