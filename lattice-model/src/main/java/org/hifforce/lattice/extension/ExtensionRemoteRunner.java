package org.hifforce.lattice.extension;

import org.hifforce.lattice.model.ability.IBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public abstract class ExtensionRemoteRunner<R> extends ExtensionRunner<R> {

    public ExtensionRemoteRunner(String extensionCode, IBusinessExt model) {
        super(extensionCode, model);
    }
}
