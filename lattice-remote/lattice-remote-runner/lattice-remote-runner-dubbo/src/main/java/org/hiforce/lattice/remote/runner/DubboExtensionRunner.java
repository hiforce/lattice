package org.hiforce.lattice.remote.runner;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.extension.ExtensionRemoteRunner;
import org.hiforce.lattice.extension.ExtensionRunnerType;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.ability.execute.ExtensionCallback;
import org.hiforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.model.register.TemplateSpec;
import org.hiforce.lattice.remote.client.LatticeRemoteInvoker;
import org.hiforce.lattice.remote.client.model.RemoteExtension;
import org.hiforce.lattice.remote.runner.init.LatticeDubboRunnerEnv;
import org.hiforce.lattice.remote.runner.key.DubboInvokeCacheKey;
import org.hiforce.lattice.utils.JacksonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@SuppressWarnings("all")
@Slf4j
public class DubboExtensionRunner<R> extends ExtensionRemoteRunner<R> {

    private static Map<DubboInvokeCacheKey, LatticeRemoteInvoker>
            INVOKE_CACHE = Maps.newHashMap();

    @Getter
    @Setter
    private IAbility ability;

    @Getter
    @Setter
    private TemplateSpec template;

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private RemoteExtension remoteExtension;

    public DubboExtensionRunner(String extensionCode) {
        super(extensionCode);
    }


    @Override
    public Object runFirstMatched(IBizObject bizObject, ExtensionCallback callback, RunnerExecuteResult executeResult) {

        IBusinessExt businessExt = ability.getDefaultRealization();
        /**
         * 对businessExt做代理，然后调用拦截实际入参
         */
        List<Object> extParams = ability.getContext().getInvokeParams();
        executeResult.setRunnerType(getType());
        try {
            return invoke(extParams);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            if (remoteExtension.isStrongDependency()) {
                throw new LatticeRuntimeException("LATTICE-RMI-DUBBO-0001", ex.getMessage());
            }
            return null;
        } finally {
            executeResult.setExecute(true);
        }
    }

    private Object invoke(List<Object> params) {
        String bizCode = template.getCode();
        String scenario = getScenario();

        DubboInvokeCacheKey key = new DubboInvokeCacheKey(bizCode, scenario, getExtensionCode());

        LatticeRemoteInvoker remoteInvoker = INVOKE_CACHE.get(key);
        if (null == remoteInvoker) {

            ApplicationConfig application = LatticeDubboRunnerEnv.getInstance().getApplication();
            RegistryConfig registry = LatticeDubboRunnerEnv.getInstance().getRegistry();

            ReferenceConfig<LatticeRemoteInvoker> reference = new ReferenceConfig<>();
            reference.setApplication(application);
            reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
            reference.setInterface(LatticeRemoteInvoker.class);
            reference.setVersion("1.0.0");
            reference.setGroup("lattice-" + bizCode);
            remoteInvoker = reference.get();
            INVOKE_CACHE.put(key, remoteInvoker);
        }
        String paramStr = null == params ? null : JacksonUtils.serializeWithoutException(params);
        log.info("[Lattice-Remote] remote invoke bizCode: {}, extCode: {}, params: {} ",
                bizCode, getExtensionCode(), paramStr);
        return remoteInvoker.invoke(bizCode, scenario,
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
