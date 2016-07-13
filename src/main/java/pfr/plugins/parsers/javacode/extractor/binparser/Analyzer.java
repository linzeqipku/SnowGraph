package pfr.plugins.parsers.javacode.extractor.binparser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinClassInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinFieldInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinInterfaceInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.entity.BinMethodInfo;
import pfr.plugins.parsers.javacode.extractor.binparser.utils.ClassSigResult;
import pfr.plugins.parsers.javacode.extractor.binparser.utils.SigUtils;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

/**
 * 从字节码中抽取出我们关注的结构化信息，并存储于一个BinCodeInfo对象中.
 * @author Zeqi Lin
 *
 */

public class Analyzer
{

	private ByteCodePool pool;

	public Analyzer(ByteCodePool pool)
	{
		this.pool = pool;
	}

	public BinCodeInfo buildCodeInfo()
	{
		List<BinClassInfo> classInfoList = buildClassInfoList();
		List<BinInterfaceInfo> interfaceInfoList = buildInterfaceInfoList();
		List<BinMethodInfo> methodInfoList = buildMethodInfoList();
		List<BinFieldInfo> fieldInfoList = buildFieldInfoList();
		return new BinCodeInfo(classInfoList, interfaceInfoList, methodInfoList, fieldInfoList);
	}

