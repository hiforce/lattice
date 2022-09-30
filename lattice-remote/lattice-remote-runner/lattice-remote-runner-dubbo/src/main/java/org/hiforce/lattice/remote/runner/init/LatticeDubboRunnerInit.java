package org.hiforce.lattice.remote.runner.init;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@Service
public class LatticeDubboRunnerInit implements InitializingBean {

    @Override
    public void afterPropertiesSet() {

//        ReferenceConfig<LatticeRemoteInvoker> reference = new ReferenceConfig<>();
//        reference.setInterface(LatticeRemoteInvoker.class);
//        reference.setGeneric("true");
//        reference.setProtocol("");
//        reference.setGroup("lattice-business.a");
//
//
//        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
//
//        ApplicationConfig applicationConfig = new ApplicationConfig("lattice-rt-consumer");
//        applicationConfig.setQosEnable(false);
//        applicationConfig.setQosPort(-1);
//        bootstrap.application(applicationConfig)
//                .registry(new RegistryConfig("nacos://172.18.70.228:8848"))
//                .protocol(new ProtocolConfig(CommonConstants.DUBBO, -1))
//                .reference(reference)
//                .start();
//
//
//        LatticeRemoteInvoker demoService = bootstrap.getCache().get(reference);

    }

    public static void main(String[] args) {
//        LatticeDubboRunnerInit init = new LatticeDubboRunnerInit();
//        init.afterPropertiesSet();
    }
}
