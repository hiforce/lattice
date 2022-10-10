package org.hiforce.lattice.remote.runner;

import org.hiforce.lattice.extension.ExtensionRemoteRunner;
import org.hiforce.lattice.extension.RemoteExtensionRunnerBuilderBean;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.register.TemplateSpec;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
@Service
@SuppressWarnings("all")
public class DubboRunnerBuilder implements RemoteExtensionRunnerBuilderBean {
    @Override
    public <R> ExtensionRemoteRunner<R> build(IAbility ability, TemplateSpec templateSpec, String extCode, String scenario) {
        DubboExtensionRunner<R> runner = new DubboExtensionRunner<R>(extCode);
        runner.setAbility(ability);
        runner.setTemplate(templateSpec);
        runner.setScenario(scenario);
        return runner;
    }
}
