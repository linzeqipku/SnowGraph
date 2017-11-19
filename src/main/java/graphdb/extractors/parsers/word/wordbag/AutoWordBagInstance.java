package graphdb.extractors.parsers.word.wordbag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class AutoWordBagInstance {

	private final static String	titlePath			= "data/wordbag/original/titles.txt";
	private final static String	cellPath			= "data/wordbag/original/cells.txt";

	public final static String	semanticTitlePath	= "data/wordbag/semantic/titles.txt";
	public final static String	semanticCellPath	= "data/wordbag/semantic/cells.txt";

	public static WordBag		tWordBag			= new WordBag(new File(semanticTitlePath));
	public static WordBag		cWordBag			= new WordBag(new File(semanticCellPath));

	public static void main(String[] args) {
		System.out.println(getCommon("功能描述"));
		System.out.println(getCommon("所属类"));
		System.out.println(getCommon("返回码、返回信息说明"));
	}

	public static String getCommon(String s) {
		String r = tWordBag.getCommon(s).length() > 0 ? tWordBag.getCommon(s) : cWordBag
				.getCommon(s);
		return (r.length() == 0) ? null : r;
	}

	public static void record() {
		tWordBag.record(titlePath);
		cWordBag.record(cellPath);
	}

	private static void mark() throws IOException {
		List<String> lines = FileUtils.readLines(new File(semanticTitlePath));
		List<String> r = new ArrayList<>();
		for (String line : lines) {
			if (line.endsWith("T"))
				return;
			String[] eles = line.split("\\s+");
			if (Integer.parseInt(eles[1]) > 24)
				r.add(line + " T");
			else
				r.add(line);
		}
		FileUtils.writeLines(new File(semanticTitlePath), r);
	}

}
