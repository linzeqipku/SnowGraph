package pfr.plugins.parsers.javacode.extractor.srcparser.entity;

/**
 *  sei.pku.edu.cn
 *  @author Jin Jing
 *	2012-4-10 上午11:08:43
 */
public class MysqlFilteredType extends CommonInfo {
	//private long id;
	private String fqn;
	private String ctype;
	private String source;
	/*public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}*/
	public String getFqn() {
		return fqn;
	}
	public void setFqn(String fqn) {
		this.fqn = fqn;
	}
	public String getCtype() {
		return ctype;
	}
	public void setCtype(String ctype) {
		this.ctype = ctype;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	@Override
	public String getHashName() {
		return "";
	}
}
