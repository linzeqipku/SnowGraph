package graphdb.extractors.parsers.word.utils;

/**
 * Created by maxkibble on 2017/5/25.
 */

public class Config {

    public static String getFileSeparator() {
        return "/";
    }

    public static String getWordBagPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/wordbag/handcraft/handcraft.txt";
    }

    public static String getApiStopWordPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/stopwords/stopwords_api.txt";
    }

    public static String getSectionTitleStopWordPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/stopwords/stopwords_sectiontitle.txt";
    }

    public static String getSubSectionStopWordPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/stopwords/stopwords_subsection.txt";
    }

}
