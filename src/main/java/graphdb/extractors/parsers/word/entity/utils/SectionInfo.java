package graphdb.extractors.parsers.word.entity.utils;

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
}
