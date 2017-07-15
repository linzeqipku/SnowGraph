package graphdb.extractors.miners.apiusage.entity;

import graphdb.extractors.miners.apiusage.FileIO;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestMethodData extends MethodData implements Serializable, Cloneable
{
	private static final long serialVersionUID = 4476491616175009932L;


	private List<String> sliceList;


	public List<String> getSliceList()
	{
		return sliceList;
	}

	public void setSliceList(List<String> sliceList)
	{
		this.sliceList = sliceList;
	}
	

	public String getSignature()
	{
		return "{" + packageName + "}[" + className + "]" + name;
	}


	public void printSliceListToDirectory(String path)
	{
		File methodRootDir = new File(path);

		if (!methodRootDir.exists())
			methodRootDir.mkdirs();

		if (methodRootDir.isDirectory())
		{
			if (sliceList != null && !sliceList.isEmpty())
			{
				for (int i = 0; i < sliceList.size(); i++)
				{
					FileIO sliceFile = new FileIO(path + File.separator + "slice[" + (i + 1)
							+ "].txt");
					sliceFile.print(sliceList.get(i));
					sliceFile.close();
				}
			}
		}
	}

	public TestMethodData clone()
	{
		TestMethodData newObject = new TestMethodData();
		newObject.setClassName(className);
		newObject.setName(name);
		newObject.setPackageName(packageName);
		newObject.setProjectName(projectName);
		newObject.setText(text);
		if (sliceList != null)
		{
			List<String> slices = new ArrayList<>();
			for (int i = 0; i < sliceList.size(); i++)
			{
				slices.add(sliceList.get(i));
			}
			newObject.setSliceList(slices);
		}
		return newObject;
	}
}
