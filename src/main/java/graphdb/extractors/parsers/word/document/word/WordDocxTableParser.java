package graphdb.extractors.parsers.word.document.word;

import graphdb.extractors.parsers.word.document.table.WTTablePartParser;
import graphdb.extractors.parsers.word.entity.table.TableCellInfo;
import graphdb.extractors.parsers.word.entity.table.TableInfo;
import graphdb.extractors.parsers.word.entity.table.TableRowInfo;
import graphdb.extractors.parsers.word.entity.utils.PlainTextInfo;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class WordDocxTableParser {

    public static TableInfo parseWordTable(XWPFTable table, String caption) {
        if (table == null) {
            return null;
        }

        TableInfo tableInfo = new TableInfo();

        tableInfo.setTableCaption(caption);
        tableInfo.setRowSize(table.getNumberOfRows());

        List<XWPFTableRow> rows = table.getRows();
        int rowNum = 0;
        // 遍历所有行
        for (XWPFTableRow row : rows) {
            TableRowInfo rowInfo = new TableRowInfo();
            if (rowInfo != null)
                tableInfo.addSubDocumentElement(rowInfo);// 添加到父节点：table

            rowNum++;
            rowInfo.setRowNum(rowNum);

            List<XWPFTableCell> cells = row.getTableCells();
            int colNum = 0;
            // 遍历所有列（单元格）
            for (XWPFTableCell cell : cells) {
                TableCellInfo cellInfo = null;

                int startColNum = colNum + 1;
                int columnSpan = getGridSpan(cell);
                colNum += columnSpan;

                // 判断是否跨行，以及是否是跨行单元格的第一行
                if (isVMerge(cell) && !isVMergeRestart(cell)) {
                    // 是跨行单元格的非首行位置，获取到这个合并单元格的对象实例
                    TableRowInfo prevRowInfo = rowInfo.getPreviousRow();
                    cellInfo = prevRowInfo.getCellAtColumn(startColNum);
                    cellInfo.setEndRowNum(rowNum);// 更新该单元格的尾行值
                }
                else {
                    // 跨行首格以及其他所有情况。
                    cellInfo = new TableCellInfo();
                    cellInfo.setStartRowNum(rowNum);
                    cellInfo.setEndRowNum(rowNum);
                    cellInfo.setStartColNum(startColNum);
                    cellInfo.setEndColNum(colNum);
                    cellInfo.addSubDocumentElement(new PlainTextInfo(cell.getText().trim()));
                }

                // 添加到父节点：row
                if (cellInfo != null)
                    rowInfo.addSubDocumentElement(cellInfo);
            }

            // table的colsize未初始化时，要根据计算出的colnum，也就是最后一行的编号，来初始化。
            if (tableInfo.getColumnSize() == -1)
                tableInfo.setColumnSize(colNum);
        }

        //第二级数据
        tableInfo.setTableParts(WTTablePartParser.parseTableToParts(tableInfo));
        return tableInfo;
    }

    /*
     * Judge whether a cell is merged vertically or not
     */
    public static boolean isVMerge(XWPFTableCell cell) {
        if (cell == null)
            return false;
        CTTcPr tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null)
            return false;

        return tcPr.isSetVMerge();
    }

    /*
     * judge whether a cell is restart or not
     */
    public static boolean isVMergeRestart(XWPFTableCell cell) {
        if (cell == null)
            return false;
        CTTcPr tcPr = cell.getCTTc().getTcPr();
        if (tcPr == null || !tcPr.isSetVMerge())
            return false;

        // 如果不判断isVMerge,会getVMerge==null? NullException Alert!
        return (tcPr.getVMerge().getVal() == STMerge.RESTART);
    }

    /*
     * @Description GridSpan记录跨列的列数，也就是跨越单元格的数量。
     */
    public static int getGridSpan(XWPFTableCell cell) {
        if (cell == null)
            return -1;
        CTTcPr tcPr = cell.getCTTc().getTcPr();

        if (tcPr == null)
            return 1;

        CTDecimalNumber number = tcPr.getGridSpan();

        if (number == null) {
            return 1;
        }
        else {
            return number.getVal().intValue();
        }
    }

}