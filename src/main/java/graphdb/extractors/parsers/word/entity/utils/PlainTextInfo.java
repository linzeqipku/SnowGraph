package graphdb.extractors.parsers.word.entity.utils;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class PlainTextInfo extends DocumentElementInfo {

    private String	text;

    public PlainTextInfo() {
        super();
    }

    public PlainTextInfo(String _text) {
        super();
        text = _text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " - { text: " + this.text + " }";
    }

}
