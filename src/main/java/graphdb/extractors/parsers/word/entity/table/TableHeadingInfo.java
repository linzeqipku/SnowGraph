package graphdb.extractors.parsers.word.entity.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */

public class TableHeadingInfo implements Serializable {
    private List<TableColumnHeadInfo> columnHeads;

    public TableHeadingInfo() {
        columnHeads = new ArrayList<>();
    }

    public List<TableColumnHeadInfo> getColumnHeads() {
        return columnHeads;
    }

    public boolean isEmptyHeading() {
        if (columnHeads == null || columnHeads.size() == 0)
            return true;
        return false;
    }

    /*
     * @Description 单元格对应的表头有哪些，将所有有交叉的都返回。
     */
    public List<TableColumnHeadInfo> getOverlappedColumnHeads(TableCellInfo dataCell) {
        List<TableColumnHeadInfo> overlappedHeads = new ArrayList<>();
        int start = dataCell.getStartColNum();
        int end = dataCell.getEndColNum();

        for (int i = 0; i < columnHeads.size(); i++) {
            TableColumnHeadInfo head = columnHeads.get(i);
            if (head.getStartColNum() > end)
                break;
            if (start <= head.getEndColNum() && end >= head.getStartColNum())
                overlappedHeads.add(head);
        }

        return overlappedHeads;
    }

    public String toString() {
        return columnHeads.toString();
    }
}
