package org.hiforce.lattice.runtime.ability;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;
import org.hifforce.lattice.annotation.model.ExtensionAnnotation;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

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

    private AbilityContext context;

    public BaseLatticeAbility(IBizObject bizObject) {
        this.bizObject = bizObject;
        this.instanceCode = this.getClass().getName();
        this.delegate = new BaseLatticeAbilityDelegate(this);
    }

    public AbilityContext getContext() {
        if (null == context) {
            context = new AbilityContext(bizObject);
        }
        return context;
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

    @Override
    public <T, R> R reduceExecute(ExtensionCallback<BusinessExt, T> callback, @NotNull Reducer<T, R> reducer) {
        if( !Lattice.getInstance().isInitialized()){
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }
        enrichAbilityInvokeContext(callback);
        String extCode = getContext().getExtCode();
        return reduceExecute(extCode, callback, reducer);
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
        if( !Lattice.getInstance().isInitialized()){
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }
        return reduceExecute(extensionCode, callback, reducer, DEFAULT_FILTER);
    }

    @SuppressWarnings("unused")
    public final <T, R> R reduceExecute(String extensionCode,
                                        ExtensionCallback<BusinessExt, T> callback,
                                        @Nonnull Reducer<T, R> reducer, ExtensionFilter filter) {

        if( !Lattice.getInstance().isInitialized()){
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }
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
        if( !Lattice.getInstance().isInitialized()){
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }

        if (StringUtils.isEmpty(extCode)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0007");
        }
        getContext().setExtCode(extCode);

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

        enrichAbilityInvokeContext(callback);

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
        RunnerCollection<R> runnerCollection = delegate.loadExtensionRunners(extCode, filter);
        return runnerCollection.distinct()
                .reduceExecute(extCode, reducer, (ExtensionCallback<IBusinessExt, T>) callback, results);
    }

    @SuppressWarnings("all")
    private <T> void enrichAbilityInvokeContext(ExtensionCallback<BusinessExt, T> callback) {
        if (null != this.getContext().getExtMethod() &&
                null != this.getContext().getInvokeParams()) {
            return;
        }
        BusinessExt businessExt = this.getDefaultRealization();
        List<Object> extParams = Lists.newArrayList();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(businessExt.getClass());
        enhancer.setCallback((MethodInterceptor) (o, method, params, methodProxy) -> {
            this.getContext().setExtMethod(method);
            if (null != params) {
                for (Object p : params) {
                    extParams.add(p);
                }
            }
            this.getContext().setInvokeParams(extParams);
            if (StringUtils.isEmpty(getContext().getExtCode())) {
                ExtensionAnnotation annotation =
                        LatticeAnnotationUtils.getExtensionAnnotation(method);
                if (null != annotation) {
                    this.getContext().setExtCode(annotation.getCode());
                }
            }
            return null;
        });
        businessExt = (BusinessExt) enhancer.create();
        callback.apply(businessExt);
    }
}
