package extractors.miners.mailqa.tag;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import extractors.miners.mailqa.utils.ReadFile;

public class FunctionWords {

	public static HashSet<String>	functionWords		= new HashSet<String>();

	public static final String		FUNCTION_WORDS_FILE	= "/functionwords.txt";

	public static void readFunctionWordsFromFile() {
		if (functionWords.size() == 0) {
			/*String path = "";
			try {
				path = FunctionWords.class.getResource("/").toURI().getPath();
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}*/
			String path = "E:\\SnowGraph\\src\\main\\java\\extractors\\miners\\mailqa";
			ArrayList<String> lines = ReadFile.readFileLines(path + FUNCTION_WORDS_FILE);
			for (String word : lines) {

				functionWords.add(word);
			}
		}
	}

	public static HashSet<String> getFunctionWordsSet() {
		if (functionWords.size() == 0)
			readFunctionWordsFromFile();
		return functionWords;
	}

	public static void main(String args[]) {
		HashSet<String> temp = getFunctionWordsSet();
		for (String word : temp) {
			System.out.println(word);
		}
		System.out.println(temp.size());
	}
}
