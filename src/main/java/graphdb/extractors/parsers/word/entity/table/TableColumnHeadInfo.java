package graphdb.extractors.parsers.word.entity.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */

public class TableColumnHeadInfo implements Serializable {
    private List<TableCellInfo> headCells;
    private int						startColNum;
    private int						endColNum;

    public TableColumnHeadInfo() {
        headCells = new ArrayList<TableCellInfo>();
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

    public List<TableCellInfo> getHeadCells() {
        return headCells;
    }

    public String toString() {
        return headCells.toString();
    }

}
