package extractors.miners.mailqa.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

public class URL2File {

	public static File getFileFromUrl(String urlString, String dest) throws IOException {
		long start = System.currentTimeMillis();
		URL url = new URL(urlString);
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		String tempDir = dest;
		String path = tempDir + urlString.substring(urlString.lastIndexOf("/") + 1)
				+ UUID.randomUUID() + ".txt";
		File f = new File(path);
		if (!f.exists())
			f.createNewFile();
		System.out.println("start copy file from " + urlString + " to " + f.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(f);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.flush();
		fos.close();
		System.out.println(f.getAbsolutePath() + "'s total copy time :"
				+ ((System.currentTimeMillis() - start) / 1000) + " second");
		return f;
	}

	public static void main(String args[]) throws IOException {
		String urlString = "http://mail-archives.apache.org/mod_mbox/lucene-java-user/201309.mbox";
		URL2File.getFileFromUrl(urlString, "D:/lab/final/mbox_file/lucene/");
	}
}
