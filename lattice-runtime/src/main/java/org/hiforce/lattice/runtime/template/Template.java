package org.hiforce.lattice.runtime.template;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.cache.LatticeCacheFactory;
import org.hifforce.lattice.model.template.ITemplate;
import org.hifforce.lattice.model.template.TemplateType;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class Template implements ITemplate {

    @Getter
    @Setter
    private Long uniqueId;

    @Getter
    private String code;

    @Setter
    private Long internalId;

    @Getter
    @Setter
    private TemplateType type;

    public Long getInternalId() {
        if (null == internalId) {
            ITemplateCache templateCache = LatticeCacheFactory.getInstance().getRuntimeCache().getTemplateCache();
            internalId = templateCache.getSecondKeyViaFirstKey(getCode());
        }
        return internalId;
    }

    private Boolean isPatternCode;

    public boolean isPatternTemplateCode() {
        if (null != isPatternCode) return isPatternCode;

        isPatternCode = StringUtils.contains(getCode(), "*");
        return isPatternCode;
    }

    public void setCode(String code) {
        if (StringUtils.isNotEmpty(code)) {
            this.code = code.intern();
        }
    }
}
