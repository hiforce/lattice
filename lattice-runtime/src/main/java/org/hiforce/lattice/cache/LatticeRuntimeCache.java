package org.hiforce.lattice.cache;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.cache.ILatticeRuntimeCache;
import org.hifforce.lattice.model.ability.cache.IBusinessExtCache;
import org.hiforce.lattice.ability.cache.BusinessExtCache;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("unused")
@AutoService(ILatticeRuntimeCache.class)
public class LatticeRuntimeCache implements ILatticeRuntimeCache {
    @Override
    public IBusinessExtCache getBusinessExtCache() {
        return BusinessExtCache.getInstance();
    }
}
