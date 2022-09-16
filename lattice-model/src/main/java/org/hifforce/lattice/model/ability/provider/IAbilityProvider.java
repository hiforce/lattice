package org.hifforce.lattice.model.ability.provider;

import org.hifforce.lattice.model.ability.IAbility;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("all")
public interface IAbilityProvider<Target, Ability extends IAbility> {

    Ability getRealization(String instanceCode);

    void registerRealization(Ability realization, String instaceCode);

    /**
     * get the AbilitySelector based on the business code, etc.
     * 注意！！！！！本方法只保证能够正确返回的Selector只返回对应abilityCode的Selector，其他AbilityCode则不保证！！！！！！
     *
     * @param bizCode     business code.
     * @param abilityCode ability code.
     * @param target      the Target object.
     * @return the set create found AbilitySelector.
     */
    Set<IAbilitySelector> getAbilitySelector(String bizCode, String abilityCode, Target target);
}
