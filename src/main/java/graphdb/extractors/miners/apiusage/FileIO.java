package graphdb.extractors.miners.apiusage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class FileIO
{
	private static String relativePath = "";
	private String path = "";
	private PrintWriter out;
	private RandomAccessFile randomAccessFile;

	public FileIO(String fileName)
	{
		path += fileName;
		try
		{
			File file = new File(path);
			if (!file.exists())
				file.createNewFile();

			//out = new PrintWriter(file);
			randomAccessFile = new RandomAccessFile(file, "rw");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public void printToEnd(Object obj)
	{
		try
		{
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.writeBytes(obj.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void printlnToEnd(Object obj)
	{
		try
		{
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.writeBytes(obj.toString() + "\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void print(String text)
	{
		try
		{
			out.print(text);
			out.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void println(String text)
	{
		try
		{
			out.println(text);
			out.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		try
		{
			if (out != null)
				out.close();
			if (randomAccessFile != null)
				randomAccessFile.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void print(String text, String fileName)
	{
		PrintWriter out;
		try
		{
			out = new PrintWriter(new File(relativePath + fileName));
			out.print(text);
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static void println(String text, String fileName)
	{
		PrintWriter out;
		try
		{
			out = new PrintWriter(new File(relativePath + fileName));
			out.println(text);
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

}
