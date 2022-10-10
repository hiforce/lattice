package org.hiforce.lattice.model.ability;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hiforce.lattice.cache.LatticeCacheFactory;
import org.hiforce.lattice.model.ability.cache.IBusinessExtCache;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public abstract class BusinessExt implements IBusinessExt {

    @Override
    public IBusinessExt getBusinessExtByCode(String extCode, String scenario) {
        IBusinessExtCache businessExtCache =
                LatticeCacheFactory.getInstance().getRuntimeCache().getBusinessExtCache();
        return businessExtCache.getCachedBusinessExt(this, extCode, scenario);
    }

    @Override
    public @NonNull List<IBusinessExt> getAllSubBusinessExt() {
        IBusinessExtCache invokeCache = LatticeCacheFactory.getInstance().getRuntimeCache().getBusinessExtCache();
        return invokeCache.getAllSubBusinessExt(this);
    }
}
