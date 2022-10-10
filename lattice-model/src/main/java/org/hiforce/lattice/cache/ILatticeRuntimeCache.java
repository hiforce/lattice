package org.hiforce.lattice.cache;

import org.hiforce.lattice.model.ability.cache.IBusinessExtCache;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface ILatticeRuntimeCache {

    IBusinessExtCache getBusinessExtCache();

    ITemplateCache getTemplateCache();
}
