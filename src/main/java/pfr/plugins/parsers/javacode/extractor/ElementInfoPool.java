package pfr.plugins.parsers.javacode.extractor;

import java.util.HashMap;
import java.util.Map;

import pfr.plugins.parsers.javacode.entity.ClassInfo;
import pfr.plugins.parsers.javacode.entity.FieldInfo;
import pfr.plugins.parsers.javacode.entity.InterfaceInfo;
import pfr.plugins.parsers.javacode.entity.MethodInfo;

public class ElementInfoPool
{

	public String srcDir;
	public Map<String,ClassInfo> classInfoMap;
	public Map<String,InterfaceInfo> interfaceInfoMap;
	public Map<String,MethodInfo> methodInfoMap;
	public Map<String,FieldInfo> fieldInfoMap;

	public ElementInfoPool(String srcDir)
	{
		this.srcDir=srcDir;
		classInfoMap=new HashMap<String,ClassInfo>();
		interfaceInfoMap=new HashMap<String,InterfaceInfo>();
		methodInfoMap=new HashMap<String,MethodInfo>();
		fieldInfoMap=new HashMap<String,FieldInfo>();
	}

}
