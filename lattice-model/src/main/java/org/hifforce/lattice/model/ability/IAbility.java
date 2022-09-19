package org.hifforce.lattice.model.ability;


import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;

/**
 * @param <BusinessExt> the ExtensionPoint which current Ability provided.
 * @author Rocky Yu
 * @since 2022/9/15
 */
public interface IAbility<BusinessExt extends IBusinessExt> {

    /**
     * @return current ability's unique code.
     */
    String getCode();

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
    IBusinessExt getDefaultRealization();

    /**
     * the current ability whether is enabled.
     *
     * @return true or false.
     */
    boolean isEnabled();

    /**
     * Execute the extension's customization.
     *
     * @param extCode  the code of extension point.
     * @param callback callback of the function.
     * @param reducer  The multi-result reduce policy.
     * @return the result of extension customization.
     */
    <T, R> R reduceExecute(String extCode, ExtensionCallback<BusinessExt, T> callback,
                           @Nonnull Reducer<T, R> reducer);
}
