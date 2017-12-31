package graphdb.extractors.parsers.word.document.word;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import graphdb.extractors.parsers.word.document.DocumentParser;
import graphdb.extractors.parsers.word.entity.table.TableCellInfo;
import graphdb.extractors.parsers.word.entity.table.TableInfo;
import graphdb.extractors.parsers.word.entity.utils.*;
import graphdb.extractors.parsers.word.entity.word.WordDocumentInfo;
import graphdb.extractors.parsers.word.utils.ApiJudge;
import graphdb.extractors.parsers.word.utils.Config;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;


/**
 * Created by maxkibble on 2017/5/25.
 */
public class WordDocxParser {

    private static final String DEFAULT_STYLE_TYPE = "DEFAULT";

    private static void setUsageType(DocumentElementInfo doc, String usageType) {
        if(doc == null || usageType == null) return;
        DocumentElementInfo tmp = doc;
        while(true) {
            tmp.setUsageType(usageType);
            tmp = tmp.getParentElement();
            if(tmp == null) break;
        }
    }

    private static void setApiList(DocumentElementInfo doc, String text) {
        if(doc == null || text == null) return;
        String[] words = text.split(" |\\(|\\)");
        for(String word : words) {
            if(!ApiJudge.isApi(word)) continue;
            boolean isPackageName = ApiJudge.isPackageName(word);
            DocumentElementInfo tmp = doc;
            //int sectionDepth = 0;
            while(true) {
                tmp.addToApiList(word);
                if(isPackageName) tmp.setPackageName(word);
                tmp = tmp.getParentElement();
                if(tmp == null) break;
                //if(tmp instanceof SectionInfo) sectionDepth++;
                //if(sectionDepth > 1) break;
            }
        }
    }

    public static void parseWordDocumentInfo(WordDocumentInfo doc, InputStream in) {
        if (doc == null || in == null) {
            return;
        }

        XWPFDocument xwpfDoc;
        try {
            xwpfDoc = new XWPFDocument(in);
        }
        catch (IOException e) {
            System.out.println(doc.getRelativePath());
            return;
        } catch(Exception e){
            System.out.println(doc.getRelativePath());
            return;
        }

        XWPFStyles xwpfStyleMap = xwpfDoc.getStyles();

        SectionContainer sectionContainer = new SectionContainer();

        String projectName = doc.getProjectName();
        // doc root section
        SectionInfo rootSection = new SectionInfo();
        rootSection.setTitle(doc.getName());
        //rootSection.setEnglishTitle(doc.getName());
        setApiList(rootSection, doc.getName());
        rootSection.setLayer(0);
        rootSection.setProjectName(projectName);

        sectionContainer.addSection(rootSection);

        Iterator<IBodyElement> elementIter = xwpfDoc.getBodyElementsIterator();

        SectionInfo lastSection = rootSection;

        boolean hasDisregardedTableOfContent = false;
        boolean hasHandledFirstHeader = false;// disregard all elements before
        // first header.
        String tableCapture = ""; // present each table's capture

        while (elementIter.hasNext()) {
            IBodyElement element = elementIter.next();

            if (element instanceof XWPFParagraph) {
                XWPFParagraph xwpfParagraph = (XWPFParagraph) element;
                String paragraphAsText = xwpfParagraph.getText();
                // paragraph type
                String paragraphStyle = xwpfParagraph.getStyle();
                String styleType = DEFAULT_STYLE_TYPE;

                if (paragraphStyle != null) {
                    XWPFStyle xwpfStyle = xwpfStyleMap.getStyle(paragraphStyle);
                    if(xwpfStyle != null){
                        styleType = xwpfStyle.getName();
                    }
                }

                if(styleType == null){
                    styleType = DEFAULT_STYLE_TYPE;
                }

                if (paragraphAsText == null || paragraphAsText.trim().isEmpty()
                        || xwpfParagraph.getParagraphText() == null) {
                    continue;
                }

                // find the caption of each table
                if (styleType.equals("caption") && xwpfParagraph.getParagraphText().contains("表")) {
                    tableCapture = xwpfParagraph.getParagraphText();
                }
                // if text is plain text,initial capture
                else if (!styleType.equals("caption")
                        && !xwpfParagraph.getParagraphText().isEmpty()) {
                    tableCapture = "";
                }

                DocumentElementInfo docElement = null;

                // toc(table of content)
                if (StyleType.isTOC(styleType)) {
                    // regard table of content
                    if (!hasDisregardedTableOfContent) {
                        // remove all elements in the rootSection(Because they
                        // are before TOC and useless for the system)
                        rootSection.getSubElements().clear();
                        sectionContainer.clear();
                        sectionContainer.addSection(rootSection);
                        lastSection = rootSection;
                    }

                    // docElement = buildTOC(paragraphAsText);
                }
                else if (StyleType.isHeading(styleType)) {
                    // disregard all elements before first Header
                    if (hasHandledFirstHeader == false) {
                        rootSection.getSubElements().clear();
                        sectionContainer.clear();
                        sectionContainer.addSection(rootSection);
                        lastSection = rootSection;

                        hasHandledFirstHeader = true;
                    }

                    // the bigger layer value,the lower layer
                    int currentLayer = getLayerFromStyleType(styleType);

                    // create a new section
                    SectionInfo newSection = new SectionInfo();
                    newSection.setLayer(currentLayer);
                    newSection.setTitle(paragraphAsText);
                    //newSection.setEnglishTitle(paragraphAsText);
                    newSection.setProjectName(projectName);

                    SectionInfo parentSection = sectionContainer.getParentSection(currentLayer);
                    parentSection.addSubDocumentElement(newSection);

                    // register new section
                    sectionContainer.addSection(newSection);

                    // update the latest section
                    lastSection = newSection;

                    continue;
                }
                else if (StyleType.isList(styleType)) {
                    docElement = buildPlainText(paragraphAsText);
                }
                else {
                    docElement = buildPlainText(paragraphAsText);
                }

                lastSection.addSubDocumentElement(docElement);
                setUsageType(docElement, doc.getUsageType());
                setApiList(docElement, paragraphAsText);
            }
            else if (element instanceof XWPFTable) {
                XWPFTable xwpfTable = (XWPFTable) element;
                TableInfo wordTable = WordDocxTableParser.parseWordTable(xwpfTable, tableCapture);

                if (wordTable == null) continue;
                lastSection.addSubDocumentElement(wordTable);
                List<DocumentElementInfo> rows = wordTable.getSubElements();
                for(DocumentElementInfo row:rows) {
                    List<DocumentElementInfo> cellsInARow = row.getSubElements();
                    for(DocumentElementInfo cell: cellsInARow){
                        if (!(cell instanceof TableCellInfo)) continue;
                        TableCellInfo cellInfo = (TableCellInfo) cell;
                        PlainTextInfo textCell = (PlainTextInfo) cellInfo.getSubElements().get(0);
                        setUsageType(textCell, doc.getUsageType());
                        setApiList(textCell, textCell.getText());
                    }
                }
            }
            else if (element instanceof XWPFSDT) {
                // regard Table of Content
                if (!hasDisregardedTableOfContent) {
                    // remove all elements in the rootSection(Because they are
                    // before TOC and useless for the system)
                    rootSection.getSubElements().clear();
                    sectionContainer.clear();
                    sectionContainer.addSection(rootSection);
                    lastSection = rootSection;

                    hasDisregardedTableOfContent = true;// set it to true,
                    // because in new docx,
                    // TOC only occurs once,
                    // and the style may
                    // share with others.
                } // end of if

            }

        }

        // add all sub-elements under rootSection to doc directly
        for (DocumentElementInfo element : rootSection.getSubElements()) {
            doc.addSubDocumentElement(element);
        }

        return;
    }

