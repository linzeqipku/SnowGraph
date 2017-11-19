package graphdb.extractors.parsers.word.entity.utils;

/**
 * Created by maxkibble on 2017/5/25.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public enum FileType {
    APK("apk"), APP("app"), ASP("asp"), ASPX("aspx"), BAK("bak"), BAT("bat"), BIN("bin"), BMP("bmp"), C(
            "c"), CAB("cab"), CLASS("class"), CLASSPATH("classpath"), CS("cs"), CSS("css"), DAT(
            "dat"), DB("db"), DLL("dll"), DOC("doc"), DOCX("docx"), DTD("dtd"), EOT("eot"), EXE(
            "exe"), FTL("ftl"), GIF("gif"), H("h"), HTM("htm"), HTML("html"), ICO("ico"), JAR("jar"), JAVA(
            "java"), JPG("jpg"), JS("js"), JSON("json"), JSP("jsp"), LOG("log"), MF("MF"), MP3(
            "mp3"), MYMETADATA("mymetadata"), MYUMLDATA("myumldata"), PDF("pdf"), PHP("php"), PNG(
            "png"), PPT("ppt"), PPTX("pptx"), PREFS("prefs"), PROJECT("project"), PROPERTIES(
            "properties"), PY("py"), PYCONF("pyconf"), RAR("rar"), SQL("sql"), SVG("svg"), SWF(
            "swf"), TLD("tld"), TPL("tpl"), TTF("ttf"), TXT("txt"), VSD("vsd"), VSDX("vsdx"), WAR(
            "war"), WPS("wps"), WSDL("wsdl"), XLS("xls"), XLSX("xlsx"), XML("xml"), XSD("xsd"), XSL(
            "xsl"), ZIP("zip"), NONE(""), OTHER("_other");

    private String	name;		// 定义自定义的变量
    @Deprecated
    private String	extension;	// 文件扩展名，只在others情况下使用
    // 事实上每个枚举类型只有一个实例，只能维护一个extension域，起不到每个filetype对象有不同extension的作用

    private FileType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    private final static Map<String, FileType>	DOC_MAP		= new HashMap<>();
    private static Map<FileType, Integer>		TYPE_STATS	= new HashMap<>(); // 统计每类类型数量
    static {
        for (FileType v : values()) {
            DOC_MAP.put(v.getName(), v);
            TYPE_STATS.put(v, 0);
        }
    }

    public static FileType getDocumentTypeByName(String name) {
        FileType type = DOC_MAP.get(name);

        if (type == null)
            type = OTHER;

        TYPE_STATS.put(type, TYPE_STATS.get(type) + 1);
        return type;
    }

    public static Map<FileType, Integer> getTypeStats() {
        return TYPE_STATS;
    }

    @Deprecated
    public String getExtension() {
        return extension;
    }

    @Deprecated
    public void setExtension(String extension) {
        this.extension = extension;
    }

    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        names.add("type");
        names.add("docx");
        names.add("");
        names.add("xls");
        names.add("c");
        names.add("docx");
        names.add("ey932hd");
        List<FileType> ft = new ArrayList<>();
        for (String name : names) {
            FileType ft1 = FileType.getDocumentTypeByName(name);
            if (ft1 == OTHER)
                ft1.setExtension(name);
            System.out.println(ft1 + ":" + ft1.getExtension());
            for (Entry<FileType, Integer> entry : FileType.getTypeStats().entrySet()) {
                if (entry.getValue() != 0)
                    System.out.println("[" + entry.getKey() + "][" + entry.getKey().getExtension()
                            + "][" + entry.getValue() + "]");
            }
            ft.add(ft1);
        }
        System.out.println();
        for (FileType fileType : ft) {
            System.out.println(fileType + ":" + fileType.getExtension());
        }
    }
}