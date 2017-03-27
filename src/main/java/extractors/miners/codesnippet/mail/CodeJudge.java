package extractors.miners.codesnippet.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @ClassName: CodeJudge
 * @Description: TODO judge whether a code
 * @author: left
 * @date: 2014年3月5日 上午10:30:16
 */

public class CodeJudge {

	public static boolean isCode(String content) {
		BufferedReader br = new BufferedReader(new StringReader(content));
		String line = null;
		int totalLOC = 0, isCodeLOC = 0;
		boolean MUST_OCCUR = false;
		try {
			while ((line = br.readLine()) != null) {
				// only handle non-blank lines
				// System.out.println(line);
				if (line.trim().length() > 0) {
					if ((!MUST_OCCUR) && hasMustOccurSymbol(line)) {
						MUST_OCCUR = true;
					}
					if (isCodeLine(line.trim())) {
						isCodeLOC++;
					}
					totalLOC++;
				}
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(totalLOC + "/" + isCodeLOC + "/" + MUST_OCCUR +
		// "/" + ((double)isCodeLOC / ((double) totalLOC)) );
		if (totalLOC > 0 && MUST_OCCUR) {
			if ((double) isCodeLOC / ((double) totalLOC) > 0.2) {
				// System.out.println("CODE------------------");
				// System.out.println(content);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}

	}

	public final static String[]	codeEndSymbol		= { "{", "}", ";", "=" };
	public final static String[]	codeKeywordSymbol	= { "public", "private", "protected",
			"return", "package", "import"				};
	public final static String[]	mustOccurSymbol		= { "(", ")", "=", "{", "}" };

	public static boolean hasMustOccurSymbol(String line) {
		for (String s : mustOccurSymbol)
			if (line.contains(s))
				return true;
		return false;
	}

	public static boolean isCodeLine(String line) {
		for (String s : codeEndSymbol) {
			if (line.endsWith(s)) {
				return true;
			}
		}
		for (String s : codeKeywordSymbol) {
			if (line.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

}
