package pfr.plugins.parsers.javacode.extractor.binparser.utils;

import java.util.ArrayList;
import java.util.List;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.ClassSignature;
import javassist.bytecode.SignatureAttribute.MethodSignature;
import javassist.bytecode.SignatureAttribute.ObjectType;
import javassist.bytecode.SignatureAttribute.Type;

/**
 * Javassist将一个代码实体的很多信息整合在一个signature字符串中，SigUtils用于对这个字符串进行解析.
 * @author Zeqi Lin
 *
 */

public class SigUtils
{

	public static List<String> getParams(CtMethod ctMethod)
	{
		List<String> r = new ArrayList<String>();
		MethodSignature sig = getSig(ctMethod);
		for (Type type : sig.getParameterTypes())
			r.add(type.toString());
		return r;
	}

	public static List<String> getParams(CtConstructor ctConstructor)
	{
		List<String> r = new ArrayList<String>();
		MethodSignature sig = getSig(ctConstructor);
		for (Type type : sig.getParameterTypes())
			r.add(type.toString());
		return r;
	}

	/**
	 * 解析一个类/接口的详细信息
	 * @param ctClass Javassist解析得的一个类/接口的对象
	 * @return 模板和父类
	 */
	public static ClassSigResult getSig(CtClass ctClass)
	{
		String sig = ctClass.getGenericSignature();
		ClassSigResult r = new ClassSigResult();
		if (sig != null)
		{
			try
			{
				ClassSignature classSignature = SignatureAttribute.toClassSignature(sig);
				String[] ele = classSignature.toString().split("((?<=\\>)\\s+(extends)\\s+)|(\\s+(implements)\\s+)");	//	类/接口的字符串形如"<T> extends B implements C"
				r.template = ele[0].trim();	//template="<T>"
				r.superClass = ele[1].trim();	//superClass="B"
			}
			catch (BadBytecode e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			r.template = "<>";
			try
			{
				r.superClass = ctClass.getSuperclass().getName();
			}
			catch (NotFoundException e)
			{
				r.superClass = "java.lang.object";
			}
		}
		return r;
	}

	/**
	 * 获取一个方法的返回类型
	 * @param ctMethod Javassist解析得的一个方法对象
	 * @return 这个方法的返回类型
	 */
	public static String getRt(CtMethod ctMethod)
	{
		return getSig(ctMethod).getReturnType().toString();
	}

	/**
	 * 获取一个域的类型
	 * @param ctField Javassist解析得的一个域对象
	 * @return 这个域的类型，如果是未知类型则返回"NotFound"
	 */
	public static String getType(CtField ctField)
	{
		String sig = ctField.getGenericSignature();
		if (sig == null)
			try
			{
				return ctField.getType().getName();
			}
			catch (NotFoundException e)
			{
				// TODO Auto-generated catch block
				return "NotFound!";
				// e.printStackTrace();
			}
		ObjectType r = null;
		try
		{
			r = SignatureAttribute.toFieldSignature(sig);
		}
		catch (BadBytecode e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r.toString();
	}

	/**
	 * 获取一个方法的详细信息
	 * @param ctMethod Javassist解析得的一个方法对象
	 * @return MethodSignature，包含了输入类型，输出类型和抛出异常类型
	 */
	private static MethodSignature getSig(CtMethod ctMethod)
	{
		String sig = ctMethod.getGenericSignature();
		if (sig == null)
			sig = ctMethod.getSignature();
		MethodSignature r = null;
		try
		{
			r = SignatureAttribute.toMethodSignature(sig);
		}
		catch (BadBytecode e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * 获取一个构造方法的详细信息
	 * @param ctConstructor Javassist解析得的一个构造方法对象
	 * @return MethodSignature，包含了输入类型，输出类型和抛出异常类型
	 */
	private static MethodSignature getSig(CtConstructor ctConstructor)
	{
		String sig = ctConstructor.getGenericSignature();
		if (sig == null)
			sig = ctConstructor.getSignature();
		MethodSignature r = null;
		try
		{
			r = SignatureAttribute.toMethodSignature(sig);
		}
		catch (BadBytecode e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

}
