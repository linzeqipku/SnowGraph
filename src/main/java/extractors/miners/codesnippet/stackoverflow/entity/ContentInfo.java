package extractors.miners.codesnippet.stackoverflow.entity;

import java.util.ArrayList;
import java.util.List;

public class ContentInfo {

	private String					content;
	private List<CodeInfo>		paragraphList;

	public ContentInfo(String content) {
		paragraphList = new ArrayList<>();
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public List<CodeInfo> getParagraphList() {
		return paragraphList;
	}

	@Override
	public String toString() {
		return content;
	}

}
