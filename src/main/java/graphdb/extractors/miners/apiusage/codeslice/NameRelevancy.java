package graphdb.extractors.miners.apiusage.codeslice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.EnglishStemmer;

public class NameRelevancy implements Comparable<NameRelevancy>
{
	public static final String[] stopWordsList = { "assert", "test", "check", "assertion" };
	private String testMethodName;
	private String apiMethodName;

	private List<String> testMethodWords;
	private List<String> apiMethodWords;

	private List<String> stemmedTestWords;
	private List<String> stemmedAPIWords;

	private List<String> commonWords;

	private double commonWordsRatio;

	// invoke root
	public void execute()
	{
		splitMethodNames();
		stemWords();
		findCommonWords();

		commonWordsRatio = (double) commonWords.size() * (double) commonWords.size()
				/ (double) stemmedAPIWords.size() / (double) stemmedTestWords.size();
	}

	private void splitMethodNames()
	{
		apiMethodWords = new ArrayList<>(Arrays.asList(camelCaseSplit(apiMethodName)));
		testMethodWords = new ArrayList<>(Arrays.asList(camelCaseSplit(testMethodName)));
		testMethodWords.removeAll(Arrays.asList(stopWordsList));
	}

	// Trim digits in word and stem the word
	private void stemWords()
	{
		EnglishStemmer stemmer = new EnglishStemmer();

		stemmedAPIWords = new ArrayList<>();
		for (int i = 0; i < apiMethodWords.size(); i++)
		{
			// trim digit
			String word = apiMethodWords.get(i);
			String digitTrimedWord = trimDigits(word);
			if (digitTrimedWord.equals(""))
			{
				apiMethodWords.remove(i);
				i--;
			}
			else
			{
				// stem
				word = digitTrimedWord;
				stemmer.setCurrent(word);
				stemmer.stem();
				stemmedAPIWords.add(stemmer.getCurrent());
			}
		}

		stemmedTestWords = new ArrayList<>();
		for (int i = 0; i < testMethodWords.size(); i++)
		{
			// trim digit
			String word = testMethodWords.get(i);
			String digitTrimedWord = trimDigits(word);
			if (digitTrimedWord.equals(""))
			{
				testMethodWords.remove(word);
				i--;
			}
			else
			{
				// stem
				word = digitTrimedWord;
				stemmer.setCurrent(word);
				stemmer.stem();
				stemmedTestWords.add(stemmer.getCurrent());
			}
		}
	}

	private void findCommonWords()
	{
		List<String> commonWordsList = new ArrayList<>();
		List<Integer> matchedAPIWords = new ArrayList<>();
		for (int i = 0; i < stemmedTestWords.size(); i++)
		{
			boolean matched = false;
			for (int j = 0; j < stemmedAPIWords.size(); j++)
			{
				if (matchedAPIWords.contains(j))
					continue;
				if (stemmedTestWords.get(i).equalsIgnoreCase(stemmedAPIWords.get(j)))
				{
					commonWordsList.add(stemmedTestWords.get(i));
					matched = true;
					matchedAPIWords.add(j);
					break;
				}
			}
			if (matched)
				continue;
		}
		commonWords = commonWordsList;
	}

	private static String trimDigits(String word)
	{
		return word.replaceAll("[0-9]", "");
	}

	// getIDNumber --> get ID Number; 注意连续大写字母的切词
	// 包含数字的切词 Jira432html5 --> jira432 html5
	private static String[] camelCaseSplit(String string)
	{
		List<String> splitWords = new ArrayList<>();

		String[] subStrings = Pattern.compile("_").split(string);
		for (String str : subStrings)
		{
			List<Integer> camelPos = new ArrayList<>();
			camelPos.add(0);
			for (int i = 1; i < str.length(); i++)
			{
				if (str.substring(i, i + 1).matches("[A-Z]"))
				{
					try
					{
						if (!str.substring(i - 1, i).matches("[A-Z]")
								|| !str.substring(i + 1, i + 2).matches("[A-Z]"))
							camelPos.add(i);
					}
					catch (StringIndexOutOfBoundsException e)
					{
						if (i + 1 < str.length())
							if (!str.substring(i + 1).matches("[A-Z]"))
								camelPos.add(i);
					}
				}
				else if (str.substring(i, i + 1).matches("[A-Z|a-z]"))
				{
					if (str.substring(i - 1, i).matches("[0-9]"))
						camelPos.add(i);
				}
			}

			for (int i = 0; i < camelPos.size() - 1; i++)
			{
				splitWords.add(str.substring(camelPos.get(i), camelPos.get(i + 1)).toLowerCase());
			}
			splitWords.add(str.substring(camelPos.get(camelPos.size() - 1)).toLowerCase());
		}

		return splitWords.toArray(new String[splitWords.size()]);
	}

