package graphdb.extractors.parsers.word.document.table;

import graphdb.extractors.parsers.word.entity.table.TableCellInfo;
import graphdb.extractors.parsers.word.entity.table.TableRowInfo;
import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;
import graphdb.extractors.parsers.word.wordbag.WordBagInstance;

/**
 * Created by maxkibble on 2017/5/25.
 */
class WTRowSignature {
    public static final String	KEY					= "K";
    public static final String	VALUE				= "V";

    private static final double	keyDominationRate	= 0.5;
    private static final double	valueDominationRate	= 0.5;

    private static int countKey(String signature) {
        int count = 0;
        for (int i = 0; i < signature.length(); i++) {
            if (KEY.equals(signature.substring(i, i + 1)))
                count++;
        }
        return count;
    }

    private static int countValue(String signature) {
        int count = 0;
        for (int i = 0; i < signature.length(); i++) {
            if (VALUE.equals(signature.substring(i, i + 1)))
                count++;
        }
        return count;
    }

    public static String getSignature(TableRowInfo rowInfo) {
        if (rowInfo == null || rowInfo.getSubElements() == null
                || rowInfo.getSubElements().size() == 0)
            return "";

        StringBuilder signature = new StringBuilder();
        for (DocumentElementInfo ele : rowInfo.getSubElements()) {
            if (ele instanceof TableCellInfo) {
                TableCellInfo cellInfo = (TableCellInfo) ele;
                if (WordBagInstance.getCommon(cellInfo.getText()) != null)
                    signature.append(KEY);
                else
                    signature.append(VALUE);
            }
        }
        return signature.toString();
    }

    public static boolean keyDominateRow(TableRowInfo rowInfo) {
        String signature = getSignature(rowInfo);
        int keyCount = countKey(signature);
        int totalWordCount = signature.length();

        if ((double) totalWordCount * keyDominationRate <= keyCount)
            return true;

        return false;
    }

    public static boolean valueDominateRow(TableRowInfo rowInfo) {
        String signature = getSignature(rowInfo);
        int valueCount = countValue(signature);
        int totalWordCount = signature.length();

        if ((double) totalWordCount * valueDominationRate <= valueCount)
            return true;

        return false;
    }

}

