package extractors.miners.mailqa.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableNameUtils {

	public static ArrayList<String>	dict	= new ArrayList<String>();

	public static void initDict() throws IOException {
		// String fileContent = "";
		// @SuppressWarnings("resource")
		// FileChannel reader = new RandomAccessFile("data/wordnet.words",
		// "r").getChannel();
		// ByteBuffer bb = ByteBuffer.allocate(1024*1024);
		// while(reader.read(bb) != -1) {
		// bb.flip();
		// fileContent += FileOperator.bb_to_str(bb);
		// bb.clear();
		// }
		// reader.close();
		// //System.out.println(fileContent.length());
		// String words[] = fileContent.split(" |_|\\n|\\.");
		// //System.out.println(words.length);
		// for( String word : words){
		// dict.add(word);
		// }
		// System.out.println("dictionary size is :"+dict.size());
	}

	/**
	 * 
	 * 1.UC behind me, UC followed by LC in front of me
	 * 
	 * XMLParser AString PDFLoader /\ /\ /\
	 * 
	 * 2.non-UC behind me, UC in front of me
	 * 
	 * MyClass 99Bottles /\ /\
	 * 
	 * 3.Letter behind me, non-letter in front of me
	 * 
	 * GL11 May5 BFG9000 /\ /\ /\
	 */
	public static String[] splitCamelCase(String s) {
		s = removeUnderline(s);
		return s.replaceAll(
				String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
						"(?<=[A-Za-z])(?=[^A-Za-z])"), " ").split(" ");
	}

	public static int countCamelCase(String text) {
		int count = 0;
		text = removeUnderline(text);
		Pattern pattern = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		// Pattern pattern =
		// Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * remove the underline and replace the letter after the underline with its
	 * upper
	 */
	private static String removeUnderline(String s) {
		String[] ss = s.split("_");
		s = "";
		for (String temp : ss) {
			if (temp.length() != 0) {
				temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
			}
			s += temp;
		}
		return s;
	}

	public static void testCamelCaseSplit() {
		String[] tests = { "lowercase", // [lowercase]
				"Class", // [Class]
				"MyClass", // [My Class]
				"HTML", // [HTML]
				"PDFLoader", // [PDF Loader]
				"AString", // [A String]
				"SimpleXMLParser", // [Simple XML Parser]
				"GL11Version", // [GL 11 Version]
				"99Bottles", // [99 Bottles]
				"May5", // [May 5]
				"BFG9000", // [BFG 9000]
				"This_Test", // [This Test]
				"this_test", // [This test]
				"This_test", // [This Test]
				"This_test_", // [This Test]
				"ThisIsALongTest", // [This Is A Long Test]
				"ThisIS11Test", // [This Is 11 Test]
				"thisIs11_LongTest",// [This Is 11 Long Test]
		};

		for (String test : tests) {
			// System.out.println("[" + splitCamelCase(test) + "]");
			String words[] = splitCamelCase(test);
			System.out.print("[");
			for (int i = 0; i < words.length; i++) {
				if (i == words.length - 1) {
					System.out.print(words[i]);
				}
				else {
					System.out.print(words[i] + " ");
				}
			}
			System.out.println("]");
		}
	}

	/**
	 * @param clazz
	 *            the class name of the class variable
	 * @param variable
	 *            the variable name
	 * @return a meaningful variable name 1. split the variable name, with "_"
	 *         or camel Case
	 * 
	 *         2. check if the variable is a meaningful word or meaningful words
	 *         a. the variable is a combination of couple of words(after
	 *         splitting) or word's root; eg. String str, XMLPattern XmlPattern,
	 *         XMLPattern xmlPattern, XMLPattern xmlpattern,XMLPattern
	 *         xml_pattern b. the variable is short(less than 2 letters) but the
	 *         clazz is kind of regular class, eg.int i, j, k, m, n float f
	 *         double d byte b char c, ch long l String s;
	 * 
	 *         3. if the variable is not meaningful, use the camel case of its
	 *         clazz to replace the variable name, if renamed,add number behind
	 * 
	 */
	public static String meaningfulClassVariable(String clazz, String variable) {
		boolean isMeaningful = checkIfMeaningFul(clazz, variable);
		if (isMeaningful)
			return variable;

		else {
			String words[] = splitCamelCase(clazz);
			String ret = words[0].toLowerCase();
			for (int i = 1; i < words.length; i++) {
				ret += words[i];
			}

			int renameNumber = 1;
			String temp2 = ret;
			while (isRenamed(ret)) { // if the variable is renamed, add number
										// behind
				ret = temp2 + renameNumber;
				renameNumber++;
			}
			return ret;
		}
	}

	private static boolean isRenamed(String variable) {
		return false;
	}

	private static boolean checkIfMeaningFul(String clazz, String variable) {
		boolean isMeaningFul = false;

		// split the variable
		String words[] = splitCamelCase(variable);

		String regularClasses[] = { "int", "long", "char", "byte", "float", "double", "String",
				"boolean" };
		LinkedList<String> classList = new LinkedList<String>();
		for (String temp : regularClasses) {
			classList.add(temp);
		}
		if (classList.contains(clazz)) {
			isMeaningFul = true;
		}
		else if (words.length == 1) { // the variable is only one word
			if (variable.length() <= 3) { // too short
				if (classList.contains(clazz)) {
					isMeaningFul = true;
				}
				else {
					isMeaningFul = false;
				}
			}
			else if (variable.length() > 3) { // check if it is a word or a
												// word's root;
				isMeaningFul = isWordCheck(clazz, variable); // check if the
																// variable is a
																// word or
																// word's root;
			}
		}
		else if (words.length > 1) {
			// the variable is a combination of couple of words or word's root
			// check if each word of the variable is a meaningful word
			isMeaningFul = isWordCheck(clazz, variable);

		}

		return isMeaningFul;
	}

	private static boolean isWordCheck(String clazz, String variable) {
		boolean isWord = false;

		String clazzWords[] = splitCamelCase(clazz);
		// LinkedList<String> clazzList = new LinkedList<String>();
		// for( String temp : clazzWords){
		// clazzList.add(temp);
		// }

		String variableWords[] = splitCamelCase(variable);

		int count = 0;
		for (String word : variableWords) {
			word = word.toLowerCase();
			isWord = false;
			for (String clazzWord : clazzWords) {
				clazzWord = clazzWord.toLowerCase();
				if (clazzWord.indexOf(word) == 0) {
					isWord = true;
					count++;
					break;
				}
			}
			if (!isWord) {
				if (isNumeric(word)) {
					count++;
				}
				else if (word.length() <= 2) {
					// too short, unmeaningful
				}
				else if (isWordOrRoot(word))
					count++;
			}
		}

		if (count == variableWords.length) { // each of the split word is a
												// meaning ful word
			return true;
		}
		else
			return false;

	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isWordOrRoot(String word) {
		if (dict.contains(word)) {
			System.out.println("dict contains :" + word);
			return true;
		}
		else {
			System.out.println("dict not contain:" + word);
			return false;
		}
	}

	public static void testMeaningfulClassVariable() {
		LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
		hashMap.put("int", "i");
		hashMap.put("boolean", "flag");
		hashMap.put("XMLPattern", "dasdsa");
		hashMap.put("ThisTest", "thTest");
		hashMap.put("DictTest", "someUseful");
		hashMap.put("ThatTest", "index");
		hashMap.put("AnotherTest", "index2");
		hashMap.put("YetAnother", "INdex");
		hashMap.put("ThatThat", "tThat");
		hashMap.put("YYTest", "indexYsds");
		// hashMap.put("", "");
		// hashMap.put("", "");
		// hashMap.put("", "");
		// hashMap.put("", "");
		// hashMap.put("", "");
		// hashMap.put("", "");

		Iterator<String> iterator = hashMap.keySet().iterator();
		while (iterator.hasNext()) {
			String clazz = iterator.next();
			String variable = hashMap.get(clazz);
			System.out.println("[" + clazz + "," + variable + "]" + " --->" + "[" + clazz + ","
					+ meaningfulClassVariable(clazz, variable) + "]");
		}
	}

	public static void main(String args[]) throws IOException {
		// initDict();
		// System.out.println("#################################");
		// testCamelCaseSplit();
		// System.out.println("#################################");
		// testMeaningfulClassVariable();
		// System.out.println("#################################");
		String text = "this is a camelCase test WhatThis how";
		System.out.println(countCamelCase(text));

	}

}
