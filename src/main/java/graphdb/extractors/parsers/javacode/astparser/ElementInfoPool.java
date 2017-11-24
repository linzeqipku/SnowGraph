package graphdb.extractors.parsers.javacode.astparser;

import java.util.HashMap;
import java.util.Map;

import graphdb.extractors.parsers.javacode.entity.ClassInfo;
import graphdb.extractors.parsers.javacode.entity.FieldInfo;
import graphdb.extractors.parsers.javacode.entity.InterfaceInfo;
import graphdb.extractors.parsers.javacode.entity.MethodInfo;

public class ElementInfoPool {

    private String srcDir;
    public Map<String, ClassInfo> classInfoMap;
    public Map<String, InterfaceInfo> interfaceInfoMap;
    public Map<String, MethodInfo> methodInfoMap;
    public Map<String, FieldInfo> fieldInfoMap;

    public ElementInfoPool(String srcDir) {
        this.srcDir = srcDir;
        classInfoMap = new HashMap<>();
        interfaceInfoMap = new HashMap<>();
        methodInfoMap = new HashMap<>();
        fieldInfoMap = new HashMap<>();
    }

}
