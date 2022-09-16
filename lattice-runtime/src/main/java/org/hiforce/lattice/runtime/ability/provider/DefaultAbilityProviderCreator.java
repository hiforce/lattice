package org.hiforce.lattice.runtime.ability.provider;

import org.hifforce.lattice.model.ability.provider.IAbilityProviderCreator;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class DefaultAbilityProviderCreator implements IAbilityProviderCreator {
    @Override
    public DefaultBaseAbilityProvider createAbilityProvider() {
        return new DefaultBaseAbilityProvider();
    }
}
