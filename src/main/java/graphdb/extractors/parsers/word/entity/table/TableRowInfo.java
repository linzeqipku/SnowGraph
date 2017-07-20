package graphdb.extractors.parsers.word.entity.table;

import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TableRowInfo extends DocumentElementInfo {
    private int	rowNum	= -1;

    public int getColumnSize() {
        if (parentElement != null && parentElement instanceof TableInfo) {
            TableInfo tableInfo = (TableInfo) parentElement;
            return tableInfo.getColumnSize();
        }
        return -1;
    }

    public TableCellInfo getCellAtColumn(int n) {
        for (DocumentElementInfo ele : subElements) {
            if (ele != null && ele instanceof TableCellInfo) {
                TableCellInfo cellInfo = (TableCellInfo) ele;
                if (cellInfo.getStartColNum() <= n) {
                    if (cellInfo.getEndColNum() >= n)
                        return cellInfo;
                }
                else
                    return null;
            }
        }
        return null;
    }

    public TableRowInfo getPreviousRow() {
        if (parentElement != null && parentElement instanceof TableInfo) {
            TableInfo tableInfo = (TableInfo) parentElement;
            return tableInfo.getRowOfNumber(this.rowNum - 1);// last row's
            // rownum
        }
        return null;
    }

    public TableRowInfo getNextRow() {
        if (parentElement != null && parentElement instanceof TableInfo) {
            TableInfo tableInfo = (TableInfo) parentElement;
            return tableInfo.getRowOfNumber(this.rowNum + 1);// next row's
            // rownum
        }
        return null;
    }

    public boolean hasCells() {
        return (subElements.size() != 0);
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String toString() {
        return subElements.toString();
    }
}