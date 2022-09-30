package org.hiforce.lattice.remote.runner;

import org.hifforce.lattice.extension.ExtensionRemoteRunner;
import org.hifforce.lattice.extension.ExtensionRunnerType;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.business.IBizObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@SuppressWarnings("all")
public class ExtensionDubboRunner extends ExtensionRemoteRunner {

    public ExtensionDubboRunner(String extensionCode, Object model) {
        super(extensionCode, model);
    }

    @Override
    public Object runFirstMatched(IBizObject bizObject, ExtensionCallback callback, RunnerExecuteResult executeResult) {
        return null;
    }

    @NotNull
    @Override
    public List runAllMatched(IBizObject bizObject, ExtensionCallback callback, RunnerExecuteResult executeResult) {
        return null;
    }

    @Override
    public ExtensionRunnerType getType() {
        return ExtensionRunnerType.RMI;
    }
}
