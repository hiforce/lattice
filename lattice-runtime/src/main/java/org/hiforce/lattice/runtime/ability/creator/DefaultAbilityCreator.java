package org.hiforce.lattice.runtime.ability.creator;

import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.model.register.AbilityInstSpec;
import org.hiforce.lattice.model.register.AbilitySpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.cache.AbilityInstCache;
import org.hiforce.lattice.runtime.ability.cache.AbilityInstCacheKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.hiforce.lattice.runtime.utils.LatticeBeanUtils.getAndCreateSpringBeanViaClass;

/**
 * @author Rocky Yu
 * @since 2023/1/26
 */
@SuppressWarnings("all")
public class DefaultAbilityCreator<Ability extends IAbility> {


    private String abilityCode;
    private IBizObject target;

    private final Map<AbilityInstCacheKey, List<Ability>> abilitySelectorCache
            = new ConcurrentHashMap<>(1024);

    public DefaultAbilityCreator(String abilityCode, IBizObject target) {
        this.target = target;
        this.abilityCode = abilityCode;
    }

    public List<Ability> getAllAbilityInstancesWithCache() {
        AbilityInstCacheKey selectorCacheKey = new AbilityInstCacheKey(target.getBizCode(), abilityCode);
        List<Ability> abilityList = abilitySelectorCache
                .computeIfAbsent(selectorCacheKey, (key) -> {
                    LinkedHashSet<Ability> abilities = new LinkedHashSet<>();
                    List<Class<IAbility>> list = availableAbilities();
                    for (Class<IAbility> instanceClass : list) {
                        Ability ability = getAndCreateSpringBeanViaClass(instanceClass, target);
                        if (ability != null) {
                            abilities.add(ability);
                        }
                    }
                    return new ArrayList<>(abilities);
                });

        return abilityList;
    }

    private List<Class<IAbility>> availableAbilities() {
        List<Class<IAbility>> returnValue = AbilityInstCache.getInstance().getAbilityInstCodes(abilityCode);
        if (returnValue != null) {
            return returnValue;
        }

        List<Class<IAbility>> abilityClasses = new ArrayList<>(8);
        AbilitySpec abilitySpec = Lattice.getInstance().getAbilitySpecByCode(abilityCode);
        if (null == abilitySpec) {
            AbilityInstCache.getInstance().cacheAbilityInstanceRelation(abilityCode, abilityClasses);
            return abilityClasses;
        }

        Set<AbilityInstSpec> abilityInstanceSpecList = abilitySpec.getAbilityInstances();
        List<AbilityInstSpec> result = new ArrayList<>();
        for (AbilityInstSpec abilityInstanceSpec : abilityInstanceSpecList) {
            if (StringUtils.isNotEmpty(abilityInstanceSpec.getInstanceClass())) {
                result.add(abilityInstanceSpec);
            }
        }
        result.sort(Comparator.comparingInt(AbilityInstSpec::getPriority));
        for (AbilityInstSpec spec : result) {
            try {
                abilityClasses.add((Class<IAbility>) Class.forName(spec.getInstanceClass()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        AbilityInstCache.getInstance().cacheAbilityInstanceRelation(abilityCode, abilityClasses);
        return abilityClasses;
    }
}
