package pfr.plugins.parsers.javacode.extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.type.UnionType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import pfr.plugins.parsers.javacode.entity.ClassInfo;
import pfr.plugins.parsers.javacode.entity.FieldInfo;
import pfr.plugins.parsers.javacode.entity.InterfaceInfo;
import pfr.plugins.parsers.javacode.entity.MethodInfo;
import pfr.plugins.parsers.javacode.extractor.ElementInfoPool;

public class JavaASTVisitor extends ASTVisitor 
{

	private ElementInfoPool elementInfoPool;
	private String sourceContent;

	public JavaASTVisitor(ElementInfoPool elementInfoPool, String sourceContent)
	{
		this.elementInfoPool=elementInfoPool;
		this.sourceContent=sourceContent;
		NameResolver.setSrcDir(elementInfoPool.srcDir);
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.isInterface())
			return visitInterface(node);
		else
			return visitClass(node);
	}

	private boolean visitInterface(TypeDeclaration node)
	{
		
		InterfaceInfo interfaceInfo = new InterfaceInfo();
		interfaceInfo.name=node.getName().getFullyQualifiedName();
		interfaceInfo.fullName=NameResolver.getFullName(node);
		interfaceInfo.visibility=getVisibility(node);
		List<Type> superInterfaceList=node.superInterfaceTypes();
		for (Type superInterface:superInterfaceList)
			interfaceInfo.superInterfaceTypeList.add(NameResolver.getFullName(superInterface));
		if (node.getJavadoc()!=null)
			interfaceInfo.comment=sourceContent.substring(node.getJavadoc().getStartPosition(),node.getJavadoc().getStartPosition()+node.getJavadoc().getLength());
		interfaceInfo.content=sourceContent.substring(node.getStartPosition(),node.getStartPosition()+node.getLength());
		elementInfoPool.interfaceInfoMap.put(interfaceInfo.fullName, interfaceInfo);
		
		MethodDeclaration[] methodDeclarations = node.getMethods();
		for (MethodDeclaration methodDeclaration:methodDeclarations){
			MethodInfo methodInfo=createMethodInfo(methodDeclaration,interfaceInfo.fullName);
			elementInfoPool.methodInfoMap.put(methodInfo.hashName(), methodInfo);
		}
		
		FieldDeclaration[] fieldDeclarations = node.getFields();
		for (FieldDeclaration fieldDeclaration:fieldDeclarations){
			List<FieldInfo> fieldInfos=createFieldInfos(fieldDeclaration,interfaceInfo.fullName);
			for (FieldInfo fieldInfo:fieldInfos)
				elementInfoPool.fieldInfoMap.put(fieldInfo.hashName(), fieldInfo);
		}
		
		return true;
	}
	
	private boolean visitClass(TypeDeclaration node){
		
		ClassInfo classInfo=new ClassInfo();
		classInfo.name=node.getName().getFullyQualifiedName();
		classInfo.fullName=NameResolver.getFullName(node);
		classInfo.visibility=getVisibility(node);
		classInfo.isAbstract=isAbstract(node);
		classInfo.isFinal=isFinal(node);
		classInfo.superClassType=node.getSuperclassType()==null?"java.lang.Object":NameResolver.getFullName(node.getSuperclassType());
		List<Type> superInterfaceList=node.superInterfaceTypes();
		for (Type superInterface:superInterfaceList)
			classInfo.superInterfaceTypeList.add(NameResolver.getFullName(superInterface));
		if (node.getJavadoc()!=null)
			classInfo.comment=sourceContent.substring(node.getJavadoc().getStartPosition(),node.getJavadoc().getStartPosition()+node.getJavadoc().getLength());
		classInfo.content=sourceContent.substring(node.getStartPosition(),node.getStartPosition()+node.getLength());
		elementInfoPool.classInfoMap.put(classInfo.fullName, classInfo);
		
		MethodDeclaration[] methodDeclarations = node.getMethods();
		for (MethodDeclaration methodDeclaration:methodDeclarations){
			MethodInfo methodInfo=createMethodInfo(methodDeclaration,classInfo.fullName);
			elementInfoPool.methodInfoMap.put(methodInfo.hashName(), methodInfo);
		}
		
		FieldDeclaration[] fieldDeclarations = node.getFields();
		for (FieldDeclaration fieldDeclaration:fieldDeclarations){
			List<FieldInfo> fieldInfos=createFieldInfos(fieldDeclaration,classInfo.fullName);
			for (FieldInfo fieldInfo:fieldInfos)
				elementInfoPool.fieldInfoMap.put(fieldInfo.hashName(), fieldInfo);
		}
		
		return true;
	}
	
	private List<FieldInfo> createFieldInfos(FieldDeclaration node, String belongTo){
		List<FieldInfo> fieldInfos=new ArrayList<FieldInfo>();
		Type type=node.getType();
		Set<String> types=getTypes(type);
		String typeString=type.toString();
		String visibility=getVisibility(node);
		boolean isStatic=isStatic(node);
		boolean isFinal=isFinal(node);
		String comment="";
		if (node.getJavadoc()!=null)
			comment=sourceContent.substring(node.getJavadoc().getStartPosition(),node.getJavadoc().getStartPosition()+node.getJavadoc().getLength());
		List<VariableDeclarationFragment> fragments=node.fragments();
		for (VariableDeclarationFragment fragment:fragments){
			FieldInfo fieldInfo=new FieldInfo();
			fieldInfo.belongTo=belongTo;
			fieldInfo.name=fragment.getName().getFullyQualifiedName();
			fieldInfo.typeString=typeString;
			fieldInfo.types=types;
			fieldInfo.visibility=visibility;
			fieldInfo.isFinal=isFinal;
			fieldInfo.isStatic=isStatic;
			fieldInfo.comment=comment;
			fieldInfos.add(fieldInfo);
		}
		return fieldInfos;
	}
	
	private MethodInfo createMethodInfo(MethodDeclaration node, String belongTo){
		MethodInfo methodInfo=new MethodInfo();
		methodInfo.methodBinding=node.resolveBinding();
		methodInfo.name=node.getName().getFullyQualifiedName();
		Type returnType=node.getReturnType2();
		methodInfo.returnString=returnType==null?"void":returnType.toString();
		methodInfo.returnTypes=getTypes(returnType);
		methodInfo.visibility=getVisibility(node);
		methodInfo.isConstruct=node.isConstructor();
		methodInfo.isAbstract=isAbstract(node);
		methodInfo.isFinal=isFinal(node);
		methodInfo.isStatic=isStatic(node);
		methodInfo.isSynchronized=isSynchronized(node);
		methodInfo.content=sourceContent.substring(node.getStartPosition(),node.getStartPosition()+node.getLength());
		if (node.getJavadoc()!=null)
			methodInfo.comment=sourceContent.substring(node.getJavadoc().getStartPosition(),node.getJavadoc().getStartPosition()+node.getJavadoc().getLength());
		methodInfo.belongTo=belongTo;
		List<SingleVariableDeclaration> params=node.parameters();
		List<String> paramStringList=new ArrayList<String>();
		for (SingleVariableDeclaration param:params){
			String name=param.getName().getFullyQualifiedName();
			Type type=param.getType();
			String paramString=(isFinal(param)?"final":"")+" "+type.toString()+" "+name;
			paramStringList.add(paramString);
			methodInfo.paramTypes.addAll(getTypes(type));
		}
		methodInfo.paramString=String.join(", ", paramStringList).trim();
		List<Type> expList=node.thrownExceptionTypes();
		for (Type exp:expList){
			String name=NameResolver.getFullName(exp);
			methodInfo.throwSet.add(name);
		}
		parseMethodBody(methodInfo, node.getBody());
		return methodInfo;
	}
	
	private void parseMethodBody(MethodInfo methodInfo, Block methodBody){
		if (methodBody==null)
			return;
		List<Statement> statementList = methodBody.statements();
		List<Statement> statements = new ArrayList<Statement>();
		for(int i = 0; i < statementList.size(); i++)
		{
			statements.add(statementList.get(i));
		}
		for(int i = 0; i < statements.size(); i++){
			
			if(statements.get(i).getNodeType() == ASTNode.BLOCK)
			{
				List<Statement> blockStatements = ((Block) statements.get(i)).statements();
				for(int j = 0; j < blockStatements.size(); j++)
				{
					statements.add(i + j + 1, blockStatements.get(j));
				}
				continue;
			}
			if(statements.get(i).getNodeType() == ASTNode.ASSERT_STATEMENT)
			{
				Expression expression = ((AssertStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);					
				}
				expression = ((AssertStatement) statements.get(i)).getMessage();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);					
				}
			}
			
			if(statements.get(i).getNodeType() == ASTNode.DO_STATEMENT)
			{
				Expression expression = ((DoStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement doBody = ((DoStatement) statements.get(i)).getBody();
				if(doBody != null)
				{
					statements.add(i + 1, doBody);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT)
			{
				Expression expression = ((EnhancedForStatement) statements.get(i)).getExpression();
				Type type = ((EnhancedForStatement) statements.get(i)).getParameter().getType();
				methodInfo.variableTypes.addAll(getTypes(type));
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement forBody = ((EnhancedForStatement) statements.get(i)).getBody();
				if(forBody != null)
				{
					statements.add(i + 1, forBody);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.EXPRESSION_STATEMENT)
			{
				Expression expression = ((ExpressionStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
			}		
			if(statements.get(i).getNodeType() == ASTNode.FOR_STATEMENT)
			{
				List<Expression> list = ((ForStatement) statements.get(i)).initializers();
				for(int j = 0; j < list.size(); j++)
				{
					parseExpression(methodInfo, list.get(j));
				}
				Expression expression = ((ForStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement forBody = ((ForStatement) statements.get(i)).getBody();
				if(forBody != null)
				{
					statements.add(i + 1, forBody);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.IF_STATEMENT)
			{
				Expression expression = ((IfStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement thenStatement = ((IfStatement) statements.get(i)).getThenStatement();
				Statement elseStatement = ((IfStatement) statements.get(i)).getElseStatement();
				if(elseStatement != null)
				{
					statements.add(i + 1, elseStatement);
				}
				if(thenStatement != null)
				{
					statements.add(i + 1, thenStatement);				
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.RETURN_STATEMENT)
			{
				Expression expression = ((ReturnStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.SWITCH_STATEMENT)
			{
				Expression expression = ((SwitchStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				List<Statement> switchStatements = ((SwitchStatement) statements.get(i)).statements();
				for(int j = 0; j < switchStatements.size(); j++)
				{
					statements.add(i + j + 1, switchStatements.get(j));
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.THROW_STATEMENT)
			{
				Expression expression = ((ThrowStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
			}
			if(statements.get(i).getNodeType() == ASTNode.TRY_STATEMENT)
			{
				Statement tryStatement = ((TryStatement) statements.get(i)).getBody();
				if(tryStatement != null)
				{
					statements.add(i + 1, tryStatement);
				}
				continue;
			}
			if(statements.get(i).getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT)
			{	
				Type type = ((VariableDeclarationStatement) statements.get(i)).getType();
				List<VariableDeclaration> list = ((VariableDeclarationStatement) statements.get(i)).fragments();
				methodInfo.variableTypes.addAll(getTypes(type));
				for(VariableDeclaration decStat:list){
					parseExpression(methodInfo, decStat.getInitializer());
				}
			}			
			if(statements.get(i).getNodeType() == ASTNode.WHILE_STATEMENT)
			{
				Expression expression = ((WhileStatement) statements.get(i)).getExpression();
				if(expression != null)
				{
					parseExpression(methodInfo, expression);
				}
				Statement whileBody = ((WhileStatement) statements.get(i)).getBody();
				if(whileBody != null)
				{
					statements.add(i + 1, whileBody);
				}
			}
		}
	}
	
	private void parseExpression(MethodInfo methodInfo, Expression expression) {
		if(expression == null) {
			return;
		}//System.out.println(expression.toString()+" "+Annotation.nodeClassForType(expression.getNodeType()));
		if(expression.getNodeType() == ASTNode.ARRAY_INITIALIZER)
		{
			List<Expression> expressions = ((ArrayInitializer) expression).expressions();
			for(Expression expression2 : expressions) {
				parseExpression(methodInfo, expression2);
			}
		}
		if(expression.getNodeType() == ASTNode.CAST_EXPRESSION)
		{
			parseExpression(methodInfo, ((CastExpression) expression).getExpression());
		}
		if(expression.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION)
		{
			parseExpression(methodInfo, ((ConditionalExpression) expression).getExpression());
			parseExpression(methodInfo, ((ConditionalExpression) expression).getElseExpression());
			parseExpression(methodInfo, ((ConditionalExpression) expression).getThenExpression());
		}
		if(expression.getNodeType() == ASTNode.INFIX_EXPRESSION)
		{
			parseExpression(methodInfo, ((InfixExpression) expression).getLeftOperand());
			parseExpression(methodInfo, ((InfixExpression) expression).getRightOperand());
		}
		if(expression.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION)
		{
			parseExpression(methodInfo, ((InstanceofExpression) expression).getLeftOperand());
		}
		if(expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION)
		{
			parseExpression(methodInfo, ((ParenthesizedExpression) expression).getExpression());
		}
		if(expression.getNodeType() == ASTNode.POSTFIX_EXPRESSION)
		{
			parseExpression(methodInfo, ((PostfixExpression) expression).getOperand());
		}
		if(expression.getNodeType() == ASTNode.PREFIX_EXPRESSION)
		{
			parseExpression(methodInfo, ((PrefixExpression) expression).getOperand());
		}
		if(expression.getNodeType() == ASTNode.THIS_EXPRESSION)
		{
			parseExpression(methodInfo, ((ThisExpression) expression).getQualifier());
		}
		if(expression.getNodeType() == ASTNode.METHOD_INVOCATION)
		{
			List<Expression> arguments = ((MethodInvocation) expression).arguments();
			IMethodBinding methodBinding=((MethodInvocation) expression).resolveMethodBinding();
			if (methodBinding!=null)
				methodInfo.methodCalls.add(methodBinding);
			for (Expression exp:arguments)
				parseExpression(methodInfo, exp);
			parseExpression(methodInfo, ((MethodInvocation) expression).getExpression());
		}
		if(expression.getNodeType() == ASTNode.ASSIGNMENT)
		{
			parseExpression(methodInfo, ((Assignment) expression).getLeftHandSide());
			parseExpression(methodInfo, ((Assignment) expression).getRightHandSide());
		}
		if(expression.getNodeType() == ASTNode.QUALIFIED_NAME)
		{
			if (((QualifiedName) expression).getQualifier().resolveTypeBinding()!=null){
				String name=((QualifiedName) expression).getQualifier().resolveTypeBinding().getQualifiedName()+"."+((QualifiedName) expression).getName().getIdentifier();
				methodInfo.fieldUsesSet.add(name);
			}
			parseExpression(methodInfo, ((QualifiedName) expression).getQualifier());
		}
	}
	
	private Set<String> getTypes(Type oType){
		Set<String> types=new HashSet<String>();
		if (oType==null)
			return types;
		ITypeBinding typeBinding=oType.resolveBinding();
		if (typeBinding==null)
			return types;
		String str=typeBinding.getQualifiedName();
		String[] eles=str.split("[^A-Za-z0-9_\\.]+");
		for (String e:eles){
			if (e.equals("extends"))
				continue;
			types.add(e);
		}
		return types;
	}
    
    private static String getVisibility(TypeDeclaration decl){
    	int modifiers = decl.getModifiers();
    	if (Modifier.isPrivate(modifiers))
			return "private";
		if (Modifier.isProtected(modifiers))
			return "protected";
		if (Modifier.isPublic(modifiers))
			return "public";
    	return "package";
    }
    
    private static String getVisibility(MethodDeclaration decl){
    	int modifiers = decl.getModifiers();
    	if (Modifier.isPrivate(modifiers))
			return "private";
		if (Modifier.isProtected(modifiers))
			return "protected";
		if (Modifier.isPublic(modifiers))
			return "public";
    	return "package";
    }
    
    private static String getVisibility(FieldDeclaration decl){
    	int modifiers = decl.getModifiers();
    	if (Modifier.isPrivate(modifiers))
			return "private";
		if (Modifier.isProtected(modifiers))
			return "protected";
		if (Modifier.isPublic(modifiers))
			return "public";
    	return "package";
    }
    
    private static boolean isAbstract(TypeDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isAbstract(modifiers));
    }
    
    private static boolean isAbstract(MethodDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isAbstract(modifiers));
    }
    
    private static boolean isFinal(TypeDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isFinal(modifiers));
    }
    
    private static boolean isFinal(FieldDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isFinal(modifiers));
    }
    
    private static boolean isFinal(MethodDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isFinal(modifiers));
    }
    
    private static boolean isStatic(MethodDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isStatic(modifiers));
    }
    
    private static boolean isStatic(FieldDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isStatic(modifiers));
    }
    
    private static boolean isSynchronized(MethodDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isSynchronized(modifiers));
    }
    
    private static boolean isFinal(SingleVariableDeclaration decl){
    	int modifiers = decl.getModifiers();
    	return (Modifier.isFinal(modifiers));
    }
	
}
