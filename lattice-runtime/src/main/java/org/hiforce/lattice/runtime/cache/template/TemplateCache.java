package org.hiforce.lattice.runtime.cache.template;


import org.hiforce.lattice.cache.ITemplateCache;
import org.hiforce.lattice.model.business.IProduct;
import org.hiforce.lattice.model.register.BaseSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.MultiKeyCache;
import org.hiforce.lattice.sequence.SequenceGenerator;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2018/7/30
 */
public class TemplateCache extends MultiKeyCache<String, Long, BaseSpec>
        implements ITemplateCache, LatticeCache {

    private static TemplateCache instance;

    public static TemplateCache getInstance() {
        if (null == instance) {
            instance = new TemplateCache();
        }
        return instance;
    }

    private TemplateCache() {
        super(120);
    }


    @Override
    public void init() {
        Lattice.getInstance().getAllRegisteredProducts()
                .forEach(p -> put(p.getCode(), SequenceGenerator.next(IProduct.class.getName()), p));

        Lattice.getInstance().getAllRegisteredUseCases()
                .forEach(p -> put(p.getCode(), SequenceGenerator.next(IProduct.class.getName()), p));

        Lattice.getInstance().getAllRegisteredBusinesses()
                .forEach(p -> put(p.getCode(), SequenceGenerator.next(IProduct.class.getName()), p));
    }
}
