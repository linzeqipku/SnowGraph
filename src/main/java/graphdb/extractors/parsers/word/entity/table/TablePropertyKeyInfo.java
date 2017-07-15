package graphdb.extractors.parsers.word.entity.table;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class TablePropertyKeyInfo implements Serializable {
    private static final Logger	logger	= Logger.getLogger(TablePropertyKeyInfo.class);
    private List<TableCellInfo> keyCellInfos;

    public TablePropertyKeyInfo() {
        super();
        keyCellInfos = new ArrayList<TableCellInfo>();
    }

    public TablePropertyKeyInfo(TableCellInfo uniqueKeyCell) {
        this();
        keyCellInfos.add(uniqueKeyCell);
    }

    public String toKeyString() {
        try {
            StringBuilder key = new StringBuilder("");
            for (int i = 0; i < keyCellInfos.size(); i++) {
                TableCellInfo cell = keyCellInfos.get(i);
                key.append((i == 0 ? "" : " - ") + ((cell.getText() == null) ? "" : cell.getText()));
            }
            return key.toString();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();

            return null;
        }
    }
    public List<TableCellInfo> getKeyCellInfos() {
        return keyCellInfos;
    }

    public void setKeyCellInfos(List<TableCellInfo> keyCellInfos) {
        this.keyCellInfos = keyCellInfos;
    }

    public String toString() {
        return toKeyString();
    }
}