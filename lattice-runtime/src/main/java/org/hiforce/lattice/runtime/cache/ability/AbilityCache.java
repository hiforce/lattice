package org.hiforce.lattice.runtime.cache.ability;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.AbilityAnnotation;
import org.hiforce.lattice.model.register.AbilitySpec;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.key.ExtensionRunnerCacheKey;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class AbilityCache implements LatticeCache {

    private static AbilityCache instance;

    private final ConcurrentMap<String, AbilitySpec> ABILITY_SPEC_CACHE = new ConcurrentHashMap<>(120);

    private final Map<Class<?>, Map<Long, Object>> ABILITY_RUNNER_CACHE = Maps.newConcurrentMap();

    private AbilityCache() {

    }

    public static AbilityCache getInstance() {
        if (null == instance) {
            instance = new AbilityCache();
        }
        return instance;
    }

    @Override
    public void init() {

    }

    @Override
    public void clear() {
        ABILITY_SPEC_CACHE.clear();
        ABILITY_RUNNER_CACHE.clear();
    }

    public void doCacheExtensionRunner(Class<?> abilityClass, ExtensionRunnerCacheKey key, Object runner) {
        Map<Long, Object> cache = ABILITY_RUNNER_CACHE.get(abilityClass);
        if (MapUtils.isEmpty(cache)) {
            cache = new ConcurrentHashMap<>(200);
            ABILITY_RUNNER_CACHE.put(abilityClass, cache);
        }
        cache.put(key.getUniqueId(), runner);
    }

    public Object getCachedExtensionRunner(Class<?> abilityClass, ExtensionRunnerCacheKey key) {
        Map<Long, Object> cache = ABILITY_RUNNER_CACHE.get(abilityClass);
        if (null == cache) {
            return null;
        }
        return cache.get(key.getUniqueId());
    }

    public AbilitySpec doCacheAbilitySpec(AbilityAnnotation ability, Class<?> targetClass) {
        String abilityCode = StringUtils.isEmpty(ability.getCode()) ? targetClass.getName() : ability.getCode();
        AbilitySpec abilitySpec = getAbilitySpecEntry(abilityCode);
        if (null == abilitySpec) {
            abilitySpec = AbilitySpec.of(abilityCode, ability.getName(), ability.getDesc());
            abilitySpec.setAbilityClass(targetClass);
            if (StringUtils.isNotEmpty(ability.getParent())) {
                abilitySpec.setParentCode(ability.getParent());
            }
            return doCacheObjectAbilitySpec(abilityCode, abilitySpec);
        }
        return abilitySpec;
    }

    public AbilitySpec getAbilitySpecEntry(String key) {
        if (key == null) {
            return null;
        }
        return ABILITY_SPEC_CACHE.get(key);
    }

    public AbilitySpec doCacheObjectAbilitySpec(String key, AbilitySpec obj) {
        ABILITY_SPEC_CACHE.put(key, obj);
        return obj;
    }

    public Collection<AbilitySpec> getAllCachedAbilities() {
        return ABILITY_SPEC_CACHE.values();
    }
}
