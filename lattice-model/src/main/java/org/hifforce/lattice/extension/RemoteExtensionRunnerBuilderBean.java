package org.hifforce.lattice.extension;

import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.register.TemplateSpec;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
public interface RemoteExtensionRunnerBuilderBean {

    <R> ExtensionRemoteRunner<R> build(
            IAbility ability, TemplateSpec templateSpec, String extCode, String scenario);
}
