package extractors.miners.mailqa.tag;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import extractors.miners.mailqa.utils.ReadFile;

public class ProjectWords {

	public static HashSet<String>	projectWords	= new HashSet<String>();

	public static final String		FILE_PATH		= "/codeelements.txt";

	public static void readProjectWordsFromFile() {
		if (projectWords.size() == 0) {
			/*String path = "";
			try {
				path = ProjectWords.class.getResource("/").toURI().getPath();
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}*/
			String path = "E:\\SnowGraph\\src\\main\\java\\extractors\\miners\\mailqa";
			ArrayList<String> lines = ReadFile.readFileLines(path + FILE_PATH);
			for (String word : lines) {
				projectWords.add(word);
			}
		}
	}

	public static HashSet<String> getProjectWords() {
		if (projectWords.size() == 0) {
			readProjectWordsFromFile();
		}
		return projectWords;
	}

	public static void main(String args[]) {
		HashSet<String> temp = getProjectWords();
		for (String word : temp) {
			System.out.println(word);
		}
		System.out.println(temp.size());
	}

}
