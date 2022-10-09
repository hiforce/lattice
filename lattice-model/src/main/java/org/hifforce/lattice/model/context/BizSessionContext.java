package org.hifforce.lattice.model.context;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.hifforce.lattice.cache.invoke.InvokeCache;
import org.hifforce.lattice.model.business.ITemplate;
import org.hifforce.lattice.model.register.TemplateSpec;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public class BizSessionContext {

    private static final ThreadLocal<BizSessionContext> SESSION_CONTEXT_THREAD_LOCAL =
            ThreadLocal.withInitial(BizSessionContext::new);

    @Getter
    private final Map<String, List<TemplateSpec<? extends ITemplate>>>
            effectiveTemplates = Maps.newConcurrentMap();

    @SuppressWarnings("unused")
    public <T> void addExtObject(Class<? super T> klass, Object id, @Nullable T instance) {
        InvokeCache.instance().put(klass, id, instance);
    }

    @SuppressWarnings("unused")
    public <T> T getExtObject(Class<? extends T> klass, Object id) {
        return InvokeCache.instance().get(klass, id);
    }

    public static BizSessionContext currentContext() {
        BizSessionContext context = InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
        if (null != context) {
            return context;
        }
        return init();
    }


    public static BizSessionContext init() {
        BizSessionContext context = SESSION_CONTEXT_THREAD_LOCAL.get();
        InvokeCache.instance().put(BizSessionContext.class, BizSessionContext.class, context);
        return context;
    }

    public static void destroy() {
        SESSION_CONTEXT_THREAD_LOCAL.set(null);
        SESSION_CONTEXT_THREAD_LOCAL.remove();
    }
}
