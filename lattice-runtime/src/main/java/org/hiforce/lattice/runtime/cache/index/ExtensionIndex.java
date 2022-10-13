package org.hiforce.lattice.runtime.cache.index;

import org.hiforce.lattice.model.register.ExtensionSpec;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.MultiKeyCache;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class ExtensionIndex extends MultiKeyCache<String, Long, ExtensionSpec>
        implements LatticeCache {

    public ExtensionIndex() {
        super(2000);
    }

    @Override
    public void init() {

    }
}
