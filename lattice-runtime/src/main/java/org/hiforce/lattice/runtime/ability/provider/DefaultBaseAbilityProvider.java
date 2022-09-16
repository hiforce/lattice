package org.hiforce.lattice.runtime.ability.provider;

import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.provider.IAbilitySelector;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class DefaultBaseAbilityProvider<Target, Ability extends IAbility>
        extends BaseAbilityProvider<Target, Ability> {

    @Override
    public Set<IAbilitySelector> getAbilitySelector(String bizCode, String abilityCode, Target target) {
        return null;
    }
}
