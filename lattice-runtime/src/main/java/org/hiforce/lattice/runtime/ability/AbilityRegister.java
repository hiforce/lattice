package org.hiforce.lattice.runtime.ability;

import org.hifforce.lattice.model.register.AbilitySpec;

import java.util.Collection;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public class AbilityRegister {

    private static AbilityRegister instance;

    private AbilityRegister() {

    }

    public static AbilityRegister getInstance() {
        if (null == instance) {
            instance = new AbilityRegister();
        }
        return instance;
    }

    public List<AbilitySpec> register(Collection<Class<?>> allClassSet) {
        return null;
    }
}
