package graphdb.extractors.miners.codesnippet.stackoverflow.entity;

public class CodeInfo {
	private String content;

	public CodeInfo(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return content;
	}

}
