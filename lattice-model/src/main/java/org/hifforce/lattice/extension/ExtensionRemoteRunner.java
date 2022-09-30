package org.hifforce.lattice.extension;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public abstract class ExtensionRemoteRunner<T, R> extends ExtensionRunner<T, R> {

    public ExtensionRemoteRunner(String extensionCode, T model) {
        super(extensionCode, model);
    }
}
