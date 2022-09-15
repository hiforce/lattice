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
     * Checking current ability effective or not.
     * Default is effective....
     *
     * @param bizCode business code.
     * @param target  the Target.
     * @return true or false.
     */
    boolean supportChecking(String bizCode, AbilityTarget target);

    /**
     * Whether current ability support customization from plugin's extension realization.
     *
     * @return true or false.
     */
    boolean supportCustomization(String bizCode, AbilityTarget target);

    /**
     * Whether current ability invoke getDefaultRealization() if the extension
     * realizaiton not found in the Plugin.
     *
     * @param bizCode business code.
     * @param target  the Target.
     * @return true or false.
     */
    boolean hasDefaultExtension(String bizCode, AbilityTarget target);


    /**
     * get the extension points' realization by business code.
     *
     * @return the ExtensionPoints realization.
     */
    ExtensionPoints getDefaultRealization(String bizCode, AbilityTarget target);

    /**
     * the current ability whether is enabled.
     *
     * @param instanceCode the instance create ability.
     * @return true or false.
     */
    boolean isEnabled(AbilityTarget target, String instanceCode);
}
