package graphdb.extractors.parsers.word.entity.utils;

import graphdb.extractors.parsers.word.utils.Config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class DocumentInfo implements Serializable {

    protected String					name;
    protected DirectoryInfo				parentDirectory;
    protected String					absolutePath;
    protected FileType					type;
    protected String					usageType;
    protected String					uuid;
    protected String                    projectName;

    protected List<DocumentElementInfo>	subElements;

    public DocumentInfo() {
        uuid = UUID.randomUUID().toString();
        subElements = new ArrayList<>();
    }

    public void addSubDocumentElement(DocumentElementInfo element) {
        if (element != null) {
            subElements.add(element);
            element.setDocumentInfo(this);
        }
    }

    public List<DocumentElementInfo> getSubElements() {
        return subElements;
    }

    public String getRelativePath() {
        if (parentDirectory == null)
            return name;
        return parentDirectory.getRelativePath() + Config.getFileSeparator() + name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DirectoryInfo getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(DirectoryInfo directoryInfo) {
        this.parentDirectory = directoryInfo;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String type) {
        this.usageType = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProjectName() { return projectName; }

    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String toString() {
        return this.getClass().getSimpleName() + " - { name: " + this.name + " }";
    }
}