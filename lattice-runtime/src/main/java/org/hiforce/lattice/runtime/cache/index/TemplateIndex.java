package org.hiforce.lattice.runtime.cache.index;


import org.hiforce.lattice.cache.ITemplateCache;
import org.hiforce.lattice.model.business.ITemplate;
import org.hiforce.lattice.model.register.BaseSpec;
import org.hiforce.lattice.model.register.TemplateSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.MultiKeyCache;
import org.hiforce.lattice.sequence.SequenceGenerator;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2018/7/30
 */
public class TemplateIndex extends MultiKeyCache<String, Long, BaseSpec>
        implements ITemplateCache, LatticeCache {

    private static TemplateIndex instance;

    public static TemplateIndex getInstance() {
        if (null == instance) {
            instance = new TemplateIndex();
        }
        return instance;
    }

    private TemplateIndex() {
        super(120);
    }


    @SuppressWarnings("all")
    public void addTemplateIndex(TemplateSpec template) {
        put(template.getCode(),SequenceGenerator.next(ITemplate.class.getName()), template);
    }

    @Override
    public void init() {
        Lattice.getInstance().getAllRegisteredProducts()
                .forEach(p -> put(p.getCode(), SequenceGenerator.next(ITemplate.class.getName()), p));

        Lattice.getInstance().getAllRegisteredUseCases()
                .forEach(p -> put(p.getCode(), SequenceGenerator.next(ITemplate.class.getName()), p));

        Lattice.getInstance().getAllRegisteredBusinesses()
                .forEach(p -> put(p.getCode(), SequenceGenerator.next(ITemplate.class.getName()), p));
    }
}
