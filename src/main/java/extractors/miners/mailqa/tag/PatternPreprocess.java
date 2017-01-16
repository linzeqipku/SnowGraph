package extractors.miners.mailqa.tag;

import java.util.ArrayList;

//import extractors.miners.mailqa.dao.TagDao;
import extractors.miners.mailqa.entity.Tag;
import extractors.miners.mailqa.utils.WriteFile;

/**
 * @ClassName: PatternPreprocess
 * @Description: TODO 挖掘pattern的一些预处理工作 包括 1、从数据库中读取用户选择的问句，对问句进行标注
 *               2、将标注后的问句写回数据库，同时将标注后的问句转化为向量 3、将向量保存至文件以备之后的算法使用
 * @author: left
 * @date: 2014年3月19日 下午4:05:20
 */

public class PatternPreprocess {

	public ArrayList<Tag>	tagList	= new ArrayList<Tag>();

	//public TagDao			tagDao	= new TagDao();

	public void process() {

	}

	private void selectTags() {
		ArrayList<Tag> list = new ArrayList<>();
		//ArrayList<Tag> list = tagDao.getAllTags();
		System.out.println("origin tag list num is :" + list.size());

		// 仅处理被选出了问题句子的邮件
		for (Tag tag : list) {
			if (tag.getRaw_text() != null && tag.getRaw_text().length() > 1) {
				tagList.add(tag);
			}
		}
		Tagger tagger = new Tagger();
		ArrayList<String> taggedStringVector = new ArrayList<String>();
		ArrayList<Integer> taggedVector = new ArrayList<Integer>();

		String path = "D:/lab/final/tag.txt";
		int count = 0;
		for (Tag tag : tagList) {
			// String tagged = Tagger.getTaggedString(tag.getRaw_text());
			taggedStringVector = tagger.getTagVectorOfText(tag.getRaw_text());
			taggedVector = tagger.getVector(tag.getRaw_text());
			int i = 0;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for (i = 0; i < taggedVector.size() - 1; i++) {
				sb.append(taggedVector.get(i) + " -1 ");
			}
			sb.append(taggedVector.get(i) + " -2\n");
			String temp = Tagger.getTaggedString(tag.getRaw_text());
			// WriteFile.writeStringToFileAppend(temp + "\n", path);
			WriteFile.writeStringToFileAppend(sb.toString(), path);
			// count++;
			// if(count > 10) break;
		}

		//
		// for(String str : taggedStringVector) {
		// System.out.print(str + "_");
		// }
		// System.out.println();
		// for(Integer i : taggedVector) {
		// System.out.print(i + "-");
		// }
		// System.out.println();

	}

	public static void main(String args[]) {
		PatternPreprocess pp = new PatternPreprocess();
		pp.selectTags();
	}
}
