package graphdb.extractors.parsers.word.entity.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class DocumentElementInfo implements Serializable {
    private DocumentInfo				documentInfo;
    protected DocumentElementInfo		parentElement;	// 可能有的element以DocumentInfo为父节点。
    protected List<DocumentElementInfo>	subElements;
    private String					uuid;
    private String					usageType;
    private String                    packageName;
    private HashSet<String>           apiList;
    private String                    projectName;

    protected DocumentElementInfo() {
        uuid = UUID.randomUUID().toString();
        subElements = new ArrayList<>();
        apiList = new HashSet<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String type) {
        this.usageType = type;
    }


    public void addSubDocumentElement(DocumentElementInfo documentElementInfo) {
        if (documentElementInfo != null) {
            subElements.add(documentElementInfo);
            documentElementInfo.setParentElement(this);
            documentElementInfo.setDocumentInfo(documentInfo);
        }
    }

    public void removeSubDocumentElement(DocumentElementInfo documentElementInfo) {
        if (documentElementInfo != null) {
            boolean success = subElements.remove(documentElementInfo);
            if (success) {
                documentElementInfo.setParentElement(null);
                documentElementInfo.setDocumentInfo(null);
            }
        }
    }

    public List<DocumentElementInfo> getSubElements() {
        return subElements;
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
    }

    public DocumentElementInfo getParentElement() {
        return parentElement;
    }

    private void setParentElement(DocumentElementInfo parentElement) {
        this.parentElement = parentElement;
    }

    public String getPackageName() { return packageName; }

    public void setPackageName(String packageName) { this.packageName = packageName; }

    public HashSet<String> getApiList() { return apiList; }

    public void setApiList(HashSet<String> apiList) { this.apiList = apiList; }

    public String getProjectName() { return projectName; }

    public void setProjectName(String projectName) { this.projectName = projectName; }

    public void addToApiList(String api) { apiList.add(api); }

    public String toString() {
        return super.toString() + " - " + this.getClass().getSimpleName();
    }

    public String toHtml(boolean en) { return ""; }

    public String toEnglish() { return ""; }
}
