package org.hiforce.lattice.model.register;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.cache.ITemplateCache;
import org.hiforce.lattice.cache.LatticeCacheFactory;
import org.hiforce.lattice.model.business.ITemplate;
import org.hiforce.lattice.model.business.TemplateType;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public abstract class TemplateSpec<T extends ITemplate> extends BaseSpec {

    @Getter
    @Setter
    private int priority;

    @Getter
    @Setter
    private TemplateType type;

    @Getter
    private final List<RealizationSpec> realizations = Lists.newArrayList();

    @Getter
    @Setter
    private boolean remote;

    @Setter
    private Long internalId;

    public Long getInternalId() {
        if (null == internalId) {
            ITemplateCache templateCache = LatticeCacheFactory.getInstance().getRuntimeCache().getTemplateIndex();
            internalId = templateCache.getSecondKeyViaFirstKey(getCode());
        }
        return internalId;
    }

    public abstract T newInstance();
}
