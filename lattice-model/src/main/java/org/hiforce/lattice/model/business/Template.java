package org.hiforce.lattice.model.business;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.cache.ITemplateCache;
import org.hiforce.lattice.cache.LatticeCacheFactory;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public abstract class Template implements ITemplate {

    @Getter
    @Setter
    private Long uniqueId;

    @Getter
    private String code;

    @Setter
    private Long internalId;

    public Long getInternalId() {
        if (null == internalId) {
            ITemplateCache templateCache = LatticeCacheFactory.getInstance()
                    .getRuntimeCache().getTemplateIndex();
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
