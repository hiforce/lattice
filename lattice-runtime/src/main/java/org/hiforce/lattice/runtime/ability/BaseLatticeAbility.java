package org.hiforce.lattice.runtime.ability;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.AbilityAnnotation;
import org.hiforce.lattice.annotation.model.ExtensionAnnotation;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.ability.execute.ExtensionCallback;
import org.hiforce.lattice.model.ability.execute.Reducer;
import org.hiforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.model.context.AbilityContext;
import org.hiforce.lattice.model.register.ExtensionSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.delegate.BaseLatticeAbilityDelegate;
import org.hiforce.lattice.runtime.ability.execute.ExecuteResult;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.execute.filter.ExtensionFilter;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.utils.JacksonUtils;
import org.hiforce.lattice.utils.LatticeAnnotationUtils;
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

    @Getter
    private final LatticeRuntimeCache runtimeCache = Lattice.getInstance().getRuntimeCache();

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
        if (!Lattice.getInstance().isInitialized()) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }
        return reduceExecute(callback, reducer, DEFAULT_FILTER);
    }


    @SuppressWarnings("unused")
    @Deprecated
    public final <T, R> R reduceExecute(ExtensionCallback<BusinessExt, T> callback,
                                        @Nonnull Reducer<T, R> reducer, ExtensionFilter filter) {

        if (!Lattice.getInstance().isInitialized()) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }
        ExecuteResult<R> result = reduceExecuteWithDetailResult(callback, reducer, filter);
        if (null == result || null == result.getResult()) {
            return null;
        }
        log.debug("[Lattice] invoke result: {}", JacksonUtils.serializeWithoutException(result));
        if (!result.isSuccess()) {
            handleReduceExecuteFailed(result);
            return null;
        }
        return result.getResult();
    }

    @SuppressWarnings("all")
    private final <T, R> ExecuteResult<R> reduceExecuteWithDetailResult(
            ExtensionCallback<BusinessExt, T> callback,
            @Nonnull Reducer<T, R> reducer, ExtensionFilter filter) {

        if (!Lattice.getInstance().isInitialized()) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0023");
        }

        try {
            initAbiliinittyInvokeContext(callback);//init the ability context.
            String extCode = getContext().getExtCode();
            if (StringUtils.isEmpty(extCode)) {
                throw new LatticeRuntimeException("LATTICE-CORE-RT-0007");
            }

            if (null == getContext().getBizObject()) {
                return ExecuteResult.failed(bizObject.getBizCode(), extCode, Message.code("LATTICE-CORE-RT-0018"));
            }
            if (getContext().getBizObject().getBizContext().getBizId() == null) {
                return ExecuteResult.failed(bizObject.getBizCode(), extCode, Message.code("LATTICE-CORE-RT-0019"));
            }

            if (!supportChecking()) {
                return ExecuteResult.success(getContext().getBizCode(), extCode, reducer.reduceName(),
                        Message.code("LATTICE-CORE-RT-0020", this.getClass().getName(),
                                Optional.ofNullable(getContext().getBizObject())
                                        .map(p -> p.getBizContext())
                                        .map(p -> p.getBizInfo()).orElse(getContext().getBizObject().getBizId().toString()), extCode));
            }


            ExtensionSpec extensionSpec = getRuntimeCache().getExtensionCache().getExtensionSpecByCode(extCode);
            if (null == extensionSpec && !Lattice.getInstance().isSimpleMode()) {
                throw new LatticeRuntimeException("LATTICE-CORE-RT-0016", extCode);
            }
            if (null != extensionSpec && !reducer.reducerType().equals(extensionSpec.getReduceType())) {
                log.warn(Message.code("LATTICE-CORE-RT-0017", extCode, reducer.reducerType(),
                        extensionSpec.getReduceType()).getText());
            }

            String bizCode = getContext().getBizObject().getBizCode();
            if (StringUtils.isEmpty(bizCode)) {
                throw new LatticeRuntimeException("LATTICE-CORE-RT-0008");
            }

            List<T> results = new ArrayList<>(16);
            RunnerCollection<R> runnerCollection = delegate.loadExtensionRunners(extCode, filter);
            return runnerCollection.distinct()
                    .reduceExecute(extCode, reducer, (ExtensionCallback<IBusinessExt, T>) callback, results);
        } finally {
            this.context = null; //destroy the context.
        }
    }

    @SuppressWarnings("all")
    private <T> void initAbiliinittyInvokeContext(ExtensionCallback<BusinessExt, T> callback) {
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
            ExtensionAnnotation annotation =
                    LatticeAnnotationUtils.getExtensionAnnotation(method);
            if (null == annotation) {
                log.warn("[Lattice] invoke context, failed to get annotation, method={}", method.getName());
            } else {
                this.getContext().setExtCode(annotation.getCode());
                this.getContext().setExtName(annotation.getName());
            }
            log.debug("[Lattice] invoke context, method={}, annotation=[code={}, name={}], params={}", method.getName(),
                    getContext().getExtCode(), getContext().getExtName(), JacksonUtils.serializeWithoutException(extParams));
            return null;
        });
        businessExt = (BusinessExt) enhancer.create();
        callback.apply(businessExt);
    }
}
