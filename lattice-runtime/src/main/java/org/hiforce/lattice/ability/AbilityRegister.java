package org.hiforce.lattice.ability;

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
}
