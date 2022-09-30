package org.hiforce.lattice.runtime.ability;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.ability.execute.Reducer;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.context.AbilityContext;
import org.hifforce.lattice.model.register.ExtensionPointSpec;
import org.hifforce.lattice.utils.LatticeAnnotationUtils;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.delegate.BaseLatticeAbilityDelegate;
import org.hiforce.lattice.runtime.ability.execute.ExecuteResult;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.execute.filter.ExtensionFilter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hiforce.lattice.runtime.ability.execute.filter.ExtensionFilter.DEFAULT_FILTER;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public abstract class BaseLatticeAbility<BusinessExt extends IBusinessExt>
        implements IAbility<BusinessExt> {

    private final IBizObject bizObject;

    @Getter
    private final String instanceCode;

    private final BaseLatticeAbilityDelegate delegate;

    public BaseLatticeAbility(IBizObject bizObject) {
        this.bizObject = bizObject;
        this.instanceCode = this.getClass().getName();
        this.delegate = new BaseLatticeAbilityDelegate(this);
    }

    public AbilityContext getContext() {
        return new AbilityContext(bizObject);
    }

    public String getCode() {
        AbilityAnnotation annotation = LatticeAnnotationUtils.getAbilityAnnotation(this.getClass());
        if (null == annotation) {
            return null;
        }
        return annotation.getCode();
    }

    public <R> void handleReduceExecuteFailed(ExecuteResult<R> result) {
        log.error(result.getErrLogText());
    }

    @Override
    public boolean supportChecking() {
        return true;
    }

    @Override
    public boolean supportCustomization() {
        return true;
    }

    @Override
    public boolean hasDefaultExtension() {
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
    public final <T, R> R reduceExecute(String extensionCode,
                                        ExtensionCallback<BusinessExt, T> callback,
                                        @Nonnull Reducer<T, R> reducer) {

        return reduceExecute(extensionCode, callback, reducer, DEFAULT_FILTER);
    }

    @SuppressWarnings("unused")
    public final <T, R> R reduceExecute(String extensionCode,
                                        ExtensionCallback<BusinessExt, T> callback,
                                        @Nonnull Reducer<T, R> reducer, ExtensionFilter filter) {

        ExecuteResult<R> result = reduceExecuteWithDetailResult(
                extensionCode, callback, reducer, filter);
        if (null == result || null == result.getResult()) {
            return null;
        }
        if (!result.isSuccess()) {
            handleReduceExecuteFailed(result);
            return null;
        }
        return result.getResult();
    }

    @SuppressWarnings("all")
    public final <T, R> ExecuteResult<R> reduceExecuteWithDetailResult(
            String extCode, ExtensionCallback<BusinessExt, T> callback,
            @Nonnull Reducer<T, R> reducer, ExtensionFilter filter) {

        if (StringUtils.isEmpty(extCode)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0007");
        }

        if (null == getContext().getBizObject()) {
            return ExecuteResult.failed(extCode, Message.code("LATTICE-CORE-RT-0018"));
        }
        if (getContext().getBizObject().getBizContext().getBizId() == null) {
            return ExecuteResult.failed(extCode, Message.code("LATTICE-CORE-RT-0019"));
        }

        if (!supportChecking()) {
            return ExecuteResult.success(extCode, reducer.reduceName(),
                    Message.code("LATTICE-CORE-RT-0020", this.getClass().getName(),
                            Optional.ofNullable(getContext().getBizObject())
                                    .map(p -> p.getBizContext())
                                    .map(p -> p.getBizInfo()).orElse(getContext().getBizObject().getBizId().toString()), extCode));
        }

        ExtensionPointSpec extensionPointSpec =
                Lattice.getInstance().getLatticeRuntimeCache().getExtensionPointSpecByCode(extCode);
        if (null == extensionPointSpec && !Lattice.getInstance().isSimpleMode()) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0016", extCode);
        }
        if (null != extensionPointSpec && !reducer.reducerType().equals(extensionPointSpec.getReduceType())) {
            log.warn(Message.code("LATTICE-CORE-RT-0017", extCode, reducer.reducerType(),
                    extensionPointSpec.getReduceType()).getText());
        }

        String bizCode = getContext().getBizObject().getBizCode();
        if (StringUtils.isEmpty(bizCode)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0008");
        }

        List<T> results = new ArrayList<>(16);
        RunnerCollection<BusinessExt, R> runnerCollection = delegate.loadExtensionRunners(extCode, filter);
        return runnerCollection.distinct()
                .reduceExecute(extCode, reducer, (ExtensionCallback<IBusinessExt, T>) callback, results);
    }
}
