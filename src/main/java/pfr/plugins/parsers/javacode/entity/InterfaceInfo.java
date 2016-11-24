package pfr.plugins.parsers.javacode.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import pfr.plugins.parsers.javacode.extractor.NameResolver;


/**
 * 
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-23 下午05:16:20
 * @version 0.1 2012-12-15
 * InterfaceInfo对象存储一个接口的信息
 */
public class InterfaceInfo{

	public String name;
	public String fullName;
	public String visibility;
	public List<String> superInterfaceTypeList=new ArrayList<String>();
	public String comment="";
	public String content;

}
