package graphlocater.wrapper;

import java.io.Serializable;

public class SentenceInfo implements Serializable {
	private static final long	serialVersionUID	= 1896002261136446138L;
	public static final String	TABLE_NAME			= "sentences";
	private int	id;
	private String text;

	/**
	 * A sentenceInfo consists of several phrases.
	 */
	private int[]				phrasesId;
	private String				treeString;
	private String				codeTermString;

	public SentenceInfo() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int[] getPhrasesId() {
		return phrasesId;
	}

	public void setPhrasesId(int[] phrasesId) {
		this.phrasesId = phrasesId;
	}

	public String getTreeString() {
		return treeString;
	}

	public void setTreeString(String treeString) {
		this.treeString = treeString;
	}

	public String getCodeTermString() {
		return codeTermString;
	}

	public void setCodeTermString(String codeTermString) {
		this.codeTermString = codeTermString;
	}
}
