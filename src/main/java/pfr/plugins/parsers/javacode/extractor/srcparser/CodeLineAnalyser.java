/**
 * 
 */
package pfr.plugins.parsers.javacode.extractor.srcparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-6-25 下午11:05:08
 * @version 0.1 2012-12-15
 */
public class CodeLineAnalyser {
	private int normalLines;
	private int commentLines;
	private int whiteLines;
	
	public void getCodelines(File file) {
		normalLines = 0;
		commentLines = 0;
		whiteLines = 0;

		boolean comment = false;
		String line = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.matches("^[\\s&&[^\\n]]*$")) {
					whiteLines++;
				}
				else if (line.startsWith("/*") && !line.endsWith("*/")) {
					commentLines++;
					comment = true;
				}
				else if (true == comment) {
					commentLines++;
					if (line.endsWith("*/")) {
						comment = false;
					}
				} 
				else if (line.startsWith("//")) {
					commentLines++;
				} 
				else {
					normalLines++;
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the normalLines
	 */
	public int getNormalLines() {
		return normalLines;
	}

	/**
	 * @param normalLines the normalLines to set
	 */
	public void setNormalLines(int normalLines) {
		this.normalLines = normalLines;
	}

	/**
	 * @return the commentLines
	 */
	public int getCommentLines() {
		return commentLines;
	}

	/**
	 * @param commentLines the commentLines to set
	 */
	public void setCommentLines(int commentLines) {
		this.commentLines = commentLines;
	}

	/**
	 * @return the whiteLines
	 */
	public int getWhiteLines() {
		return whiteLines;
	}

	/**
	 * @param whiteLines the whiteLines to set
	 */
	public void setWhiteLines(int whiteLines) {
		this.whiteLines = whiteLines;
	}
	
	
}
