package pfr.framework;

import org.eclipse.jdt.core.dom.IMethodBinding;

import pfr.plugins.parsers.javacode.entity.ClassInfo;
import pfr.plugins.parsers.javacode.entity.MethodInfo;
import pfr.plugins.parsers.javacode.extractor.JavaParser;
import pfr.plugins.parsers.javacode.extractor.ElementInfoPool;

public class TestSrcParser
{

	public static void main(String[] args){
		ElementInfoPool elementInfoPool=JavaParser.parse("E:/testjava");
		
		for (ClassInfo classInfo:elementInfoPool.classInfoMap.values())
			System.out.println(classInfo.superClassType);
		
		for (MethodInfo methodInfo:elementInfoPool.methodInfoMap.values()){
			for (IMethodBinding methodBinding:methodInfo.methodCalls)
				for (MethodInfo methodInfo2:elementInfoPool.methodInfoMap.values())
					if (methodBinding==methodInfo2.methodBinding)
						System.out.println(methodInfo.hashName()+" calls "+methodInfo2.hashName());
		}
		
		for (MethodInfo methodInfo:elementInfoPool.methodInfoMap.values()){
			for (String variable:methodInfo.variableTypes)
				System.out.println(methodInfo.hashName()+" haveVariable "+variable);
		}
		
		for (MethodInfo methodInfo:elementInfoPool.methodInfoMap.values())
			for (String fieldUse:methodInfo.fieldUsesSet)
				System.out.println(methodInfo.hashName()+" uses "+fieldUse);
		
		for (MethodInfo methodInfo:elementInfoPool.methodInfoMap.values())
			for (String exp:methodInfo.throwSet)
				System.out.println(methodInfo.hashName()+" throws "+exp);
		
	}
	
}
