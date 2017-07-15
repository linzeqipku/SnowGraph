package graphdb.extractors.parsers.word.entity.table;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TableKeyValuePairInfo extends TablePartInfo {
    private TablePropertyKeyInfo	key;
    private TableCellInfo	value;

    public TableKeyValuePairInfo() {
        super();
    }
    public TableKeyValuePairInfo(TablePropertyKeyInfo _key, TableCellInfo _value) {
        key = _key;
        value = _value;
    }

    public TablePropertyKeyInfo getKey() {
        return key;
    }
    public void setKey(TablePropertyKeyInfo key) {
        this.key = key;
    }

    public TableCellInfo getValue() {
        return value;
    }
    public void setValue(TableCellInfo value) {
        this.value = value;
    }

    public String toString() {
        return "{" + key.toString() + ":" + value.toString() + "}";
    }
}

