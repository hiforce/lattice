package org.hiforce.lattice.remote.runner;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.hifforce.lattice.extension.ExtensionRemoteRunner;
import org.hifforce.lattice.extension.ExtensionRunnerType;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.register.TemplateSpec;
import org.hiforce.lattice.remote.client.LatticeRemoteInvoker;
import org.hiforce.lattice.remote.runner.init.LatticeDubboRunnerEnv;
import org.jetbrains.annotations.NotNull;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.util.Collections;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@SuppressWarnings("all")
@Slf4j
public class ExtensionDubboRunner<R> extends ExtensionRemoteRunner<R> {

    @Getter
    @Setter
    private IAbility ability;

    @Getter
    @Setter
    private TemplateSpec template;

    @Getter
    @Setter
    private String scenario;

    public ExtensionDubboRunner(String extensionCode) {
        super(extensionCode);
    }


    @Override
    public Object runFirstMatched(IBizObject bizObject, ExtensionCallback callback, RunnerExecuteResult executeResult) {

        IBusinessExt businessExt = ability.getDefaultRealization();
        /**
         * 对businessExt做代理，然后调用拦截实际入参
         */
        List<Object> extParams = Lists.newArrayList();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(businessExt.getClass());
        enhancer.setCallback((MethodInterceptor) (o, method, params, methodProxy) -> {
            System.out.println("调用方法:" + method);
            if (null != params) {
                for (Object p : params) {
                    extParams.add(p);
                }
            }
            return methodProxy.invokeSuper(o, params);
        });

        businessExt = (IBusinessExt) enhancer.create();
        callback.apply(businessExt); //用于拦截代理，获取extParams

        executeResult.setRunnerType(getType());
        try {
            return invoke(extParams);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException();
        } finally {
            executeResult.setExecute(true);
        }
    }

    private Object invoke(List<Object> params) {
        ApplicationConfig application = LatticeDubboRunnerEnv.getInstance().getApplication();
        RegistryConfig registry = LatticeDubboRunnerEnv.getInstance().getRegistry();

        ReferenceConfig<LatticeRemoteInvoker> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setInterface(LatticeRemoteInvoker.class);
        reference.setVersion("1.0.0");
        reference.setGroup("lattice-" + template.getCode());

        LatticeRemoteInvoker remoteInvoker = reference.get();
        return remoteInvoker.invoke(template.getCode(), getScenario(),
                getExtensionCode(), (Object[]) params.toArray());
    }

    @NotNull
    @Override
    public List runAllMatched(IBizObject bizObject, ExtensionCallback callback, RunnerExecuteResult executeResult) {
        return Collections.singletonList(runFirstMatched(bizObject, callback, executeResult));
    }

    @Override
    public ExtensionRunnerType getType() {
        return ExtensionRunnerType.RMI;
    }
}
