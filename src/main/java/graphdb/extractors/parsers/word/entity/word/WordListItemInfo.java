package graphdb.extractors.parsers.word.entity.word;

import graphdb.extractors.parsers.word.document.word.WordDocxListParser;
import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class WordListItemInfo extends DocumentElementInfo {

    private XWPFParagraph para=null;
    private String numId=null;

    public WordListItemInfo(XWPFParagraph para, WordDocxListParser analyzer, String numId){
        super();
        this.para=para;
        analyzer.addPara(para);
        this.numId=numId;
    }

    public String getText(){
        return para.getText();
    }

    public XWPFParagraph getPara(){
        return para;
    }

    public String getNumId() {
        return numId;
    }

}
