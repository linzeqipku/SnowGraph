package graphdb.extractors.parsers.word.entity.utils;

import graphdb.extractors.parsers.word.corpus.Translator;

import java.io.IOException;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class PlainTextInfo extends DocumentElementInfo {

    private String	text;
    private String  englishText;

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

    public String getEnglishText() { return englishText; }

    public void setText(String text) {
        this.text = text;
    }

    public void setEnglishText() { this.englishText = toEnglish(); }

    public String toString() {
        return this.getClass().getSimpleName() + " - { text: " + this.text + " }";
    }

    public String toEnglish() {
        //System.out.println("=== Translation ===");
        if(text == null) return "";
        String ret = "";
        try {
           // System.out.println(" === ch:" + text + "===");
            ret = Translator.ch2en(text);
            //System.out.println(" === en:" + ret + "===");
        }
        catch (IOException e) {
            System.out.println(text + " IOException in plain text translation");
            //e.getMessage();
            System.out.println(e.getMessage());
        }

        return ret;
    }

    public String toHtml(boolean en) {
        if(en) return "<p>" + englishText + "</p>\n";
        else return "<p>" + text + "</p>";
    }
}
