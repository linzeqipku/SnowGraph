package exps.graphlocater.wrapper;

import java.io.Serializable;

public class CodeTermInfo implements Serializable {
	private static final long	serialVersionUID	= -1656083196175000238L;

	private int	id;
	private String original;
	private String mask;
	private int	index;

	public CodeTermInfo() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	public String getCodeTerm() {
		// remove <code> </code> html tags
		int len = original.length();
		return original.substring(6, len - 7).trim();
	}
}
