package graphdb.extractors.parsers.word.entity.utils;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class UsageType {

    public static final String   CODE  =  "code";
    public static final String   DESIGN  =  "design";
    public static final String   TEST  =  "test";
    public static final String   REQUIREMENT  =  "requirement";
    public static final String   DEFAULT  =  "default";

    private String	usageType;		// 定义自定义的变量
    private String	detailUsageType;		// 定义自定义的变量

    private UsageType(String usageType,String detailUsageType) {
        this.usageType = usageType;
        this.detailUsageType = detailUsageType;
    }

    public String getUsageType() {
        return usageType;
    }

    public String getDetailUsageType() {
        return detailUsageType;
    }

}