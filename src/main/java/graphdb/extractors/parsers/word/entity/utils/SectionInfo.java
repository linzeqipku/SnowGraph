package graphdb.extractors.parsers.word.entity.utils;

import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class SectionInfo extends DocumentElementInfo {

    private String	title;
    private int		layer;
    private String	sectionNumber;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " - { title: " + this.title + " }";
    }

    public String toHtml() {
        StringBuilder html = new StringBuilder("<section>\n");
        html.append("<h" + getLayer() + ">" + getTitle() + "</h" + getLayer() + ">");
        List<DocumentElementInfo> subElements = getSubElements();
        for(DocumentElementInfo subEle : subElements) {
            html.append(subEle.toHtml());
        }
        html.append("</section>\n");
        return html.toString();
    }
}
