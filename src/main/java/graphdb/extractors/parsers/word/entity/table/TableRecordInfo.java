package graphdb.extractors.parsers.word.entity.table;

import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TableRecordInfo extends TablePartInfo {
    @Deprecated
    private List<TableKeyValuePairInfo> properties;

    public void addProperty(TableKeyValuePairInfo pair) {
        subElements.add(pair);
    }

    public String toString() {
        return subElements.toString();
    }

}
