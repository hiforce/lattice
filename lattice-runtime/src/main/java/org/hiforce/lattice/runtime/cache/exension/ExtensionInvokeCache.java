package org.hiforce.lattice.runtime.cache.exension;

import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.NotExistedExtensionPointRealization;
import org.hiforce.lattice.runtime.cache.key.ExtensionInvokeCacheKey;

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

    public IBusinessExt doCacheExtensionRealization(ExtensionInvokeCacheKey cacheKey, IBusinessExt realization) {
        EXT_REALIZATION_CACHE.putIfAbsent(cacheKey.getUniqueId(), null == realization ?
                new NotExistedExtensionPointRealization() : realization);
        return realization;
    }

    public IBusinessExt getCachedExtensionRealization(ExtensionInvokeCacheKey cacheKey) {
        if (null == cacheKey) {
            return null;
        }
        return EXT_REALIZATION_CACHE.get(cacheKey.getUniqueId());
    }

    @Override
    public void clear() {
        EXT_REALIZATION_CACHE.clear();
    }
}
