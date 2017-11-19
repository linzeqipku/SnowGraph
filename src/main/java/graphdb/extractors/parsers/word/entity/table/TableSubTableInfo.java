package graphdb.extractors.parsers.word.entity.table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TableSubTableInfo extends TablePartInfo {
    private List<TableRowInfo>	headingRows;

    private TableCellInfo		horizontalTitle;
    private TableCellInfo		verticalTitle;
    private TableHeadingInfo			heading;

    public TableSubTableInfo() {
        headingRows = new ArrayList<>();
    }

    public TableCellInfo getHorizontalTitle() {
        return horizontalTitle;
    }

    public void setHorizontalTitle(TableCellInfo horizontalTitle) {
        this.horizontalTitle = horizontalTitle;
    }

    public TableCellInfo getVerticalTitle() {
        return verticalTitle;
    }

    public void setVerticalTitle(TableCellInfo verticalTitle) {
        this.verticalTitle = verticalTitle;
    }

    public TableHeadingInfo getHeading() {
        return heading;
    }

    public void setHeading(TableHeadingInfo heading) {
        this.heading = heading;
    }

    public List<TableRowInfo> getHeadingRows() {
        return headingRows;
    }

    public String toString() {
        return "[HTitle:" + horizontalTitle + "][VTitle:" + verticalTitle + "]"
                + subElements.toString();
    }

}