    private static class SectionContainer {
        private Stack<SectionInfo>	sectionStack	= new Stack<>();

        /*
         * return the last section which satisfies the condition that
         * section.layer < layer
         */
        SectionInfo getParentSection(int layer) {
            SectionInfo parentSection = null;

            // find the first section in the stack which is the first lower layer
            while (!sectionStack.isEmpty()) {
                parentSection = sectionStack.peek();
                int parentLayer = parentSection.getLayer();

                if (parentLayer < layer) {
                    return parentSection;
                }
                else {
                    sectionStack.pop();
                }
            }

            return parentSection;
        }

        void addSection(SectionInfo sectionInfo) {
            sectionStack.push(sectionInfo);
        }

        void clear() {
            sectionStack.clear();
        }
    }

    private static DocumentElementInfo buildPlainText(String txt) {
        if (txt == null) {
            return null;
        }

        PlainTextInfo plainTextInfo = new PlainTextInfo();
        plainTextInfo.setText(txt);
        //plainTextInfo.setEnglishText();

        return plainTextInfo;
    }

    /*
     * eg: styleType = "heading 1" return 1.
     */
    private static int getLayerFromStyleType(String styleType) {
        int layer = 1;

        if (styleType != null) {
            styleType = styleType.trim();

            String tmpArr[] = styleType.split(" ");

            try {
                layer = Integer.parseInt(tmpArr[1]);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return layer;
    }

    public static void print(WordDocumentInfo documentInfo) {
        for (DocumentElementInfo element : documentInfo.getSubElements()) {
            print(element);
        }
    }

    private static void print(DocumentElementInfo element) {
        if (element == null)
            return;

        if (element instanceof SectionInfo) {
            //System.out.println("Usage Type of This Section: " + element.getUsageType());
            SectionInfo sectionInfo = (SectionInfo) element;

            System.out.println(sectionInfo.getTitle());
            //System.out.println(sectionInfo.getEnglishTitle());

            List<DocumentElementInfo> subElements = sectionInfo.getSubElements();
            for (DocumentElementInfo subElement : subElements) {
                print(subElement);
            }

            /*
            String packageName = element.getPackageName();
            if(packageName != null)
                System.out.println("Package name of this section: " + packageName);
            else System.out.println("No package appeared in this section");

            HashSet<String> apiList = element.getApiList();
            System.out.println("Api in this section: ");
            for(String s : apiList)
                System.out.print(s + " ");
            System.out.println();
            */
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
                        System.out.print(textCell.getText() + "\t");
                        //System.out.println(textCell.getEnglishText() + "\t");
                    }
                }
                System.out.println();
            }
        }
        else if (element instanceof PlainTextInfo) {
            PlainTextInfo textElement = (PlainTextInfo) element;
            System.out.println(textElement.getText());
            //System.out.println(textElement.getEnglishText());
        }
    }

    public static void main(String args[]) {
        File file = new File(Config.getSampleDocumentPath());
        if(file.isFile()){
            testFile(file);
        } else if(file.isDirectory()){
            testDirectory(file);
        }
    }

    private static void testDirectory(File folder){
        if(folder == null)
            return;

        File files[] = folder.listFiles();
        for(File file:files){
            if(file.isDirectory()){
                testDirectory(file);
            }else{
                testFile(file);
            }
        }
    }

    private static void testFile(File file){
        if(file == null || !file.getName().toLowerCase().endsWith(".docx"))
            return;

        System.out.println(file.getAbsolutePath());
        DocumentInfo doc = DocumentParser.parseFileToDocumentInfo(file);
        //System.out.println("Project: " + doc.getProjectName());

        if(doc == null)
            return;

        if(doc instanceof WordDocumentInfo)
            print((WordDocumentInfo) doc);
    }

}
