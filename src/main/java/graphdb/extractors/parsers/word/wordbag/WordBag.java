package graphdb.extractors.parsers.word.wordbag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class WordBag {

	List<WordBagItem>	wordBag	= new ArrayList<WordBagItem>();

	public static void main(String[] args) {
		WordBag wb = new WordBag(new File("data/label/titles.txt"));
		System.out.println(wb.getCommon("1.API说明"));
		System.out.println(wb.getCommon("引言"));
		System.out.println(wb.getCommon("业务API"));
		System.out.println(wb.getCommon("设计目标与约束"));
		System.out.println(wb.getCommon("原子API"));
	}
	
	public WordBag(){
		
	}
	
	public WordBag(File file){
		if (file.exists())
			load(file);
		else
			System.out.println(file.getAbsolutePath());
	}

	public void add(String s,int c) {
		List<String> v = Tokenizer.token(s);
		for (String word : v)
			if (word.length() < 20 && word.length() > 1 && !word.matches("[\\-\\.0-9\\s]+"))
				addWord(word,c);
	}
	
	public void add(String s){
		add(s,1);
	}

	public void load(File file) {
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(file,"UTF-8");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : lines) {
			String[] eles = line.split("\\s+");
			if (eles.length!=3)
				continue;
			String word=eles[0].replace("_(SPACE)_", " ");
			if (eles[2].equals("T"))
				add(word, Integer.parseInt(eles[1]));
		}
	}

	public String getCommon(String s) {
		List<String> v = Tokenizer.token(s);
		int count = 0;
		for (WordBagItem item : wordBag)
			for (String title : v)
				if (item.word.equals(title)) {
					count++;
					break;
				}
		if (count != v.size())
			return "";
		if (v.size()==0)
			return "";
		String r="";
		for (String title:v)
			r+=title;
		return r;
	}

	public void record(String path) {
		List<String> lines = new ArrayList<String>();
		for (WordBagItem item : wordBag){
			String s=item.word.replaceAll("\\s", "_(SPACE)_");
			s+=" " + item.count;
			lines.add(s);
		}
		try {
			FileUtils.writeLines(new File(path), lines);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addWord(String word,int c) {
		int p = -1;
		for (int i = 0; i < wordBag.size(); i++)
			if (wordBag.get(i).word.equals(word)) {
				p = i;
				break;
			}
		if (p == -1) {
			wordBag.add(new WordBagItem(word, c));
			p=wordBag.size()-1;
		}
		else
			wordBag.get(p).count += c;
		while (p > 0 && wordBag.get(p).count > wordBag.get(p - 1).count) {
			WordBagItem wp = wordBag.get(p);
			wordBag.set(p, wordBag.get(p - 1));
			wordBag.set(p - 1, wp);
			p--;
		}
	}

}
