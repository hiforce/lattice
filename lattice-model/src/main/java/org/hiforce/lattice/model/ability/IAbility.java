package org.hiforce.lattice.model.ability;


import org.hiforce.lattice.model.ability.execute.ExtensionCallback;
import org.hiforce.lattice.model.ability.execute.Reducer;
import org.hiforce.lattice.model.context.AbilityContext;

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


    AbilityContext getContext();

    /**
     * Whether current ability support current bizObject
     *
     * @return default return true.
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
     * @param callback callback of the function.
     * @param reducer  The multi-result reduce policy.
     * @return the result of extension customization.
     */
    <T, R> R reduceExecute(ExtensionCallback<BusinessExt, T> callback,
                           @Nonnull Reducer<T, R> reducer);
}
