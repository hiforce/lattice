package org.hifforce.lattice.model.register;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.cache.LatticeCacheFactory;
import org.hifforce.lattice.model.ability.IBusinessExt;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class RealizationSpec extends BaseSpec {

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private IBusinessExt businessExt;

    @Getter
    @Setter
    private Class<IBusinessExt> businessExtClass;

    /**
     * The extension points current realization supported.
     */
    @Getter
    private final Set<String> extensionCodes = Sets.newHashSet();

    @Setter
    private Long internalId;

    public Long getInternalId() {
        if (null == internalId) {
            ITemplateCache templateCache = LatticeCacheFactory.getInstance().getRuntimeCache().getTemplateCache();
            internalId = templateCache.getSecondKeyViaFirstKey(getCode());
        }
        return internalId;
    }
}
