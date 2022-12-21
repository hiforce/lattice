package org.hiforce.lattice.remote.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.extension.ExtensionRemoteRunner;
import org.hiforce.lattice.extension.RemoteExtensionRunnerBuilder;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.register.TemplateSpec;
import org.hiforce.lattice.remote.client.LatticeRemoteClient;
import org.hiforce.lattice.remote.client.model.RemoteBusiness;
import org.hiforce.lattice.remote.client.model.RemoteExtension;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class DubboRunnerBuilder implements RemoteExtensionRunnerBuilder {
    @Override
    public <R> ExtensionRemoteRunner<R> build(IAbility ability, TemplateSpec templateSpec, String extCode, String scenario) {

        String bizCode = ability.getContext().getBizCode();

        RemoteBusiness remoteBusiness = LatticeRemoteClient.getInstance().getSupportRemoteBusinessList().stream()
                .filter(p -> StringUtils.equals(p.getBizCode(), bizCode))
                .findFirst().orElse(null);
        if (null == remoteBusiness) {
            log.debug("[Lattice-Remote] The business {} not support remote invoke.", bizCode);
            return null;
        }
        if (remoteBusiness.isForceSupportAllCodes()) {
            log.info("[Lattice-Remote] Force build the remote invoke runner for bizCode: {}", bizCode);
            return buildDubboExtensionRunner(RemoteExtension.of(extCode, true),
                    ability, templateSpec, extCode, scenario);
        }
        if (CollectionUtils.isEmpty(remoteBusiness.getExtensions())) {
            log.debug("[Lattice-Remote] Won't build remote invoke runner " +
                    "due to empty extension registered, bizCode: {}, extCode: {}", bizCode, extCode);
            return null;
        }

        RemoteExtension extension = remoteBusiness.getExtensions().stream()
                .filter(p -> StringUtils.equals(p.getExtCode(), extCode))
                .findFirst().orElse(null);
        if (null == extension) {
            log.debug("[Lattice-Remote] Won't build remote invoke runner " +
                    "due to specific extension not registered, bizCode: {}, extCode: {}", bizCode, extCode);
            return null;
        }
        log.debug("[Lattice-Remote] Create the remote runner for bizCode: {}, extCode: {}", bizCode, extCode);
        return buildDubboExtensionRunner(extension, ability, templateSpec, extCode, scenario);
    }

    private <R> DubboExtensionRunner<R> buildDubboExtensionRunner(
            RemoteExtension extension,
            IAbility ability, TemplateSpec templateSpec, String extCode, String scenario) {
        DubboExtensionRunner<R> runner = new DubboExtensionRunner<R>(extCode);
        runner.setRemoteExtension(extension);
        runner.setAbility(ability);
        runner.setTemplate(templateSpec);
        runner.setScenario(scenario);
        return runner;
    }
}
