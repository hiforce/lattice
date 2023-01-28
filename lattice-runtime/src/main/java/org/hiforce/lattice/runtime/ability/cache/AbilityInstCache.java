package org.hiforce.lattice.runtime.ability.cache;

import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.runtime.cache.LatticeCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rocky Yu
 * @since 2023/1/28
 */
public class AbilityInstCache implements LatticeCache {

    private static AbilityInstCache instance;

    private static final Object lock = new Object();

    private static final Map<String, List<Class<IAbility>>> ABILITY_INST_MAP = new ConcurrentHashMap<>();

    public static AbilityInstCache getInstance() {
        if (null == instance) {
            synchronized (lock) {
                if (null == instance) {
                    instance = new AbilityInstCache();
                }
            }
        }
        return instance;
    }

    public List<Class<IAbility>> getAbilityInstCodes(String abilityCode){
        return ABILITY_INST_MAP.get(abilityCode);
    }

    public void cacheAbilityInstanceRelation(String abilityCode, List<Class<IAbility>> instanceClasses){
        ABILITY_INST_MAP.put(abilityCode, instanceClasses);
    }

    @Override
    public void init() {

    }

    @Override
    public void clear() {
        ABILITY_INST_MAP.clear();
    }
}
