package extractors.miners.codesnippet.stackoverflow;

import extractors.miners.codesnippet.stackoverflow.entity.CodeInfo;
import extractors.miners.codesnippet.stackoverflow.entity.ContentInfo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class StackOverflowParser {
	public static ContentInfo parse(String content) {
		ContentInfo contentInfo = new ContentInfo(content);
		parseHTMLContent(contentInfo);

		return contentInfo;
	}

	private static ContentInfo parseHTMLContent(ContentInfo content) {
		String contentText = content.getContent();
		if (contentText == null) return content;

		Document htmlRoot = Jsoup.parse(content.getContent(), "UTF-8");

		List<CodeInfo> paragraphList = parseHTMLNodeToParagraphs(htmlRoot);
		if (paragraphList != null && paragraphList.size() > 0)
			content.getParagraphList().addAll(paragraphList);
		else {
			CodeInfo codeInfo = new CodeInfo(content.getContent());
			content.getParagraphList().add(codeInfo);
		}

		return content;
	}

	private static List<CodeInfo> parseHTMLNodeToParagraphs(Node node) {
		List<CodeInfo> paragraphList = new ArrayList<>();
		List<Node> childNodes = node.childNodes();
		for (Node childNode : childNodes) {
			if (childNode.nodeName().equals("p") || childNode.nodeName().equals("li")) continue;
			if (childNode.nodeName().equals("pre"))
				childNode.childNodes().stream()
						.filter(n -> n.nodeName().equals("code"))
						.map(n -> new CodeInfo(StringEscapeUtils.unescapeHtml4(((Element) n).text())))
						.forEach(paragraphList::add);
			else paragraphList.addAll(parseHTMLNodeToParagraphs(childNode));
		}
		return paragraphList;
	}

}
