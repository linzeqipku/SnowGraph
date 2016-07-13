package pfr.plugins.parsers.javacode.extractor.binparser;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javassist.ClassPool;
import javassist.bytecode.ClassFile;

import org.apache.commons.io.FileUtils;

/**
 * 使用Javassist对存放在目录dir中的Java项目字节码进行解析.
 * 
 * @author Zeqi Lin
 *
 */

public class ByteCodePool
{

	ClassPool cp = ClassPool.getDefault();
	List<ClassFile> classes = new ArrayList<ClassFile>();

	public ByteCodePool(File dir)
	{
		String[] extensions = { "class" };
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(dir, extensions, true);

		for (File file : files)
			try
			{
				classes.add(new ClassFile(new DataInputStream(new FileInputStream(file))));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		for (File file : files)
			try
			{
				cp.makeClass(new FileInputStream(file));
			}
			catch (IOException | RuntimeException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
