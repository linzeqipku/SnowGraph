/**
 * 
 */
package pfr.plugins.parsers.javacode.extractor.srcparser.entity;

/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-10-18 上午11:15:18
 * @version 1.0
 */
public abstract class CommonInfo {
	protected long id = -1;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	public abstract String getHashName();
}
