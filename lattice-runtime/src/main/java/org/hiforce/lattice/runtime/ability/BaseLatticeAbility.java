package org.hiforce.lattice.runtime.ability;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.context.AbilityContext;
import org.hiforce.lattice.runtime.ability.execute.ExecuteResult;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.reduce.Reducer;

import javax.annotation.Nonnull;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public abstract class BaseLatticeAbility<Target, BusinessExt extends IBusinessExt>
        implements IAbility<Target, BusinessExt> {

    @Getter
    @Setter
    private String abilityCode;

    @Getter
    private final String instanceCode;

    private final BaseLatticeAbilityDelegate delegate;

    @SuppressWarnings("unused")
    public BaseLatticeAbility() {
        this.instanceCode = this.getClass().getName();
        this.delegate = new BaseLatticeAbilityDelegate(this);
    }

    public abstract AbilityContext getContext();


    @Override
    public boolean supportChecking(String bizCode, Target target) {
        return true;
    }

    @Override
    public boolean supportCustomization() {
        return true;
    }

    @Override
    public boolean hasDefaultExtension(String bizCode, Target target) {
        return true;
    }

    /**
     * Customization for current ability to judge whether is enabled.
     *
     * @param target       The BizObject target.
     * @param instanceCode current ability's instance code.
     * @return true or false.
     */
    public boolean isEnabled(Target target, String instanceCode) {
        return true;
    }

    /**
     * The ability will execute the extension's realization with reducer
     * When multi extension realization found.
     *
     * @param extensionCode the code of extension point.
     * @param callback      extension point's callback.
     * @param reducer       The result's reduce policy for multi-realization.
     * @return The result of extension point execution.
     */
    protected final <T, R> R reduceExecute(String extensionCode,
                                           ExtensionCallback<BusinessExt, T> callback,
                                           @Nonnull Reducer<T, R> reducer) {

        ExecuteResult<R> result = reduceExecuteWithDetailResult(extensionCode, callback, reducer);
        if (null == result || null == result.getResult()) {
            return null;
        }
        return result.getResult();
    }

    protected <T, R> ExecuteResult<R> reduceExecuteWithDetailResult(
            String extensionCode, ExtensionCallback<BusinessExt, T> callback,
            @Nonnull Reducer<T, R> reducer) {

        if (null == getContext().getBizObject()) {
            log.error("[Lattice]bizInstance is null, extensionCode: {}", extensionCode);
            return ExecuteResult.success(null, null, null);
        }
        if (getContext().getBizObject().getBizContext().getBizId() == null) {
            log.error("[Lattice]bizInstance id is null, extensionCode: {}", extensionCode);
            return ExecuteResult.success(null, null, null);
        }

        if (StringUtils.isEmpty(extensionCode)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0007");
        }

        String bizCode = getContext().getBizObject().getBizCode();
        if (StringUtils.isEmpty(bizCode)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0008");
        }

        RunnerCollection<BusinessExt, R> runnerCollection = delegate.loadExtensionRunners(extensionCode);
        return null;
    }
}