	@Deprecated
	private String getLongestCommonSubString(String str1, String str2)
	{
		int size = Math.min(str1.length(), str2.length());
		boolean found = false;

		int closeSize = size <= 1 ? 1 : (size / 2);

		while (size > closeSize && !found)
		{
			for (int i = 0; i <= str1.length() - size; i++)
				for (int j = 0; j <= str2.length() - size; j++)
				{
					if (str1.substring(i, size + i).equalsIgnoreCase(str2.substring(j, j + size)))
						return str1.substring(i, size + i).toLowerCase();
				}
			size--;
		}

		return null;
	}

	@SuppressWarnings("unused")
	private String[] findCommonWords0(String[] testMethodWords, String[] apiMethodWords)
	{
		List<String> cmn = new ArrayList<>();

		// 更准确的匹配和检索，以后实现
		// 可以考虑动态规划方法的检测
		
		List<Integer> matched1 = new ArrayList<>();
		List<Integer> matched2 = new ArrayList<>();

		for (int i = 0; i < testMethodWords.length; i++)
		{
			if (matched1.contains(i))
				continue;
			for (int j = 0; j < apiMethodWords.length; j++)
			{
				if (matched2.contains(j))
					continue;

				String common = getLongestCommonSubString(testMethodWords[i], apiMethodWords[j]);
				if (common != null)
				{
					matched1.add(i);
					matched2.add(j);
					cmn.add(common);
				}
			}
		}

		return cmn.toArray(new String[0]);
	}

	@Deprecated
	public String[] findCommonWordsByString(String str1, String str2)
	{
		String[] words1 = camelCaseSplit(str1);
		String[] words2 = camelCaseSplit(str2);

		List<String> cmn = new ArrayList<>();

		List<Integer> matched1 = new ArrayList<>();
		List<Integer> matched2 = new ArrayList<>();

		for (int i = 0; i < words1.length; i++)
		{
			if (matched1.contains(i))
				continue;
			for (int j = 0; j < words2.length; j++)
			{
				if (matched2.contains(j))
					continue;

				String common = getLongestCommonSubString(words1[i], words2[j]);
				if (common != null)
				{
					matched1.add(i);
					matched2.add(j);
					cmn.add(common);
				}
			}
		}

		return cmn.toArray(new String[0]);
	}
	

	private boolean hasSameRelevancyWith(Object o)
	{
		if (o != null && o instanceof NameRelevancy)
			return commonWordsRatio == ((NameRelevancy) o).commonWordsRatio;
		else
			return false;
	}

	@Override
	public int compareTo(NameRelevancy r)
	{
		return new Double(commonWordsRatio).compareTo(new Double(r.getCommonWordsRatio()));
	}

	// getters and setters
	public String getApiMethodName()
	{
		return apiMethodName;
	}

	public void setApiMethodName(String apiMethodName)
	{
		this.apiMethodName = apiMethodName;
	}

	public String getTestMethodName()
	{
		return testMethodName;
	}

	public void setTestMethodName(String testMethodName)
	{
		this.testMethodName = testMethodName;
	}

	public double getCommonWordsRatio()
	{
		return commonWordsRatio;
	}

	private List<String> getTestMethodWords()
	{
		return testMethodWords;
	}

	public List<String> getApiMethodWords()
	{
		return apiMethodWords;
	}

	private List<String> getStemmedTestWords()
	{
		return stemmedTestWords;
	}

	public List<String> getStemmedAPIWords()
	{
		return stemmedAPIWords;
	}

	private List<String> getCommonWords()
	{
		return commonWords;
	}


	public static void main(String[] args)
	{
		NameRelevancy nr = new NameRelevancy();
		nr.setApiMethodName("assertJira322html5");
		nr.setTestMethodName("778assertionJira432HHHtml45");

		nr.execute();

		for (String string : nr.getTestMethodWords())
		{
			System.out.println(string);
		}
		System.out.println();
		for (String string : nr.getStemmedTestWords())
		{
			System.out.println(string);
		}
		System.out.println();
		for (String string : nr.getCommonWords())
		{
			System.out.println(string);
		}
		System.out.println("\n" + nr.getCommonWordsRatio());

		NameRelevancy c = new NameRelevancy();
		System.out.println(nr.hasSameRelevancyWith(c));

	}

}
