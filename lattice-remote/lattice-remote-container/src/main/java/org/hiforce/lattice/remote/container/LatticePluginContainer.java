package org.hiforce.lattice.remote.container;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.*;
import org.hiforce.lattice.model.register.BusinessSpec;
import org.hiforce.lattice.remote.client.LatticeRemoteInvoker;
import org.hiforce.lattice.remote.client.properties.LatticeRemoteClientProperties;
import org.hiforce.lattice.remote.container.service.LatticeRemoteInvokerImpl;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public class LatticePluginContainer {

    private static LatticePluginContainer instance;

    private LatticePluginContainer() {

    }

    public static LatticePluginContainer getInstance() {
        if (null == instance) {
            instance = new LatticePluginContainer();
        }
        return instance;
    }

    public void start() {
        Lattice.getInstance().start();

        String registryAddress = LatticeRemoteClientProperties.getInstance().getRegistryAddress();
        if(StringUtils.isEmpty(registryAddress)){
            return;
        }

        ApplicationConfig application = new ApplicationConfig();
        application.setName("lattice-plugin-server");
        application.setId("lattice-plugin-server");

        MonitorConfig monitorConfig = new MonitorConfig();
        monitorConfig.setProtocol("dubbo-registry");
        application.setMonitor(monitorConfig);

        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(-1);
        protocol.setThreads(200);

        RegistryConfig registry = new RegistryConfig();

        registry.setAddress(registryAddress);
        registry.getMetaData().put("report.address",
                LatticeRemoteClientProperties.getInstance().getRegistryAddress());


        for (BusinessSpec businessSpec : TemplateRegister.getInstance().getBusinesses()) {
            ServiceConfig<LatticeRemoteInvoker> service = new ServiceConfig<>();
            service.setApplication(application);
            service.setRegistry(registry); // Use setRegistries() for multi-registry case
            service.setProtocol(protocol); // Use setProtocols() for multi-protocol case
            service.setInterface(LatticeRemoteInvoker.class);
            service.setRef(new LatticeRemoteInvokerImpl());
            service.setVersion("1.0.0");
            service.setGroup(String.format("lattice-%s", businessSpec.getCode()));
            service.export();
        }
    }
}
