package graphlocater.utils;

import edu.stanford.nlp.trees.Tree;

import java.util.List;

public class TreeUtils {
    public static String interpretTreeToString(Tree tree) {
		List<Tree> words = tree.getLeaves();
		String str = "";
		int tot = 0;
		int tot1 = 0;
		boolean flag = false;
		for (Tree word : words) {
			String s = word.toString();
			if (s.equals("''") || s.equals("``")) {
				tot ^= 1;
				if (tot == 1) {
					flag = true;
					str = str + " ";
					str = str + "\"";
					continue;
				}
				else {
					str = str + "\"";
				}
			}
			else if (s.equals("'") || s.equals("`")) {
				tot1 ^= 1;
				if (tot1 == 1) {
					flag = true;
					str = str + " ";
					str = str + "'";
					continue;
				}
				else {
					str = str + "'";
				}
			}
			else if (s.toUpperCase().equals("-LRB-")) {
				flag = true;
				str = str + " ";
				str = str + word;
				continue;
			}
			else if (s.toUpperCase().equals("-RRB-")) {
				str = str + word;
			}
			else if (s.toUpperCase().equals("-LCB-")) {
				flag = true;
				str = str + " ";
				str = str + word;
				continue;
			}
			else if (s.toUpperCase().equals("-RCB-")) {
				str = str + word;
			}
			else if (s.toUpperCase().equals("-LSB-")) {
				flag = true;
				str = str + " ";
				str = str + word;
				continue;
			}
			else if (s.toUpperCase().equals("-RSB-")) {
				str = str + word;
			}
			else if (s.startsWith(".")) {
				str = str + word;
			}
			else if (s.equals(",")) {
				str = str + word;
			}
			else if (s.equals(":")) {
				str = str + word;
			}
			else if (s.equals(";")) {
				str = str + word;
			}
			else if (s.equals("?")) {
				str = str + word;
			}
			else if (s.equals("!")) {
				str = str + word;
			}
			else if (s.equals("n't")) {
				str = str + word;
			}
			else if (s.equals("'m")) {
				str = str + word;
			}
			else if (s.equals("'s")) {
				str = str + word;
			}
			else if (s.equals("'re")) {
				str = str + word;
			}
			else if (s.equals("'ve")) {
				str = str + word;
			}
			else if (flag) {
				str = str + word;
			}
			else {
				str = str + " ";
				str = str + word;
			}
			flag = false;
		}
		str = str.replace("-LRB-", "(");
		str = str.replace("-RRB-", ")");
		str = str.replace("-LCB-", "{");
		str = str.replace("-RCB-", "}");
		str = str.replace("-LSB-", "[");
		str = str.replace("-RSB-", "]");
		str = str.replace("-lrb-", "(");
		str = str.replace("-rrb-", ")");
		str = str.replace("-lcb-", "{");
		str = str.replace("-rcb-", "}");
		str = str.replace("-lsb-", "[");
		str = str.replace("-rsb-", "]");
		return str.trim();
	}

}
