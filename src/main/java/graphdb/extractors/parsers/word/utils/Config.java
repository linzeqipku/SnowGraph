package graphdb.extractors.parsers.word.utils;

/**
 * Created by maxkibble on 2017/5/25.
 */

public class Config {

    public static String getFileSeparator() {
        return "/";
    }

    public static String getProjectType() { return "Chinese"; }

    public static String getSampleDocumentPath() {
        return "E:\\data\\企业中文知识图谱\\csp-copy-all\\样例\\文档\\csp-msg\\设计\\市民综合服务平台V1.0-短信服务管理系统-Dubbo API定义.docx";
    }

    public static String getProjectDocumentPath() {
        return "E:\\data\\企业中文知识图谱\\csp-copy-all\\样例\\文档\\csp-mgr";
    }

    public static String getProjectGraphPath() {
        return "E:\\data\\graphdb-mgr";
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

    public static String getProjectChineseTokenPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/corpus/corpus_ch.txt";
    }

    public static String getProjectApiTokenPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/corpus/corpus_en.txt";
    }

    public static String getProjectTranslationPath() {
        return "src/main/java/graphdb/extractors/parsers/word/data/corpus/corpus_trans.txt";
    }
}
