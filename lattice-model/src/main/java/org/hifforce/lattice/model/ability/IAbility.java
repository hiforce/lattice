package org.hifforce.lattice.model.ability;


/**
 * @param <ExtensionPoints> the ExtensionPoint which current Ability provided.
 * @author Rocky Yu
 * @since 2022/9/15
 */
public interface IAbility<BusinessExt extends IBusinessExt> {

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
     * @return true or false.
     */
    boolean supportChecking();

    /**
     * Whether current ability support customization from plugin's extension realization.
     *
     * @return true or false.
     */
    boolean supportCustomization();

    /**
     * Whether current ability invoke getDefaultRealization() if the extension
     * realizaiton not found in the Plugin.
     *
     * @return true or false.
     */
    boolean hasDefaultExtension();


    /**
     * get the extension points' realization by business code.
     *
     * @return the ExtensionPoints realization.
     */
    BusinessExt getDefaultRealization();

    /**
     * the current ability whether is enabled.
     *
     * @return true or false.
     */
    boolean isEnabled();
}
