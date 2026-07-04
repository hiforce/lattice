package org.hiforce.lattice.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class BizCodeUtils {

    public static boolean isCodesMatched(String code, String specificCode) {
        return matchEitherDirection(code, specificCode);
    }

    public static boolean isCodeMatched(String code, String specificCode) {
        return matchEitherDirection(code, specificCode);
    }

    /**
     * Matches two codes where either side may carry the {@code *} wildcard pattern.
     * The pattern side (the one containing {@code *}) is compiled to a regex and
     * matched against the concrete side, so callers may pass the arguments in
     * either order (e.g. a business template code {@code task.*} against a runtime
     * business identity {@code task.101.RECRUITMENT}).
     */
    private static boolean matchEitherDirection(String code, String specificCode) {
        if (StringUtils.equals(code, specificCode)) {
            return true;
        }
        if (StringUtils.contains(code, "*")) {
            return isPatternCodeMatched(code, specificCode);
        }
        if (StringUtils.contains(specificCode, "*")) {
            return isPatternCodeMatched(specificCode, code);
        }
        return false;
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
        pattern = pattern.replace("*", "[a-zA-Z0-9\\._-]*");
        return pattern + "$";
    }

    private static String code2Pattern(String code) {
        String pattern = code.replace(".", "\\.");
        pattern = pattern.replace("*", "[a-zA-Z0-9\\._-]*");
        return "^" + pattern;
    }
}
