package org.hiforce.lattice.runtime.cache.template;


import org.hiforce.lattice.cache.ITemplateCache;
import org.hiforce.lattice.model.register.BaseSpec;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.MultiKeyCache;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2018/7/30
 */
public class TemplateCache extends MultiKeyCache<String, Long, BaseSpec>
        implements ITemplateCache, LatticeCache {

    public TemplateCache() {
        super(120);
    }
}
