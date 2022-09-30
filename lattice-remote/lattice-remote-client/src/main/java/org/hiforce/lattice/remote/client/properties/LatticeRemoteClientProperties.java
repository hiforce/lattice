package org.hiforce.lattice.remote.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.remote.client.PropertiesUtils;
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

        registryAddress = PropertiesUtils.getValueString("lattice.remote.registry.address");

    }
}
