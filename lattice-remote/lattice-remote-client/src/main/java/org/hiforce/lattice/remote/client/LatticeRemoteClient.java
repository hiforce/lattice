package org.hiforce.lattice.remote.client;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.hiforce.lattice.remote.client.model.RemoteBusiness;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/14
 */
@Service
public class LatticeRemoteClient implements InitializingBean {

    @Getter
    private final List<RemoteBusiness> supportRemoteBusinessList = Lists.newArrayList();

    @Getter
    private static LatticeRemoteClient instance;

    private LatticeRemoteClient() {

    }

    public void registerRemoteBusiness(RemoteBusiness... businesses) {
        if (null == businesses) {
            return;
        }
        supportRemoteBusinessList.addAll(Arrays.asList(businesses));
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }
}
