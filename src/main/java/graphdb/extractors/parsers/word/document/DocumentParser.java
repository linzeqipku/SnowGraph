package graphdb.extractors.parsers.word.document;

import graphdb.extractors.parsers.word.document.word.WordDocxParser;
import graphdb.extractors.parsers.word.entity.utils.*;
import graphdb.extractors.parsers.word.entity.word.WordDocumentInfo;
import graphdb.extractors.parsers.word.utils.ApiJudge;

import java.io.*;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class DocumentParser {

    public static DocumentInfo parseFileToDocumentInfo(Object file) {

        DocumentInfo documentInfo = null;
        String projectName = "";
        if (file == null)
            return null;
        if (file instanceof File) {
            File localFile = (File) file;
            documentInfo = parseFileToDocumentInfo(localFile);
            projectName = getProjectName(localFile);
        }
        documentInfo.setProjectName(projectName);
        parseDocumentInfo(documentInfo);
        return documentInfo;
    }

    private static DocumentInfo parseFileToDocumentInfo(File file) {

        if (!file.isFile())
            return null;

        DocumentInfo documentInfo = null;

        FileType docType = FileUtils.getFileTypeByName(file.getName());

        if (docType == FileType.DOC || docType == FileType.DOCX) {
            documentInfo = new WordDocumentInfo();
        }

        String fileUsageType;
        if (IsCode(docType)) {
            fileUsageType = UsageType.CODE;
        }
        else {
            fileUsageType = getUsageTypeByName(file);
        }

        if (documentInfo != null) {
            // 除了前述5个类型，其余文件不处理，均是null
            documentInfo.setName(file.getName());
            documentInfo.setAbsolutePath(file.getPath());
            documentInfo.setType(docType);
            documentInfo.setUsageType(fileUsageType);
        }
        return documentInfo;
    }

    private static void parseDocumentInfo(DocumentInfo documentInfo) {
        if (documentInfo == null) {
            return;
        }
        String filePath = documentInfo.getAbsolutePath();
        if (filePath.contains("~$"))
            return;
        try {
            InputStream in = new FileInputStream(filePath);
            if (in == null)
                return;

            if (documentInfo instanceof WordDocumentInfo) {
                if (documentInfo.getType() == FileType.DOCX) {
                    WordDocxParser.parseWordDocumentInfo((WordDocumentInfo) documentInfo, in);
                }
            }
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void print(DocumentInfo documentInfo) {
        documentInfo.setName("A changed name!");
        if (documentInfo == null)
            return;

        if (documentInfo instanceof WordDocumentInfo) {
            WordDocxParser.print((WordDocumentInfo) documentInfo);
        }
    }

    private static boolean IsCode(FileType f) {
        if (f == FileType.APK || f == FileType.APP || f == FileType.ASP || f == FileType.ASPX
                || f == FileType.BAK || f == FileType.BAT || f == FileType.BIN || f == FileType.C
                || f == FileType.CLASS || f == FileType.CLASSPATH || f == FileType.CSS
                || f == FileType.DAT || f == FileType.DB || f == FileType.DLL || f == FileType.EXE
                || f == FileType.H || f == FileType.HTM || f == FileType.HTML || f == FileType.JAR
                || f == FileType.JAVA || f == FileType.JS || f == FileType.JSON
                || f == FileType.JSP || f == FileType.PHP || f == FileType.PY
                || f == FileType.PYCONF || f == FileType.APP.SQL || f == FileType.WAR
                || f == FileType.XML || f == FileType.XSD) {
            return true;
        }
        return false;
    }

    private static String getUsageTypeByName(File f) {
        String pureName = FileUtils.getNameWithoutExtension(f.getName());
        String fileUsageType = getType(pureName);
        // System.out.println(pureName+"+"+fileUsageType);
        while (f.getParentFile() != null) {
            String tmp = getType(f.getParentFile().getName());
            if (!tmp.equals("default")) {
                fileUsageType = tmp;
            }
            //System.out.println(f.getParentFile().getName()+"+"+fileUsageType);
            f = f.getParentFile();
        }
        return fileUsageType;
    }

    private static String getProjectName(File f) {
        File curFile = f;
        while(curFile.getParentFile() != null) {
            String fileName = curFile.getName();
            if(ApiJudge.isProjectName(fileName) && fileName.length() > 1) return fileName;
            curFile = curFile.getParentFile();
        }
        return "";
    }

    private static String getType(String name) {
        if (name.contains("设计")) {
            return UsageType.DESIGN;
        }
        else if (name.contains("需求")) {
            return UsageType.REQUIREMENT;
        }
        else if (name.contains("测试")) {
            return UsageType.TEST;
        }
        else {
            return UsageType.DEFAULT;
        }
    }


}
