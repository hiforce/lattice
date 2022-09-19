package org.hifforce.lattice.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class CodeUtils {

    public static boolean isCodesMatched(String code, String specificCode) {

        if (StringUtils.equals(code, specificCode))
            return true;
        if (!StringUtils.contains(code, "*"))
            return false;
        return isPatternCodeMatched(code, specificCode);
    }

    public static boolean isPatternCodeMatched(String code, String specificCode) {
        String patternStr;
        if (code.startsWith("*.")) {
            patternStr = code2PrePattern(code);
        } else {
            patternStr = code2Pattern(code);
        }
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(specificCode);
        return matcher.find();
    }

    private static String code2PrePattern(String code) {
        String pattern = code.replace(".", "\\.");
        pattern = pattern.replace("*", "[a-zA-Z\\._]*");
        return pattern + "$";
    }

    private static String code2Pattern(String code) {
        String pattern = code.replace(".", "\\.");
        pattern = pattern.replace("*", "[a-zA-Z\\._]*");
        return "^" + pattern;
    }
}
