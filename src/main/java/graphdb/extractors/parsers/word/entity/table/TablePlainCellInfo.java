package graphdb.extractors.parsers.word.entity.table;

import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TablePlainCellInfo extends TablePartInfo {
    @Deprecated
    private TableCellInfo	cell;

    public TablePlainCellInfo() {
        super();
    }

    public TablePlainCellInfo(TableCellInfo tableCellInfo) {
        subElements.add(tableCellInfo);
    }

    private TableCellInfo getCell() {
        if (subElements != null && subElements.size() == 1) {
            DocumentElementInfo ele = subElements.get(0);
            if (ele != null && ele instanceof TableCellInfo)
                return (TableCellInfo) ele;
        }
        return null;
    }

    public void setCell(TableCellInfo cell) {
        subElements.add(cell);
    }

    public String toString() {
        try {
            return getCell().getText();
        }
        catch (NullPointerException e) {
            return null;
        }
    }
}

