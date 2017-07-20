package graphdb.extractors.parsers.word.entity.table;

import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;
import graphdb.extractors.parsers.word.entity.utils.PlainTextInfo;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TableCellInfo extends DocumentElementInfo {
    private int	startRowNum	= -1;
    private int	endRowNum	= -1;
    private int	startColNum	= -1;
    private int	endColNum	= -1;

    // vertically merged
    public boolean isVMerged() {
        return startRowNum < endRowNum;
    }

    // horizontally merged
    public boolean isHMerged() {
        return startColNum < endColNum;
    }

    public String getText() {
        if (subElements == null || subElements.get(0) == null
                || !(subElements.get(0) instanceof PlainTextInfo))
            return null;
        PlainTextInfo plainTextInfo = (PlainTextInfo) (subElements.get(0));
        return plainTextInfo.getText();
    }

    public boolean hasText() {
        return (subElements.size() != 0);
    }

    public int getStartRowNum() {
        return startRowNum;
    }

    public void setStartRowNum(int startRowNum) {
        this.startRowNum = startRowNum;
    }

    public int getEndRowNum() {
        return endRowNum;
    }

    public void setEndRowNum(int endRowNum) {
        this.endRowNum = endRowNum;
    }

    public int getStartColNum() {
        return startColNum;
    }

    public void setStartColNum(int startColNum) {
        this.startColNum = startColNum;
    }

    public int getEndColNum() {
        return endColNum;
    }

    public void setEndColNum(int endColNum) {
        this.endColNum = endColNum;
    }

    public String toString()
    {
        return getText();
    }
}
