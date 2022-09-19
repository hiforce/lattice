package org.hiforce.lattice.runtime.cache;

import org.hifforce.lattice.model.register.ExtensionPointSpec;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class ExtensionSpecCache extends MultiKeyCache<String, Long, ExtensionPointSpec>{
    public ExtensionSpecCache() {
        super(2000);
    }
}
