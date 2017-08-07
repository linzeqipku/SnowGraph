package graphdb.extractors.parsers.word.entity.utils;

import graphdb.extractors.parsers.word.WordKnowledgeExtractor;
import graphdb.extractors.parsers.word.entity.table.TableInfo;
import graphdb.extractors.parsers.word.entity.word.WordDocumentInfo;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.HashSet;

/**
 * Created by maxkibble on 2017/5/26.
 */
public class GraphNodeUtil {
    public static void createDocumentNode(WordDocumentInfo doc, Node node) {
        node.addLabel(Label.label(WordKnowledgeExtractor.DOCX_FILE));
        if(doc.getName() != null) node.setProperty(WordKnowledgeExtractor.DOCX_NAME, doc.getName());
        else node.setProperty(WordKnowledgeExtractor.DOCX_NAME, "");
        if(doc.getAbsolutePath() != null) node.setProperty(WordKnowledgeExtractor.ABSOLUTE_PATH, doc.getAbsolutePath());
        else node.setProperty(WordKnowledgeExtractor.ABSOLUTE_PATH, "");
        if(doc.getUsageType() != null) node.setProperty(WordKnowledgeExtractor.DOC_USEGE_TYPE, doc.getUsageType());
        else node.setProperty(WordKnowledgeExtractor.DOC_USEGE_TYPE, "");
        if(doc.getProjectName() != null) node.setProperty(WordKnowledgeExtractor.DOCX_PROJECT_NAME, doc.getProjectName());
        else node.setProperty(WordKnowledgeExtractor.DOCX_PROJECT_NAME, "");
    }

    public static void createPlainTextNode(PlainTextInfo plainText, Node node) {
        node.addLabel(Label.label(WordKnowledgeExtractor.DOCX_PLAIN_TEXT));
        if(plainText.getText() != null) node.setProperty(WordKnowledgeExtractor.PLAIN_TEXT_CONTENT, plainText.toHtml());
        else node.setProperty(WordKnowledgeExtractor.PLAIN_TEXT_CONTENT, "");
    }

    public static void createSectionNode(SectionInfo section, Node node) {
        node.addLabel(Label.label(WordKnowledgeExtractor.DOCX_SECTION));
        if(section.getTitle() != null) node.setProperty(WordKnowledgeExtractor.SECTION_TITLE, section.getTitle());
        else node.setProperty(WordKnowledgeExtractor.SECTION_TITLE, "");
        node.setProperty(WordKnowledgeExtractor.SECTION_LAYER, section.getLayer());
        if(section.getSectionNumber() != null) node.setProperty(WordKnowledgeExtractor.SECTION_NUMBER, section.getSectionNumber());
        else node.setProperty(WordKnowledgeExtractor.SECTION_NUMBER, "");
        if(section.getUsageType() != null) node.setProperty(WordKnowledgeExtractor.SECTION_USAGE_TYPE, section.getUsageType());
        else node.setProperty(WordKnowledgeExtractor.SECTION_USAGE_TYPE, "");
        if(section.getPackageName() != null) node.setProperty(WordKnowledgeExtractor.SECTION_PACKAGE, section.getPackageName());
        else node.setProperty(WordKnowledgeExtractor.SECTION_PACKAGE, "");
        HashSet<String> sectionApis = section.getApiList();
        String nodeApiList = "";
        for(String api : sectionApis) {
            nodeApiList = nodeApiList + "\n" + api;
        }
        node.setProperty(WordKnowledgeExtractor.SECTION_APIS, nodeApiList);
        if(section.getProjectName() != null) node.setProperty(WordKnowledgeExtractor.SECTION_PROJECT_NAME, section.getProjectName());
        else node.setProperty(WordKnowledgeExtractor.SECTION_PROJECT_NAME, "");
    }

    public static void createTableNode(TableInfo table, Node node) {
        node.addLabel(Label.label(WordKnowledgeExtractor.DOCX_TABLE));
        if(table.getTableCaption() != null) node.setProperty(WordKnowledgeExtractor.TABLE_CAPTION, table.getTableCaption());
        else node.setProperty(WordKnowledgeExtractor.TABLE_CAPTION, "");
        if(table.getTableNumber() != null) node.setProperty(WordKnowledgeExtractor.TABLE_NUMBER, table.getTableNumber());
        else node.setProperty(WordKnowledgeExtractor.TABLE_NUMBER, "");
        node.setProperty(WordKnowledgeExtractor.TABLE_COLUMN_NUM, table.getColumnSize());
        node.setProperty(WordKnowledgeExtractor.TABLE_ROW_NUM, table.getRowSize());
    }
}
