package extractors.parsers.javacode.astparser;

import java.util.HashMap;
import java.util.Map;

import extractors.parsers.javacode.entity.ClassInfo;
import extractors.parsers.javacode.entity.FieldInfo;
import extractors.parsers.javacode.entity.InterfaceInfo;
import extractors.parsers.javacode.entity.MethodInfo;

public class ElementInfoPool {

    public String srcDir;
    public Map<String, ClassInfo> classInfoMap;
    public Map<String, InterfaceInfo> interfaceInfoMap;
    public Map<String, MethodInfo> methodInfoMap;
    public Map<String, FieldInfo> fieldInfoMap;

    public ElementInfoPool(String srcDir) {
        this.srcDir = srcDir;
        classInfoMap = new HashMap<String, ClassInfo>();
        interfaceInfoMap = new HashMap<String, InterfaceInfo>();
        methodInfoMap = new HashMap<String, MethodInfo>();
        fieldInfoMap = new HashMap<String, FieldInfo>();
    }

}
