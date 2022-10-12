package org.hiforce.lattice.runtime.cache.exension;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.key.ExtensionInvokeCacheKey;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@Slf4j
public class ExtensionInvokeCache implements LatticeCache {

    private static ExtensionInvokeCache INSTANCE;

    private static final Object LOCK = new Object();

    private final Map<Long, IBusinessExt> EXT_REALIZATION_CACHE = new ConcurrentHashMap<>(2000);

    private final Map<String, Long> SCENARIO_IDX_MAP = new ConcurrentHashMap<>(120);

    private ExtensionInvokeCache() {

    }

    public static ExtensionInvokeCache getInstance() {
        if (null == INSTANCE) {
            synchronized (LOCK) {
                if (null == INSTANCE) {
                    INSTANCE = new ExtensionInvokeCache();
                }
            }
        }
        return INSTANCE;
    }

    public Long getScenarioIndex(String scenario) {
        return SCENARIO_IDX_MAP.get(scenario);
    }

    public IBusinessExt doCacheExtensionRealization(ExtensionInvokeCacheKey cacheKey, IBusinessExt realization) {
        EXT_REALIZATION_CACHE.putIfAbsent(cacheKey.getUniqueId(), null == realization ?
                new NotExistedRealization() : realization);
        return realization;
    }

    public IBusinessExt getCachedExtensionRealization(ExtensionInvokeCacheKey cacheKey) {
        if (null == cacheKey) {
            return null;
        }
        return EXT_REALIZATION_CACHE.get(cacheKey.getUniqueId());
    }

    @Override
    public void init() {
        long scenarioIdx = 1;
        List<RealizationSpec> realizationSpecs = Lattice.getInstance().getAllRealizations();
        for (RealizationSpec realizationSpec : realizationSpecs) {
            if (StringUtils.isEmpty(realizationSpec.getScenario()))
                continue;
            SCENARIO_IDX_MAP.putIfAbsent(realizationSpec.getScenario(), scenarioIdx++);
        }
    }

    @Override
    public void clear() {
        EXT_REALIZATION_CACHE.clear();
        SCENARIO_IDX_MAP.clear();
    }
}
