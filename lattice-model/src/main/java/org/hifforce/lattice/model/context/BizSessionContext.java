package org.hifforce.lattice.model.context;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.hifforce.lattice.model.business.ProductTemplate;
import org.hifforce.lattice.model.register.ProductSpec;

import java.util.List;
import java.util.Map;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public class BizSessionContext {

    public static ThreadLocal<BizSessionContext> SESSION_CONTEXT_THREAD_LOCAL =
            ThreadLocal.withInitial(BizSessionContext::new);

    @Getter
    private final Map<String, List<ProductSpec>> effectiveProducts = Maps.newConcurrentMap();
}
