package org.hiforce.lattice.runtime.cache;


import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.model.register.BaseSpec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hifforce.lattice.utils.BizCodeUtils.isPatternCodeMatched;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2018/7/30
 */
public class TemplateCache extends MultiKeyCache<String, Long, BaseSpec> implements ITemplateCache {

    public TemplateCache() {
        super(120);
    }

    private static final Map<Long, Boolean> REALIZATION_TEMPLATE_SUPPORT_MAP = new ConcurrentHashMap<>();

    public boolean templateCodeMatched(String code, String specificCode) {
        if (StringUtils.equals(code, specificCode))
            return true;
        if (!StringUtils.contains(code, "*"))
            return false;
        return isPatternCodeMatched(code, specificCode);
    }
}
