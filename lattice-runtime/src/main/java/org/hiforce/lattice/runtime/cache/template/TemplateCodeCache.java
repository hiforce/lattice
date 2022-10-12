package org.hiforce.lattice.runtime.cache.template;

import org.hiforce.lattice.model.business.BusinessTemplate;
import org.hiforce.lattice.model.register.BaseSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.sequence.SequenceGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class TemplateCodeCache implements LatticeCache {

    private static TemplateCodeCache instance;
    private final Map<String, Long> TEMPLATE_CODE_IDX_MAP = new ConcurrentHashMap<>(120);


    private TemplateCodeCache() {

    }

    public static TemplateCodeCache getInstance() {
        if (null == instance) {
            instance = new TemplateCodeCache();
        }
        return instance;
    }

    public Long getCodeIndex(String code) {
        return TEMPLATE_CODE_IDX_MAP.get(code);
    }


    @Override
    public void init() {
        Lattice.getInstance().getAllRegisteredBusinesses().stream()
                .map(BaseSpec::getCode)
                .forEach(bizCode -> TEMPLATE_CODE_IDX_MAP.put(bizCode, SequenceGenerator.next(BusinessTemplate.class.getName())));
        Lattice.getInstance().getAllRegisteredProducts().stream()
                .map(BaseSpec::getCode)
                .forEach(bizCode -> TEMPLATE_CODE_IDX_MAP.put(bizCode, SequenceGenerator.next(BusinessTemplate.class.getName())));
        Lattice.getInstance().getAllRegisteredUseCases().stream()
                .map(BaseSpec::getCode)
                .forEach(bizCode -> TEMPLATE_CODE_IDX_MAP.put(bizCode, SequenceGenerator.next(BusinessTemplate.class.getName())));
    }

    @Override
    public void clear() {

    }
}
