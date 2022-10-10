package org.hiforce.lattice.runtime.cache.spec;

import org.hifforce.lattice.model.register.ExtensionPointSpec;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.MultiKeyCache;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class ExtensionSpecCache extends MultiKeyCache<String, Long, ExtensionPointSpec>
        implements LatticeCache {
    public ExtensionSpecCache() {
        super(2000);
    }
}
