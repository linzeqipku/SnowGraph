package pfr.plugins.refiners.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.tartarus.snowball.ext.EnglishStemmer;

import pfr.PFR;
import pfr.annotations.PropertyDeclaration;
import pfr.plugins.utils.NodeToTextUtil;

public class PfrPluginForTokenizer implements PFR
{

	@PropertyDeclaration
	public static final String TOKENS = "tokens";

	GraphDatabaseService db = null;
	Set<String> focusSet = new HashSet<String>();
	Map<Node, String> nodeToTextMap = new HashMap<Node, String>();

	public void setFocusSet(Set<String> focusSet)
	{
		this.focusSet.clear();
		this.focusSet.addAll(focusSet);
	}

	@Override
	public void run(GraphDatabaseService db)
	{
		this.db = db;
		try
		{
			nodeToTextMap = NodeToTextUtil.prepareNodeToTextMap(db, focusSet);
		}
		catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tokenize();
	}

	void tokenize()
	{
		EnglishStemmer stemmer = new EnglishStemmer();
		try (Transaction tx = db.beginTx())
		{
			for (Node node : nodeToTextMap.keySet())
			{
				List<String> tokens = new ArrayList<String>();
				String content = "<html>" + nodeToTextMap.get(node) + "</html>";
				content = Jsoup.parse(content).text();
				for (String word : content.split("[^A-Za-z]+"))
				{
					if (word.length() == 0)
						continue;
					List<String> camelTokens = camelSplit(word);
					for (String token : camelTokens)
					{
						stemmer.setCurrent(token);
						stemmer.stem();
						tokens.add(stemmer.getCurrent());
					}
				}
				String tokensLine = "";
				for (String token : tokens)
					tokensLine += token + " ";
				tokensLine = tokensLine.trim();
				node.setProperty(TOKENS, tokensLine);
			}
			tx.success();
		}
	}

	static List<String> camelSplit(String e)
	{
		List<String> r = new ArrayList<String>();
		Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
		if (m.find())
		{
			String s = m.group().toLowerCase();
			r.add(s);
			if (s.length() < e.length())
				r.addAll(camelSplit(e.substring(s.length())));
		}
		return r;
	}

}
