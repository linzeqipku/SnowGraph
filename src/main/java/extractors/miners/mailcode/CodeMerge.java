package extractors.miners.mailcode;

/**
 * @ClassName: CodeMerge
 * @Description: TODO 合并相邻的代码段落
 * @author: left
 * @date: 2014年3月5日 下午3:34:20
 */

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.neo4j.helpers.collection.Iterators;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fang Lu, fanglupku@gmail.com
 */

public class CodeMerge {

	private static ASTParser astParser;
	static{
		 astParser = ASTParser.newParser(AST.JLS8);
	}
	
	public static List<Segment> continualCodeMerge(List<Segment> srcList) {
		List<Segment> result = new ArrayList<Segment>();

		int preIndex = 0, currentIndex = 1;
		while (currentIndex < srcList.size()) {
			if (!srcList.get(preIndex).isCode()) {
				result.add(srcList.get(preIndex));
				preIndex++;
				currentIndex++;
				continue;
			}

			int tot = srcList.get(currentIndex).getSentenceNumber();
			while (currentIndex < srcList.size() && srcList.get(currentIndex).isCode()) {
				tot += srcList.get(currentIndex).getSentenceNumber();
				if (tot > 100) break;
				currentIndex++;
			}

			
			List<String> sentences = new ArrayList<>();
			for (int i = preIndex; i < currentIndex; i++) {
				Iterators.addToCollection(srcList.get(i).sentenceIterator(), sentences);
			}
			Segment newItem = new Segment(sentences);
            newItem.setCode(true);
			result.add(newItem);
			preIndex = currentIndex;
			currentIndex++;
		}
		if(preIndex < srcList.size())
			result.add(srcList.get(srcList.size()-1));
		return result;
	}
	
	public static List<Segment> SplitCodeSegment(List<Segment> srcList){
		List<Segment> result = new ArrayList<Segment>();
		List<String> list;
 		for (Segment seg : srcList){
			if (!seg.isCode()){
				result.add(seg);
				continue;
			}
			boolean flag = false;
			for (int len = seg.getSentenceNumber(); len >= 1; len--){
				if (flag) break;
				for (int start = 0; start < seg.getSentenceNumber(); start++){
					if (flag) break;
					int end = start + len;
					if (end > seg.getSentenceNumber()) break;
					String s = seg.getText(start, end);
					astParser.setSource(s.toCharArray());
					astParser.setKind(ASTParser.K_STATEMENTS);
					if (s.contains("istData")){
						System.out.println();
					}
					ASTNode ret = astParser.createAST(null);
					if (ret.toString().length() > 5){
						flag = true;
						Segment newSeg1 = new Segment(seg.getSentences().subList(0, start));
						Segment newSeg2 = new Segment(seg.getSentences().subList(start, end));
                        newSeg2.setCode(true);
						Segment newSeg3 = new Segment(seg.getSentences().subList(end, seg.getSentenceNumber()));
						result.add(newSeg1);
						result.add(newSeg2);
						result.add(newSeg3);
						break;
					}
				}
			}
			if (!flag) result.add(seg);
            seg.setCode(false);
		}
		return result;
	}
}
