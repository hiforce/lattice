package org.hifforce.lattice.model.register;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.model.ability.IBusinessExt;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class RealizationSpec extends BaseSpec {

    @Getter
    @Setter
    private String[] codes;

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private IBusinessExt businessExt;

    @Getter
    @Setter
    private Class<IBusinessExt> businessExtClass;

    /**
     * The extension points current realization supported.
     */
    @Getter
    private final Set<String> extensionCodes = Sets.newHashSet();

    public boolean isCodeMatched(String specificCode){
        for( String code: codes ){
            if (StringUtils.equals(code, specificCode))
                return true;
            if (!StringUtils.contains(code, "*"))
                return false;
            if(isPatternCodeMatched(code, specificCode)){
                return true;
            }
        }
        return false;
    }

    private static boolean isPatternCodeMatched(String code, String specificCode) {
        //如果一直没有命中，就会做正则匹配，这里也是懒加载
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
