package org.hiforce.lattice.runtime.cache;


import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.model.business.ITemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hifforce.lattice.utils.CodeUtils.isPatternCodeMatched;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2018/7/30
 */
public class TemplateCache extends MultiKeyCache<String, Long, ITemplate> implements ITemplateCache {

    public TemplateCache() {
        super(120);
    }

    private static final Map<Long, Boolean> REALIZATION_TEMPLATE_SUPPORT_MAP = new ConcurrentHashMap<>();

    public boolean templateCodeMatched(ITemplate realization, ITemplate specificTemplate) {
        if (null == realization.getInternalId() || null == specificTemplate.getInternalId()) {
            return templateCodeMatched(realization.getCode(), specificTemplate.getCode());
        }
        if (realization.getInternalId().equals(specificTemplate.getInternalId()))
            return true;
        int x = realization.getInternalId().intValue();
        int y = specificTemplate.getInternalId().intValue();
        long uniqueId = x * (long) Math.pow(10, 5) + y;
        Boolean value = REALIZATION_TEMPLATE_SUPPORT_MAP.get(uniqueId);
        if (null != value) return value;

        if (realization.isPatternTemplateCode()) {
            boolean matched = isPatternCodeMatched(realization.getCode(), specificTemplate.getCode());
            REALIZATION_TEMPLATE_SUPPORT_MAP.put(uniqueId, matched);
            return matched;
        }
        return false;
    }


    public boolean templateCodeMatched(String code, String specificCode) {
        if (StringUtils.equals(code, specificCode))
            return true;
        if (!StringUtils.contains(code, "*"))
            return false;
        return isPatternCodeMatched(code, specificCode);
    }

    @Override
    public boolean templateCodeMatched(String[] codes, String specificCode) {
        for (String code : codes) {
            if (templateCodeMatched(code, specificCode)) {
                return true;
            }
        }
        return false;
    }
}
