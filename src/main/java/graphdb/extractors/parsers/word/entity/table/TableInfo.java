package graphdb.extractors.parsers.word.entity.table;

import java.util.ArrayList;
import java.util.List;

import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;
import graphdb.extractors.parsers.word.entity.utils.PlainTextInfo;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TableInfo extends DocumentElementInfo {
    private String					tableCaption;
    private String					tableNumber;
    private int						rowSize		= -1;
    private int						columnSize	= -1;

    private List<TablePartInfo>	tableParts;

    public TableInfo() {
        tableParts = new ArrayList<>();
    }

    public TableRowInfo getRowOfNumber(int rowNum) {

        if (rowNum < 1)
            return null;

        try {
            // 行号从1开始，下标从0开始，按说应该是这样。
            DocumentElementInfo elementInfo = this.subElements.get(rowNum - 1);

            if (elementInfo != null && elementInfo instanceof TableRowInfo) {
                TableRowInfo rowInfo = (TableRowInfo) elementInfo;
                // 未初始化或者行号一致
                if (rowInfo.getRowNum() == -1 || rowInfo.getRowNum() == rowNum)
                    return rowInfo;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getTableCaption() {
        return tableCaption;
    }

    public void setTableCaption(String tableCaption) {
        this.tableCaption = tableCaption;
    }

    public boolean hasRows() {
        return (subElements.size() != 0);
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getRowSize() {
        return rowSize;
    }

    public void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public List<TablePartInfo> getTableParts() {
        return tableParts;
    }

    public void setTableParts(List<TablePartInfo> tableParts) {
        this.tableParts = tableParts;
    }

    public String toHtml() {
        StringBuilder html = new StringBuilder("<table border=\"1\">\n");
        List<DocumentElementInfo> rows = getSubElements();

        for(DocumentElementInfo row : rows) {
            html.append("\n<tr>");
            List<DocumentElementInfo> cellsInARow = row.getSubElements();
            for(DocumentElementInfo cell: cellsInARow){
                if (cell instanceof TableCellInfo) {
                    TableCellInfo cellInfo = (TableCellInfo) cell;
                    PlainTextInfo textCell = (PlainTextInfo) cellInfo.getSubElements().get(0);
                    html.append("  <th>" + textCell.getText() + "</th>\n");
                }
            }
            html.append("</tr>");
        }
        html.append("</table>\n");
        return html.toString();
    }

}