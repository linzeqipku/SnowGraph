package crawlers.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ApacheMailCrawler {

	private static String httpU = "http://mail-archives.apache.org/mod_mbox/";
	private static String outDirPath = "";
	private static String date[] = { "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10", "11", "12" };

	public static void downPro(String fileName, String downURL) {
		try{
		File file = new File(fileName);
		URL u;
		URLConnection connection = null;
		
			u = new URL(downURL);
			connection = u.openConnection();
			connection.setReadTimeout(100000);

		InputStream is = null;
		file.createNewFile();
		is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		FileWriter writer = new FileWriter(file);
		int w = 0;
		while ((w = reader.read()) != -1) {
			writer.write(w);
			// System.out.println(w);
		}
		reader.close();
		writer.close();
		} catch (Exception e) {
			System.out.println("Error");
		}
	}

	public static void downPro(File file, String downURL) {
		URL u;
		URLConnection connection = null;
		try {
			u = new URL(downURL);
			connection = u.openConnection();
			connection.setReadTimeout(100000);

			InputStream is = null;
			file.createNewFile();

			is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);
			FileWriter writer = new FileWriter(file);
			int w = 0;
			while ((w = reader.read()) != -1) {
				writer.write(w);
				// System.out.println(w);
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			System.out.println("Error");
		}

	}

	public static void getPro(String proName, String year) throws Exception {
		Document doc = Jsoup
				.connect("http://mail-archives.apache.org/mod_mbox/")
				.timeout(200000).get();
		Element content = doc.getElementById("tlpform");
		String title = doc.title();
		System.out.println(title);
		Elements links = content.getElementsByTag("a");
		int proTag = 0;
		String linkU = proName;
		String tmpU;
		String linkName;
		File outDir = new File(outDirPath + proName);
		outDir.mkdirs();
		File outFile = null;
		File mailFile = null;

	
		for (Element link : links) {
			String linkText = link.text();
			if (linkText.startsWith(proName+".")) {
				proTag = 0;
			}
			if (linkText.contains(proName)) {
				System.out.println(linkText);
				proTag = 1;
			} else if (proTag == 1) {
				linkName = linkU + "-" + linkText;
				tmpU = httpU + linkU + "-" + linkText;
				System.out.println(tmpU);
				System.out.println(linkName);
				outFile = new File(outDir + "\\" + linkName);
				outFile.mkdirs();
				for (int i = 0; i < 12; i++) {
					mailFile = new File(outFile + "\\" + year + date[i]
							+ ".mbox");
					System.out.println(tmpU + "/" + year + date[i] + ".mbox");
					downPro(mailFile, tmpU + "/" + year + date[i] + ".mbox");
					if (mailFile.length() == 0) {
						mailFile.delete();
					}
				}
			}
		}
		
	}
	
	public static void getPro(String proName, String year, String type) throws Exception {
		Document doc = Jsoup
				.connect("http://mail-archives.apache.org/mod_mbox/")
				.timeout(200000).get();
		Element content = doc.getElementById("tlpform");
		String title = doc.title();
		System.out.println(title);
		Elements links = content.getElementsByTag("a");
		int proTag = 0;
		String linkU = proName;
		String tmpU;
		String linkName;
		File outDir = new File(outDirPath + proName);
		outDir.mkdirs();
		File outFile = null;
		File mailFile = null;
		for (Element link : links) {
			String linkText = link.text();
			System.out.println("-linkText: " + linkText);

			if (linkText.contains("apache")) {
				proTag = 0;
			}
			if (linkText.startsWith(proName+".")) {
				System.out.println("linkText: " + linkText);
				proTag = 1;
			} else if (proTag == 1) {
				linkName = linkU + "-" + linkText;
				tmpU = httpU + linkU + "-" + linkText;
				if(!linkName.contains(type) || linkName.contains("java")) continue;
				System.out.println("linkText: " + linkText);
				System.out.println(tmpU);
				System.out.println(linkName);
				outFile = new File(outDir + "\\" + linkName);
				outFile.mkdirs();
				for (int i = 0; i < 12; i++) {
					mailFile = new File(outFile + "\\" + year + date[i]
							+ ".mbox");
					downPro(mailFile, tmpU + "/" + year + date[i] + ".mbox");
					if (mailFile.length() == 0) {
						mailFile.delete();
					}
				}

			}

		}
	}

	public static void main(String args[]) throws Exception {
		getPro("lucene", "2015");
	}
}

