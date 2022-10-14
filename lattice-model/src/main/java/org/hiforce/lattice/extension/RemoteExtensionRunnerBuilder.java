package org.hiforce.lattice.extension;

import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.register.TemplateSpec;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
@SuppressWarnings("all")
public interface RemoteExtensionRunnerBuilder {

    <R> ExtensionRemoteRunner<R> build(
            IAbility ability, TemplateSpec templateSpec, String extCode, String scenario);
}
