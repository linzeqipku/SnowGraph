package graphdb.extractors.parsers.word.entity.word;

import graphdb.extractors.parsers.word.entity.table.TableCellInfo;
import graphdb.extractors.parsers.word.entity.table.TableInfo;
import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;
import graphdb.extractors.parsers.word.entity.utils.DocumentInfo;
import graphdb.extractors.parsers.word.entity.utils.PlainTextInfo;
import graphdb.extractors.parsers.word.entity.utils.SectionInfo;

import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class WordDocumentInfo extends DocumentInfo {

    public WordDocumentInfo() {
        super();
    }

    public String toString() {
        return this.getClass().getSimpleName() + " - { name: " + this.name + " }";
    }

    public String getDocStr() {
        StringBuilder ret = new StringBuilder("");
        for (DocumentElementInfo element : this.getSubElements()) {
            ret.append(getEleStr(element));
        }
        return ret.toString();
    }

    private String getEleStr(DocumentElementInfo element) {
        StringBuilder ret = new StringBuilder("");

        if (element == null) return ret.toString();

        if (element instanceof SectionInfo) {
            SectionInfo sectionInfo = (SectionInfo) element;

            ret.append(sectionInfo.getTitle());

            List<DocumentElementInfo> subElements = sectionInfo.getSubElements();
            for (DocumentElementInfo subElement : subElements) {
                ret.append(getEleStr(subElement));
            }
        }
        else if (element instanceof TableInfo) {
            TableInfo table = (TableInfo) element;

            List<DocumentElementInfo> rows = table.getSubElements();

            for(DocumentElementInfo row:rows) {
                List<DocumentElementInfo> cellsInARow = row.getSubElements();
                for(DocumentElementInfo cell: cellsInARow){
                    if (cell instanceof TableCellInfo) {
                        TableCellInfo cellInfo = (TableCellInfo) cell;
                        PlainTextInfo textCell = (PlainTextInfo) cellInfo.getSubElements().get(0);
                        ret.append(textCell.getText() + "\t");
                    }
                }
                ret.append("\n");
            }
        }
        else if (element instanceof PlainTextInfo) {
            PlainTextInfo textElement = (PlainTextInfo) element;
            ret.append(textElement.getText());
        }
        return ret.toString();
    }
}