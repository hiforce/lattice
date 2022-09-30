package org.hiforce.lattice.runtime.cache;

import org.hifforce.lattice.model.register.AbilitySpec;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class SimpleCache {

    private final ConcurrentMap<String, AbilitySpec> CACHE_ABILITY_SPEC = new ConcurrentHashMap<>(120);

    private final ExtensionSpecCache CACHE_EXTENSION_POINT_SPEC = new ExtensionSpecCache();

    protected AbilitySpec getAbilitySpecEntry(String key) {
        if (key == null) {
            return null;
        }
        return CACHE_ABILITY_SPEC.get(key);
    }

    protected AbilitySpec doCacheObjectAbilitySpec(String key, AbilitySpec obj) {
        CACHE_ABILITY_SPEC.put(key, obj);
        return obj;
    }

    protected Collection<AbilitySpec> getAllCacheAbilitySpec() {
        return CACHE_ABILITY_SPEC.values();
    }


    public ExtensionSpecCache getExtensionSpecCache() {
        return CACHE_EXTENSION_POINT_SPEC;
    }
}
