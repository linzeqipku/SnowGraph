package graphdb.extractors.parsers.word.entity.utils;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class StyleType {

    /* TOC */
    private static final String	TOC_REGEX		= "toc \\d+";

    /* chapter */
    private static final String	HEADING_REGEX	= "heading \\d+";

    private static final String	LIST			= "a4";
    private static final String	CAPTION			= "a3";

    public static boolean isCaption(String style) {
        return CAPTION.equalsIgnoreCase(style);
    }

    public static boolean isList(String style) {
        return LIST.equalsIgnoreCase(style);
    }

    public static boolean isTOC(String style) {
        if (style == null) {

            return false;
        }

        if (style.matches(TOC_REGEX)) {
            return true;
        }

        return false;
    }

    public static boolean isHeading(String style) {
        if (style == null) {
            return false;
        }

        if (style.matches(HEADING_REGEX)) {
            return true;
        }

        return false;
    }
}
