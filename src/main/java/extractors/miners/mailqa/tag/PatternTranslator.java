package extractors.miners.mailqa.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import extractors.miners.mailqa.utils.ReadFile;
import extractors.miners.mailqa.utils.WriteFile;

/**
 * @ClassName: PatternTranslator
 * @Description: TODO 将算法挖掘出的模式翻译成英文 pattern 格式 数字后接空格后接-1后接空格 最后是 #SUP:接空格接数字
 * @author: left
 * @date: 2014年3月19日 下午8:17:46
 */
class Pattern implements Comparable<Pattern> {

	public ArrayList<String>	wordVector	= new ArrayList<String>();

	public ArrayList<Integer>	intVector	= new ArrayList<Integer>();

	public int					supTime		= 0;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("sup time : " + supTime + "\n");
		for (String word : wordVector) {
			sb.append(word + " ");
		}
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public int compareTo(Pattern o) {
		return o.supTime - this.supTime;
	}
}

public class PatternTranslator {

	public static final String	path		= "D:/lab/final/output.txt";

	public ArrayList<String>	lines		= new ArrayList<String>();

	public ArrayList<Pattern>	patterns	= new ArrayList<Pattern>();

	public void readLinesFromFile() {
		lines = ReadFile.readFileLines(path);
		int count = 0;

		HashMap<Integer, String> tMap = TagEncoder.getTagMap();
		for (String line : lines) {
			Pattern p = new Pattern();
			int index = line.indexOf("#SUP: ");
			String pre = line.substring(0, index);
			String post = line.substring(index + 6);

			String[] vector = pre.split("\\s+-1\\s+");
			for (String str : vector) {
				p.intVector.add(Integer.parseInt(str));
				p.wordVector.add(tMap.get(Integer.parseInt(str)));
				p.supTime = Integer.parseInt(post);
			}
			if (p.wordVector.size() >= 3)
				patterns.add(p);
		}
		// Collections.sort(patterns);

		System.out.println(patterns.size());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < patterns.size(); i++) {
			System.out.println(patterns.get(i));
			if (patterns.get(i).supTime >= 10 && patterns.get(i).wordVector.size() >= 5)
				sb.append(patterns.get(i) + "\n");
		}
		WriteFile.writeStringToFile(sb.toString(), "D:/lab/final/pattern.txt");

		Collections.sort(patterns);
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < patterns.size(); i++) {
			sb2.append(patterns.get(i) + "\n");
		}
		WriteFile.writeStringToFile(sb2.toString(), "D:/lab/final/pattern_sort.txt");
	}

	public static void main(String args[]) {
		PatternTranslator pt = new PatternTranslator();
		pt.readLinesFromFile();
		// for(String str : pt.lines) {
		// System.out.println(str);
		// }
	}
}
