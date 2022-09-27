package org.hifforce.lattice.model.ability.provider;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface IAbilitySelector {

    /**
     * get the available domain abilities with specific ability code.
     *
     * @param abilityCode the ability unique code.
     * @return the available abilities.
     */
    List<String> availableAbilities(String abilityCode);
}
