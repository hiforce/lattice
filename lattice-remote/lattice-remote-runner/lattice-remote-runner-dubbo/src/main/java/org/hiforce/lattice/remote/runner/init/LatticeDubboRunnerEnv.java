package org.hiforce.lattice.remote.runner.init;

import lombok.Getter;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.hiforce.lattice.remote.client.properties.LatticeRemoteClientProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@Service
public class LatticeDubboRunnerEnv implements InitializingBean {

    @Getter
    private static LatticeDubboRunnerEnv instance;

    @Getter
    private ApplicationConfig application;

    @Getter
    private RegistryConfig registry;

    @Override
    public void afterPropertiesSet() {
        instance = this;
        application = new ApplicationConfig();
        application.setName("lattice-rt-consumer");
        registry = new RegistryConfig();
        registry.setAddress(LatticeRemoteClientProperties.getInstance().getRegistryAddress());

    }
}