	private List<BinClassInfo> buildClassInfoList()
	{
		List<BinClassInfo> r = new ArrayList<BinClassInfo>();
		for (ClassFile cf : pool.classes)
		{
			if (cf.isInterface())
				continue;
			CtClass ctClass = null;
			try
			{
				ctClass = pool.cp.getCtClass(cf.getName());
			}
			catch (NotFoundException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			String name = ctClass.getSimpleName();	//类名
			String fullName = ctClass.getName();	//类的全名
			ClassSigResult csr = SigUtils.getSig(ctClass);	//类的模板和父类
			String template = csr.template;	//模板
			int modifiers = ctClass.getModifiers();	//访问控制符
			String superClass = csr.superClass;	//父类
			List<String> interfaces = new ArrayList<String>();	//实现的接口
			for (String i : cf.getInterfaces())
				interfaces.add(i);
			HashSet<String> refClasses = new HashSet<String>();	//用到的类
			for (Object tClass : ctClass.getRefClasses())
				refClasses.add((String) tClass);
			r.add(new BinClassInfo(name, fullName, template, modifiers, superClass, interfaces, refClasses));
		}
		return r;
	}

	private List<BinInterfaceInfo> buildInterfaceInfoList()
	{
		List<BinInterfaceInfo> r = new ArrayList<BinInterfaceInfo>();
		for (ClassFile cf : pool.classes)
		{
			if (!cf.isInterface())
				continue;
			CtClass ctClass = null;
			try
			{
				ctClass = pool.cp.getCtClass(cf.getName());
			}
			catch (NotFoundException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			String name = ctClass.getSimpleName();	//接口名
			String fullName = ctClass.getName();	//接口的全名
			ClassSigResult csr = SigUtils.getSig(ctClass);	//接口的模板和父接口
			String template = csr.template;	//模板
			String superInterface = csr.superClass;	//父接口
			r.add(new BinInterfaceInfo(name, fullName, template, superInterface));
		}
		return r;
	}

	private List<BinMethodInfo> buildMethodInfoList()
	{
		List<BinMethodInfo> r = new ArrayList<BinMethodInfo>();
		for (ClassFile cf : pool.classes)
		{
			CtClass ctClass = null;
			try
			{
				ctClass = pool.cp.getCtClass(cf.getName());
			}
			catch (NotFoundException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			
			//处理普通方法
			for (CtMethod ctMethod : ctClass.getDeclaredMethods())
			{
				String name = ctMethod.getName();	//方法名
				String fullName = ctMethod.getLongName();	//方法的全名，例如"javassist.CtMethod.setBody(java.lang.String)"
				int modifier = ctMethod.getModifiers();	//方法的访问控制符
				String belongTo = ctClass.getName();	//方法所属的类/接口
				List<String> params = SigUtils.getParams(ctMethod);	//方法中包含的参数
				String rt = SigUtils.getRt(ctMethod);	//方法的返回类型
				List<String> exceptions = new ArrayList<String>();	//方法抛出的缺陷
				try
				{
					for (CtClass tClass : ctMethod.getExceptionTypes())
						exceptions.add(tClass.getName());
				}
				catch (NotFoundException e)
				{
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				final HashSet<String> methodCalls = new HashSet<String>();
				final HashSet<String> uses = new HashSet<String>();
				try
				{
					//寻找普通方法的调用
					ctMethod.instrument(new ExprEditor()
					{
						@Override
						public void edit(MethodCall m) throws CannotCompileException
						{
							try
							{
								methodCalls.add(m.getMethod().getLongName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						//寻找域的调用
						@Override
						public void edit(FieldAccess a)
						{
							try
							{
								uses.add(a.getField().getDeclaringClass().getName() + "." + a.getField().getName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						//寻找对自己的构造方法的调用
						@Override
						public void edit(ConstructorCall m) throws CannotCompileException
						{
							try
							{
								methodCalls.add(m.getConstructor().getLongName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						//寻找对其它类的构造方法的调用
						@Override
						public void edit(NewExpr m) throws CannotCompileException
						{
							try
							{
								methodCalls.add(m.getConstructor().getLongName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}
					});
				}
				catch (CannotCompileException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				r.add(new BinMethodInfo(name, fullName, modifier, belongTo, params, rt, exceptions, methodCalls, uses));
			}
			
			//处理构造方法
			for (CtConstructor ctConstructor : ctClass.getDeclaredConstructors())
			{
				String name = ctConstructor.getName();
				String fullName = ctConstructor.getLongName();
				int modifier = ctConstructor.getModifiers();
				String belongTo = ctClass.getName();
				List<String> params = SigUtils.getParams(ctConstructor);
				String rt = "";
				List<String> exceptions = new ArrayList<String>();
				try
				{
					for (CtClass tClass : ctConstructor.getExceptionTypes())
						exceptions.add(tClass.getName());
				}
				catch (NotFoundException e)
				{
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				final HashSet<String> methodCalls = new HashSet<String>();
				final HashSet<String> uses = new HashSet<String>();
				try
				{
					ctConstructor.instrument(new ExprEditor()
					{
						@Override
						public void edit(MethodCall m) throws CannotCompileException
						{
							try
							{
								methodCalls.add(m.getMethod().getLongName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						@Override
						public void edit(FieldAccess a)
						{
							try
							{
								uses.add(a.getField().getDeclaringClass().getName() + "." + a.getField().getName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						@Override
						public void edit(ConstructorCall m) throws CannotCompileException
						{
							try
							{
								methodCalls.add(m.getConstructor().getLongName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}

						@Override
						public void edit(NewExpr m) throws CannotCompileException
						{
							try
							{
								methodCalls.add(m.getConstructor().getLongName());
							}
							catch (NotFoundException e)
							{
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}
					});
				}
				catch (CannotCompileException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				r.add(new BinMethodInfo(name, fullName, modifier, belongTo, params, rt, exceptions, methodCalls, uses));
			}
		}
		return r;
	}

	private List<BinFieldInfo> buildFieldInfoList()
	{
		List<BinFieldInfo> r = new ArrayList<BinFieldInfo>();
		for (ClassFile cf : pool.classes)
		{
			CtClass ctClass = null;
			try
			{
				ctClass = pool.cp.getCtClass(cf.getName());
			}
			catch (NotFoundException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			for (CtField ctField : ctClass.getDeclaredFields())
			{
				String name = ctField.getName();	//域名
				int modifiers = ctField.getModifiers();	//域的访问控制符
				String type = SigUtils.getType(ctField);	//域的类型
				String belongTo = ctClass.getName();	//域所属的类
				r.add(new BinFieldInfo(name, modifiers, type, belongTo));
			}
		}
		return r;
	}

}
