package org.hiforce.lattice.runtime.cache.key;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class BaseObjectCacheKey {

    public static Map<String, Long> BIZ_CODE_IDX_MAP = new ConcurrentHashMap<String, Long>(120);

    public static Map<String, Long> SCENARIO_IDX_MAP = new ConcurrentHashMap<String, Long>(120);

    private static final int templateCodeLen = 5;
    private static final int supportCustomBitLen = 1;
    private static final int scenarioBitLen = 2;
    private static final int bizCodeBitLen = 4;
    private static final int extCodeBitLen = 6;

    private static final int extCodeStart = 0;
    private static final int bizCodeStart = extCodeStart + extCodeBitLen;//6
    private static final int scenarioStart = bizCodeStart + bizCodeBitLen;//10
    private static final int supportCustomStart = scenarioStart - scenarioBitLen;//12
    private static final int templateStart = supportCustomStart - supportCustomBitLen; //13
    private static final int productStart = templateStart + templateCodeLen; //18

    private static final long[] uniqueIdStarts = new long[productStart + 1];

    private static final Long NO_INDEX = (long) -1;

    @Getter
    @Setter
    protected Long uniqueId;

    @Getter
    @Setter
    private Long bizCodeIndex;

    @Getter
    @Setter
    private Long templateIndex;

    @Getter
    @Setter
    private Long extCodeIndex;

    @Getter
    @Setter
    private Long scenarioIndex;

    static {
        for (int i = 0; i < uniqueIdStarts.length; i++) {
            uniqueIdStarts[i] = (long) Math.pow(10, i);
        }
    }

    public abstract String getBizCode();

    public abstract String getTemplateCode();

    public abstract String getExtensionCode();

    public abstract String getScenario();

    public abstract boolean isSupportCustomization();

    public abstract boolean isOnlyProduct();

    public abstract boolean validateIndex();

    public abstract boolean customEquals(Object o);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseObjectCacheKey that = (BaseObjectCacheKey) o;
        if (NO_INDEX.equals(uniqueId)) {
            return customEquals(that);
        }
        return this.uniqueId.longValue() == that.uniqueId.longValue();
    }

    protected void generateUniqueId() {
        if (getUniqueId() != null && getUniqueId().longValue() > 0)
            return;

        if (!validateIndex()) {
            uniqueId = NO_INDEX;
            return;
        }
        long customIndex = isSupportCustomization() ? 1L : 0L;
        long onlyProductIndex = isOnlyProduct() ? 1L : 0L;

        long uniqueId = onlyProductIndex * uniqueIdStarts[productStart]
                + (null == templateIndex ? 0L : templateIndex) * uniqueIdStarts[templateStart]
                + customIndex * uniqueIdStarts[supportCustomStart]
                + (null == scenarioIndex ? 0L : scenarioIndex) * uniqueIdStarts[scenarioStart]
                + (null == bizCodeIndex ? 0L : bizCodeIndex) * uniqueIdStarts[bizCodeStart]
                + (null == extCodeIndex ? 0L : extCodeIndex) * uniqueIdStarts[extCodeStart];

        setUniqueId(uniqueId);
    }

    @SuppressWarnings("all")
    protected void buildObjectCacheUniqueId() {
        if (getUniqueId() != null && getUniqueId().longValue() > 0)
            return;
        LatticeRuntimeCache latticeRuntimeCache = Lattice.getInstance().getLatticeRuntimeCache();
        if (null == bizCodeIndex) {
            bizCodeIndex = null == getBizCode() ? null : BIZ_CODE_IDX_MAP.get(getBizCode());
        }
        if (null == extCodeIndex) {
            extCodeIndex = null == getExtensionCode() ? null : latticeRuntimeCache.getExtensionSpecCache()
                    .getSecondKeyViaFirstKey(getExtensionCode());
        }
        if (null == scenarioIndex) {
            if (StringUtils.isEmpty(getScenario())) {
                scenarioIndex = 0L;
            } else {
                Long scenario = SCENARIO_IDX_MAP.get(getScenario());
                scenarioIndex = scenario == null ? 0L : scenario;
            }
        }
        if (null == templateIndex || templateIndex <= 0) {
            templateIndex = null == getTemplateCode() ? null : latticeRuntimeCache
                    .getTemplateCache().getSecondKeyViaFirstKey(getTemplateCode());
        }
        generateUniqueId();
    }
}
