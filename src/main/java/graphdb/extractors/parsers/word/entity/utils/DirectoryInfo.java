package graphdb.extractors.parsers.word.entity.utils;

import graphdb.extractors.parsers.word.utils.Config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class DirectoryInfo implements Serializable {

    private String				path;
    private String				name;
    private DirectoryInfo		parentDirectory;
    private List<DirectoryInfo>	subDirectories;
    private List<DocumentInfo>	subFiles;
    private String				uuid;

    private boolean				isParsed	= false;
    private DirectoryInfo() {
        super();
        subDirectories = new ArrayList<>();
        subFiles = new ArrayList<>();
        uuid = UUID.randomUUID().toString();
    }

    public DirectoryInfo(String _name) {
        this();
        name = _name;
    }

    public String getRelativePath() {
        DirectoryInfo parentDir = parentDirectory;
        if (parentDir == null)
            return name;
        else
            return parentDir.getRelativePath() + Config.getFileSeparator() + name;
    }

    private String getAbsolutePath() {
        DirectoryInfo parentDir = parentDirectory;
        if (parentDir == null)
            return path;
        else
            return parentDir.getAbsolutePath() + Config.getFileSeparator() + name;
    }

    public void addSubDirectory(DirectoryInfo dir) {
        if (dir != null)
            subDirectories.add(dir);
    }

    public void addDocument(DocumentInfo documentInfo) {
        if (documentInfo != null)
            subFiles.add(documentInfo);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String rootPath) {
        this.path = rootPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DocumentInfo> getSubFiles() {
        return subFiles;
    }

    public void setSubFiles(List<DocumentInfo> documentInfos) {
        this.subFiles = documentInfos;
    }

    public DirectoryInfo getParentDirectory() {
        return parentDirectory;
    }

    public List<DirectoryInfo> getSubDirectories() {
        return subDirectories;
    }

    public void setParentDirectory(DirectoryInfo parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " - { relative_path: " + this.getRelativePath()
                + " }";
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isParsed() {
        return isParsed;
    }

    public void setParsed(boolean isParsed) {
        this.isParsed = isParsed;
    }

    public static void main(String[] args) {
        File file = new File("D:\\workspaces\\zhuzx\\KnowledgeBaseDC\\testdocs\\testdir");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getPath());


    }
}
