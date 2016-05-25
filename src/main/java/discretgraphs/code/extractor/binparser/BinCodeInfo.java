package discretgraphs.code.extractor.binparser;

import java.util.List;

import discretgraphs.code.extractor.binparser.entity.BinClassInfo;
import discretgraphs.code.extractor.binparser.entity.BinFieldInfo;
import discretgraphs.code.extractor.binparser.entity.BinInterfaceInfo;
import discretgraphs.code.extractor.binparser.entity.BinMethodInfo;

/**
 * 类Analyzer将字节码中我们关心的信息抽取出来之后，记录在一个BinCodeInfo对象之中.
 * @author Zeqi Lin
 *
 */

public class BinCodeInfo
{

	private List<BinClassInfo> classInfoList;
	private List<BinFieldInfo> fieldInfoList;
	private List<BinInterfaceInfo> interfaceInfoList;
	private List<BinMethodInfo> methodInfoList;

	public BinCodeInfo(List<BinClassInfo> classInfoList, List<BinInterfaceInfo> interfaceInfoList, List<BinMethodInfo> methodInfoList, List<BinFieldInfo> fieldInfoList)
	{
		this.classInfoList = classInfoList;
		this.interfaceInfoList = interfaceInfoList;
		this.methodInfoList = methodInfoList;
		this.fieldInfoList = fieldInfoList;
	}

	public List<BinClassInfo> getClassInfoList()
	{
		return classInfoList;
	}

	public List<BinInterfaceInfo> getInterfaceInfoList()
	{
		return interfaceInfoList;
	}

	public List<BinMethodInfo> getMethodInfoList()
	{
		return methodInfoList;
	}

	public List<BinFieldInfo> getFieldInfoList()
	{
		return fieldInfoList;
	}

}
