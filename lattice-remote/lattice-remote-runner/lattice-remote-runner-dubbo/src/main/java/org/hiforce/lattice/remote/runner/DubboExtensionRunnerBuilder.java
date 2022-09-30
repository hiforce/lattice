package org.hiforce.lattice.remote.runner;

import org.hifforce.lattice.extension.ExtensionRemoteRunner;
import org.hifforce.lattice.extension.RemoteExtensionRunnerBuilderBean;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.register.TemplateSpec;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
@Service
@SuppressWarnings("all")
public class DubboExtensionRunnerBuilder implements RemoteExtensionRunnerBuilderBean {
    @Override
    public <R> ExtensionRemoteRunner<R> build(IAbility ability, TemplateSpec templateSpec, String extCode, String scenario) {
        ExtensionDubboRunner<R> runner = new ExtensionDubboRunner<R>(extCode);
        runner.setAbility(ability);
        runner.setTemplate(templateSpec);
        runner.setScenario(scenario);
        return runner;
    }
}
