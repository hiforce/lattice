package org.hiforce.lattice.remote.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@Service("latticeRemoteClientProperties")
public class LatticeRemoteClientProperties implements InitializingBean {

    @Getter
    private static LatticeRemoteClientProperties instance;

    @Getter
    @Setter
    private String registryAddress;


    private LatticeRemoteClientProperties() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
        registryAddress = ApplicationProperties.getValueString("lattice.remote.registry.address");
        if (StringUtils.isEmpty(registryAddress)) {
            registryAddress = ApplicationProperties.getValueString("dubbo.registry.address");
        }
        if (StringUtils.isEmpty(registryAddress)) {
            String value = ApplicationProperties.getValueString("spring.cloud.nacos.config.server-addr");
            if (StringUtils.isNotEmpty(value)) {
                registryAddress = "nacos://" + value;
            }
        }
        if (StringUtils.isEmpty(registryAddress)) {
            String value = BootstrapProperties.getValueString("spring.cloud.nacos.config.server-addr");
            if (StringUtils.isNotEmpty(value)) {
                registryAddress = "nacos://" + value;
            }
        }
    }
}
