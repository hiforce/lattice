package org.hiforce.lattice.extension;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public abstract class ExtensionRemoteRunner<R> extends ExtensionRunner<R> {

    public ExtensionRemoteRunner(String extensionCode) {
        super(extensionCode);
    }
}
