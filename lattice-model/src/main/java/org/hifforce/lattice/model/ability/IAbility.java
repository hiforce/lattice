package org.hifforce.lattice.model.ability;


/**
 * @param <AbilityTarget>   the Target which current Ability serviced.
 * @param <ExtensionPoints> the ExtensionPoint which current Ability provided.
 * @author Rocky Yu
 * @since 2022/9/15
 */
public interface IAbility<AbilityTarget, ExtensionPoints extends IBusinessExt> {

    /**
     * @return current ability's unique code.
     */
    String getAbilityCode();

    /**
     * set current ability unique code.
     *
     * @param abilityCode the ability unique code.
     */
    void setAbilityCode(String abilityCode);

    /**
     * @return current domain ability's instance unique code.
     */
    String getInstanceCode();


    /**
     * check whether the target is current ability will support.
     *
     * @param bizCode the business unique code.
     * @param target  the target object.
     * @return true or false.
     */
    boolean supportChecking(String bizCode, AbilityTarget target);

    /**
     * get the extension points' realization by business code.
     *
     * @return the ExtensionPoints realization.
     */
    ExtensionPoints getDefaultRealization();

    /**
     * the current ability whether is enabled.
     *
     * @param instanceCode the instance create ability.
     * @return true or false.
     */
    boolean isEnabled(AbilityTarget target, String instanceCode);

}
